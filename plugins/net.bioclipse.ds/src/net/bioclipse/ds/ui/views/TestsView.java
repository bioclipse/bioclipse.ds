/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.ui.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculesEditor;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.Activator;
import net.bioclipse.ds.business.IDSManager;
import net.bioclipse.ds.model.ErrorResult;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.TestHelper;
import net.bioclipse.ds.model.TestRun;
import net.bioclipse.ds.model.impl.DSException;
import net.bioclipse.jobs.BioclipseJob;
import net.bioclipse.jobs.BioclipseJobUpdateHook;
import net.bioclipse.jobs.BioclipseUIJob;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.*;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

public class TestsView extends ViewPart implements IPartListener{

    private static final Logger logger = Logger.getLogger(TestsView.class);

    public static final String COLOR_PROP = "SMARTS_MATCHING_COLOR";

    private TreeViewer viewer;
    private Action runAction;

    //Kepp track of existing mappings from editor to TestRun
    private Map<IWorkbenchPart, List<TestRun>> editorTestMap;
    
    //The active test runs. Initializes upon test run, and updates on editor switch
    private List<TestRun> activeTestRuns;

    //Tracks the state in the view. True if a run has been made.
    private boolean executed;

    private Action clearAction;

    private Action excludeAction;

    private Action includeAction;

    private Action expandAllAction;

    private Action collapseAllAction;

    private Action refreshAction;

    private List<BioclipseJob<List<ITestResult>>> runningJobs;
    
    /**
     * The constructor.
     */
    public TestsView() {
    }

    public List<BioclipseJob<List<ITestResult>>> getRunningJobs() {
        return runningJobs;
    }

    public boolean isExecuted() {
        return executed;
    }
    public void setExecuted( boolean executed ) {
        this.executed = executed;
    }

    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     */
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
//        drillDownAdapter = new DrillDownAdapter(viewer);
        viewer.setContentProvider(new TestsViewContentProvider());
        viewer.setLabelProvider(new TestsViewLabelProvider());
        viewer.setSorter(new ViewerSorter());
//        viewer.addFilter( new NoErrorsFilter() );

        //Init with available tests
        viewer.setInput(TestHelper.readTestsFromEP().toArray());

        // Create the help context id for the viewer's control
        PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "com.genettasoft.warningsystem.ui.viewer");
        makeActions();
        hookContextMenu();
        contributeToActionBars();

        editorTestMap=new HashMap<IWorkbenchPart, List<TestRun>>();
        
        runningJobs=new ArrayList<BioclipseJob<List<ITestResult>>>();

        
        //Listen for part lifecycle events to react on editors
        getSite().getWorkbenchWindow().getPartService().addPartListener(this);
        
        getSite().setSelectionProvider(viewer);

    }

    /**
     * Clean up part listener
     */
    @Override
    public void dispose() {
        super.dispose();
        getSite().getWorkbenchWindow().getPartService().removePartListener(this);
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                updateActionStates();
                TestsView.this.fillContextMenu(manager);
            }

        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {
    }

    private void fillContextMenu(IMenuManager manager) {
        manager.add(runAction);
        manager.add(clearAction);
        manager.add(new Separator());
        manager.add(includeAction);
        manager.add(excludeAction);
        manager.add(new Separator());
        manager.add(refreshAction);
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(runAction);
        manager.add(clearAction);
        manager.add(new Separator());
        manager.add(includeAction);
        manager.add(excludeAction);
        manager.add(new Separator());
        manager.add(expandAllAction);
        manager.add(collapseAllAction);
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }
    
    private void updateActionStates() {

        if (activeTestRuns!=null && activeTestRuns.size()>0)
            runAction.setEnabled( true );
        else
            runAction.setEnabled( false );
        
        boolean testSelected=false;
        for (Object obj : ((IStructuredSelection)viewer.getSelection()).toList()){
            if ( obj instanceof TestRun ) {
                testSelected=true;
            }
            else if ( obj instanceof IDSTest ) {
                testSelected=true;
            }
        }
        if (testSelected){
            includeAction.setEnabled( true );
            excludeAction.setEnabled( true );
        }
        else{
            includeAction.setEnabled( false );
            excludeAction.setEnabled( false );
        }
    }

    private void makeActions() {
        runAction = new Action() {
            public void run() {
                doRunAllTests();
            }
        };
        runAction.setText("Run all Tests");
        runAction.setToolTipText("Runs the Decision Support Tests " +
        		"on the active molecule(s)");
        runAction.setImageDescriptor(Activator.getImageDecriptor( "icons2/smallRun.gif" ));
        runAction.setDisabledImageDescriptor( Activator.getImageDecriptor( "icons2/smallRun_dis.gif" ));

        clearAction = new Action() {
            public void run() {
                doClearAllTests();
            }
        };
        clearAction.setText("Clear all Tests");
        clearAction.setToolTipText("Clear all active tests");
        clearAction.setImageDescriptor(Activator.getImageDecriptor( "icons2/broom.png" ));

        excludeAction = new Action() {
            public void run() {
                doExcludeSelectedTests();
            }
        };
        excludeAction.setText("Exclude test");
        excludeAction.setToolTipText("Exclude selected test(s)");
        excludeAction.setImageDescriptor(Activator.getImageDecriptor( "icons2/item_delete.gif" ));
        excludeAction.setDisabledImageDescriptor(Activator.getImageDecriptor( "icons2/item_delete_dis.gif" ));

        includeAction = new Action() {
            public void run() {
                doExcludeSelectedTests();
            }
        };
        includeAction.setText("Include test");
        includeAction.setToolTipText("Include selected test(s)");
        includeAction.setImageDescriptor(Activator.getImageDecriptor( "icons2/item_add.gif" ));
        includeAction.setDisabledImageDescriptor(Activator.getImageDecriptor( "icons2/item_add_dis.gif" ));

        
        collapseAllAction = new Action() {
            public void run() {
                viewer.collapseAll();
            }
        };
        collapseAllAction.setText("Collapse all");
        collapseAllAction.setToolTipText("Collapse all tests");
        collapseAllAction.setImageDescriptor(Activator.getImageDecriptor( "icons2/collapseall.gif" ));

        expandAllAction = new Action() {
            public void run() {
                viewer.expandAll();
            }
        };
        expandAllAction.setText("Expand all");
        expandAllAction.setToolTipText("Expand all tests to reveal hits");
        expandAllAction.setImageDescriptor(Activator.getImageDecriptor( "icons2/expandall.gif" ));

        refreshAction = new Action() {
            public void run() {
                viewer.refresh();
            }
        };
        refreshAction.setText("Refresh");
        refreshAction.setToolTipText("Force a refresh of all tests' status");
        refreshAction.setImageDescriptor(Activator.getImageDecriptor( "icons2/refresh2.png" ));

    }
    
    protected void doExcludeSelectedTests() {

        IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
        for (Object obj : sel.toList()){
            if ( obj instanceof IDSTest ) {
                IDSTest dstest = (IDSTest) obj;
                dstest.setExcluded( true );
            }
            else if ( obj instanceof TestRun ) {
                TestRun testrun = (TestRun) obj;
                testrun.setExcluded( true );
            }
        }
        
    }

    protected void doClearAllTests() {

        IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                            .getActivePage().getActiveEditor();
        if ( editor instanceof JChemPaintEditor ) {
            JChemPaintEditor jcp = (JChemPaintEditor) editor;
            doClearNewTests( jcp );
        }else{
            showError( "NOT IMPLEMENTED FOR EDITOR: " + editor );
        }
        
    }

    private void doRunAllTests() {

        logger.debug("Running tests...");

        if (activeTestRuns==null || activeTestRuns.size()<=0){
            showMessage( "No active testruns to run" );
            return;
        }

        //Get the molecule from the editor
        //Asumption: All testruns operate on the same molecule
        IEditorPart part=activeTestRuns.get( 0 ).getEditor();
        if (!( part instanceof JChemPaintEditor )) {
            showError("The editor: " + part + " is not " +
            "supported to run DS tests on.");
            return;
        }
        JChemPaintEditor jcp = (JChemPaintEditor) part;
        final ICDKMolecule mol=jcp.getCDKMolecule();
        
        //Cancel all running tests
        for (BioclipseJob<List<ITestResult>> job : runningJobs){
            //Ask job to cancel
            job.cancel();
            logger.debug("Job: " + job.getName() + " asked to cancel.");
        }

        //Wait for all runnig jobs to cancel
        for (BioclipseJob<List<ITestResult>> job : runningJobs){
            //Ask job to cancel
            logger.debug("Waiting for Job: " + job.getName() + " to finish...");
            try {
                job.join();
            } catch ( InterruptedException e ) {
            }
            logger.debug("Job: " + job.getName() + " finished.");
        }


        //We need to clear previous tests if already run
        if (isExecuted()==true){
            doClearNewTests( jcp );
            activeTestRuns=editorTestMap.get( part );
//            partActivated( jcp );
            viewer.refresh();
        }
        setExecuted( true );

        IDSManager ds=Activator.getDefault().getJavaManager();
        for (final TestRun tr : activeTestRuns){

            logger.debug( "===== Testrun: " + tr + " started" );
            tr.setStatus( TestRun.RUNNING );
            viewer.refresh(tr);

            runTestAsJobs( mol, ds, tr ); 
//            runTestAsJobWithGuiUpdate( mol, ds, tr ); 
        }

        logger.debug( "===== All testruns started" );
    }

    private void runTestAsJobs( final ICDKMolecule mol, IDSManager ds, final TestRun tr ) {

            try {

                
                //Start up a job with the test
                BioclipseJob<List<ITestResult>> job = 
                    ds.runTest( tr.getTest().getId(), mol, 
                    new BioclipseJobUpdateHook(tr.getTest().getName()));
                
                //Store ref to job in list
                runningJobs.add(job);
                

            job.addJobChangeListener( new IJobChangeListener(){

                public void aboutToRun( IJobChangeEvent event ) {
                }

                public void awake( IJobChangeEvent event ) {
                }

                @SuppressWarnings("unchecked")
                public void done( IJobChangeEvent event ) {

                    final BioclipseJob<List<ITestResult>> job=(BioclipseJob<List<ITestResult>>) event.getJob();
                    final List<ITestResult> matches = job.getReturnValue();
                    
                    //Update viewer in SWT thread
                    Display.getDefault().asyncExec( new Runnable(){
                        public void run() {

                            logger.debug( "лл Job done: " + tr.getTest().getName() );
                            logger.debug( "лл Matches: " + matches.size());

                            boolean hasErrors=false;

                            for (ITestResult result : matches){
                                
                                if ( result instanceof ErrorResult ) {
                                    ErrorResult eres = (ErrorResult) result;
                                    logger.debug("Test: " + tr + " returned error: " + eres.getName());
                                    result.setTestRun( tr );
                                    hasErrors=true;
                                }
                                    result.setTestRun( tr );
                                    tr.addMatch(result);
                            } 
                            tr.setMatches( matches );
                            if (hasErrors==true)
                                tr.setStatus( TestRun.FINISHED_WITH_ERRORS );
                            else
                                tr.setStatus( TestRun.FINISHED );

                            logger.debug( "===== Testrun: " + tr + " finished" );
                            
                            viewer.refresh( tr );
                            
                            //This job is done, remove from list of running jobs
                            getRunningJobs().remove( job );

                            }
                    });


                }

                public void running( IJobChangeEvent event ) {
                }

                public void scheduled( IJobChangeEvent event ) {
                }

                public void sleeping( IJobChangeEvent event ) {
                }});
                
            } catch ( BioclipseException e ) {
                logger.error( "Error running test: " + tr.getTest() + 
                              ": " + e.getMessage());
                LogUtils.debugTrace( logger, e );
            }

    }

    @Deprecated
    private void runTestAsJobWithGuiUpdate( final ICDKMolecule mol,
                                            IDSManager ds, final TestRun tr ) {

        try {
            ds.runTest( tr.getTest().getId(), mol, new BioclipseUIJob<List<ITestResult>>(){

                @Override
                public void runInUI() {
                    List<ITestResult> matches = new ArrayList<ITestResult>(getReturnValue());

                    boolean hasErrors=false;

                    for (ITestResult result : matches){
                        
                        if ( result instanceof ErrorResult ) {
                            ErrorResult eres = (ErrorResult) result;
                            //TODO: handle how errors should be presented in UI here
                            logger.debug("Test: " + tr + " returned error: " + eres.getName());
                            result.setTestRun( tr );
                            hasErrors=true;
//                                try {
//                                    IMarker marker = mol.getResource().createMarker( IMarker.PROBLEM );
//                                    marker.setAttribute( IMarker.MESSAGE, eres );
//                                    marker.setAttribute( IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
//                                } catch ( CoreException e ) {
//                                    e.printStackTrace();
//                                }
                        }
//                            else{
                            result.setTestRun( tr );
                            tr.addMatch(result);
//                            }
                    } 
                    tr.setMatches( matches );
                    if (hasErrors==true)
                        tr.setStatus( TestRun.FINISHED_WITH_ERRORS );
                    else
                        tr.setStatus( TestRun.FINISHED );

                    logger.debug( "===== Testrun: " + tr + " finished" );

                    viewer.refresh( tr );
                }

            });
        } catch ( BioclipseException e ) {
            logger.error( "Error running test: " + tr.getTest() + 
                          ": " + e.getMessage());
            LogUtils.debugTrace( logger, e );
        }
    }

    private void showMessage(String message) {
        MessageDialog.openInformation(
                                      viewer.getControl().getShell(),
                                      "Decision support",
                                      message);
    }
    private void showError(String message) {
        MessageDialog.openError( 
                                      viewer.getControl().getShell(),
                                      "Decision support",
                                      message);
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    private boolean isSupportedEditor( IWorkbenchPart part ) {
        if ( part instanceof JChemPaintEditor ) {
            return true;
        }
        if ( part instanceof MoleculesEditor ) {
            return true;
        }

        //Not supported
        return false;
    }

    private void updateView() {
        
        if (getSite()==null) return;
        if (getSite().getWorkbenchWindow()==null) return;
        if (getSite().getWorkbenchWindow().getActivePage()==null) return;
        
        if (getSite().getWorkbenchWindow().getActivePage().getActiveEditor()==null){
          activeTestRuns=null;
        }
        
        if (activeTestRuns!=null && activeTestRuns.size()>0){
            viewer.setInput( activeTestRuns.toArray() );
            viewer.refresh();
        }else{
         //ok, we have nothing.
            viewer.setInput(TestHelper.readTestsFromEP().toArray());
            viewer.refresh();
        }

        updateActionStates();

    }

    /**
     * We have a new editor. Create a new TestRun for the molecules it contains 
     * and add to local model map.
     * @param part 
     */
    private void addNewTestRuns(IWorkbenchPart part) {

        if ( part instanceof JChemPaintEditor ) {
            final JChemPaintEditor jcp = (JChemPaintEditor) part;
            // Register interest in changes from editor
            //    as a
            jcp.addPropertyChangedListener( new IPropertyChangeListener() {
                public void propertyChange( PropertyChangeEvent event ) {

                    if(event.getProperty().equals( JChemPaintEditor.
                                                   STRUCUTRE_CHANGED_EVENT )) {
                        
                        

                        // editor model has changed
                        // do stuff...
                        logger.debug(
                           ((JChemPaintEditor)event.getSource()).getTitle()
                           +" editor has changed");
                        
                        doClearNewTests( jcp );

//                        logger.debug(
//                            ((JChemPaintEditor)event.getSource()).getTitle()
//                            + " editor has changed");
                    }
                }
            });

            doClearNewTests( jcp );

            return;
        }

        else if ( part instanceof MoleculesEditor ) {
//            MoleculesEditor moleditor = (MoleculesEditor) part;

            showMessage( "MOLTABLE NOT YET SUPPORTED!" );
            logger.debug("MOLTABLE NOT YET SUPPORTED!");

            return;
        }

    }

    private void doClearNewTests( JChemPaintEditor jcp ) {

        List<TestRun> newTestRuns=new ArrayList<TestRun>();

        IDSManager ds = Activator.getDefault().getJavaManager();
        
        try {
            for (String testid : Activator.getDefault().getJavaManager().getTests()){
                IDSTest test = ds.getTest( testid );
                TestRun newTestRun=new TestRun(jcp,test);
                newTestRuns.add( newTestRun );
            }
        } catch ( BioclipseException e ) {
            e.printStackTrace();
        }
        
        editorTestMap.put( jcp, newTestRuns );
        activeTestRuns=newTestRuns;
        setExecuted( false );
        updateView();
    }

    
    
    
    /* ================================
     * Below is for part lifecycle events
     *====================================  */

    /**
     * 
     */
    public void partActivated( IWorkbenchPart part ) {
        if (!(isSupportedEditor(part))) return;
//        logger.debug("Part:" + part.getTitle() + " activated");

        if (editorTestMap.keySet().contains( part )){
            activeTestRuns=editorTestMap.get( part );
        }else {
            addNewTestRuns(part);
        }
        updateView();
    }


    public void partBroughtToTop( IWorkbenchPart part ) {
        if (!(isSupportedEditor(part))) return;
//        logger.debug("Part:" + part.getTitle() + " brought to top");
        
        if (editorTestMap.keySet().contains( part )){
            activeTestRuns=editorTestMap.get( part );
        }else {
            addNewTestRuns(part);
        }
        updateView();
    }


    public void partClosed( IWorkbenchPart part ) {
        if (!(isSupportedEditor(part))) return;
//        logger.debug("Part:" + part.getTitle() + " closed");
        
        if (editorTestMap.keySet().contains( part )){
            editorTestMap.remove( part );
            updateView();
        }
    }

    /**
     * Occurs when lost focus. Does not mean it is not topmost editor, but 
     * could mean an unsupported editor is topmost. Need to verify this and 
     * clean in that case.
     */
    public void partDeactivated( IWorkbenchPart part ) {
        if (!(isSupportedEditor(part))) return;
//        logger.debug("Part:" + part.getTitle() + " deactivated");
    }

    public void partOpened( IWorkbenchPart part ) {
        if (!(isSupportedEditor(part))) return;
//        logger.debug("Part:" + part.getTitle() + " opened");
    }

    

}
