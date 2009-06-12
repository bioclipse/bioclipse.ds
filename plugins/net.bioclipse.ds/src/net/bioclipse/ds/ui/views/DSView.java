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

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculesEditor;
import net.bioclipse.cdk.ui.sdfeditor.editor.MultiPageMoleculesEditorPart;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.Activator;
import net.bioclipse.ds.business.IDSManager;
import net.bioclipse.ds.business.TestHelper;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.TestRun;
import net.bioclipse.ds.model.report.AbstractTestReportModel;
import net.bioclipse.ds.model.report.DSRow;
import net.bioclipse.ds.model.report.DSSingleReportModel;
import net.bioclipse.ds.model.report.ReportHelper;
import net.bioclipse.ds.ui.IDSViewNoCloseEditor;
import net.bioclipse.jobs.BioclipseJob;
import net.bioclipse.jobs.BioclipseJobUpdateHook;

import org.apache.log4j.Logger;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.part.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.openscience.cdk.CDKConstants;

public class DSView extends ViewPart implements IPartListener{

    private static final Logger logger = Logger.getLogger(DSView.class);

    public static final String VIEW_ID="net.bioclipse.ds.ui.views.DSView";
    
    private static Image questImg;
    private static Image warnImg;
    private static Image crossImg;
    private static Image checkImg;
    private static Image wheelImg;

    
    private TreeViewer viewer;
    private Action runAction;

    //Kepp track of existing mappings from editor to TestRun
    private Map<IWorkbenchPart, List<TestRun>> editorTestMap;

    //Kepp track of existing mappings from editor to TestRun
    private Map<IWorkbenchPart, IPropertyChangeListener> editorListenerMap;

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
     * Used to store and set selection for a new run
     */
    private IStructuredSelection storedSelection;

    private Action autoRunAction;
    
    private boolean autorun;

    //The currently shown image in consensusView 
    private Image consensusImage;

    private Text consensusText;

    private Canvas consensusCanvas;

    private Action helpAction;

    private static DSView instance;
    
    /**
     * The constructor.
     */
    public DSView() {
    }
    
    public static DSView getInstance(){
        return instance;
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
        
        DSView.instance=this;
        
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        parent.setLayout(gridLayout);

        viewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.setContentProvider(new DSViewContentProvider());
        viewer.setLabelProvider(new DecoratingLabelProvider(new DSViewLabelProvider(),new DSViewDecorator()));
//        viewer.setSorter(new ViewerSorter());
        viewer.addSelectionChangedListener( new ISelectionChangedListener(){
            public void selectionChanged( SelectionChangedEvent event ) {
                updateActionStates();
            }
            
        });

        GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
        viewer.getTree().setLayoutData(gridData);
        
        
        //Init with available tests
        viewer.setInput(TestHelper.readTestsFromEP().toArray());

        // Create the help context id for the viewer's control
        PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "com.genettasoft.warningsystem.ui.viewer");
        makeActions();
        hookContextMenu();
        contributeToActionBars();

        //Create the ConsensusSection at the bottom of View
        Composite consensusComposite=new Composite(parent,SWT.BORDER);
        GridData gridData2 = new GridData(GridData.FILL, GridData.END, true, false);
        gridData2.heightHint=50;
        consensusComposite.setLayoutData(gridData2);
        GridLayout gridLayout2 = new GridLayout(2, false);
        gridLayout2.marginLeft=0;
        gridLayout2.marginWidth=0;
        gridLayout2.marginBottom=10;
        gridLayout2.marginTop=0;
        gridLayout2.marginRight=0;
        gridLayout2.marginHeight=0;
        consensusComposite.setLayout(gridLayout2);

        //Create components of consensusComposite
        consensusText=new Text(consensusComposite,SWT.BORDER);
        GridData gridData3 = new GridData(GridData.FILL, GridData.BEGINNING, true, true);
        gridData3.heightHint=40;
        consensusText.setLayoutData(gridData3);
        
        //Initialize and cache consensus images
        questImg= Activator.getImageDecriptor( "icons48/question.png" ).createImage();
        warnImg= Activator.getImageDecriptor( "icons48/warn.png" ).createImage();
        crossImg= Activator.getImageDecriptor( "icons48/cross.png" ).createImage();
        checkImg= Activator.getImageDecriptor( "icons48/check.png" ).createImage();
        wheelImg= Activator.getImageDecriptor( "icons48/wheel.png" ).createImage();

        //Start off with a question-image
        consensusImage=questImg;
        consensusCanvas = new Canvas(consensusComposite,SWT.NO_REDRAW_RESIZE);
        consensusCanvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
             e.gc.drawImage(consensusImage,0,1);
            }
        });
        
        GridData gridData4 = new GridData(GridData.END, GridData.BEGINNING, false, false);
        gridData4.widthHint=50;
        gridData4.heightHint=50;
        gridData4.minimumHeight=50;
        gridData4.minimumWidth=50;
        consensusCanvas.setLayoutData(gridData4);

        updateConsensusView();
        
        //Initialize instance variables
        editorTestMap=new HashMap<IWorkbenchPart, List<TestRun>>();
        editorListenerMap=new HashMap<IWorkbenchPart, IPropertyChangeListener>();
        runningJobs=new ArrayList<BioclipseJob<List<ITestResult>>>();
        
        //Turn off autorun by default
        setAutorun( false );
        updateActionStates();

        
        //Listen for part lifecycle events to react on editors
        getSite().getWorkbenchWindow().getPartService().addPartListener(this);
        
        getSite().setSelectionProvider(viewer);
        
        //If editor is open, react on it
        if (getSite()==null) return;
        if (getSite().getWorkbenchWindow()==null) return;
        if (getSite().getWorkbenchWindow().getActivePage()==null) return;

        IEditorPart openEditor = getSite().getWorkbenchWindow()
                             .getActivePage().getActiveEditor();
        if (openEditor!=null){
            partActivated( openEditor );
            if (isAutorun()){
//                doRunAllTests();
            }
        }
        
        Job job=new Job("Initializing decision support tests"){
            @Override
            protected IStatus run( IProgressMonitor monitor ) {

                IDSManager ds = Activator.getDefault().getJavaManager();
                try {
                    monitor.beginTask( "Initializing decision support tests", ds.getTests().size()+1 );
                    monitor.worked( 1 );
                    for (String testID : ds.getTests()){
                        IDSTest test = ds.getTest( testID );
                        monitor.subTask( "Initializing test: " + testID );
                        test.initialize( monitor );
                    }
                } catch ( BioclipseException e1 ) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        "All tests could not be initalized: " + e1.getMessage());
                } catch ( DSException e ) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    "All tests could not be initalized: " + e.getMessage());
                }
                
                monitor.done();
                return Status.OK_STATUS;
            }
            
        };
        job.setUser( false );
//        job.schedule();
        
    }

    private void updateConsensusView() {

        if (activeTestRuns==null){
            consensusText.setText( "Not run"); 
            consensusImage=questImg;
            consensusCanvas.update();
            consensusCanvas.redraw();
            return;
        }
        
        boolean running=false;
        boolean notStarted=false;

        //If we have any tests running or not started, do not do consensus
        for (TestRun tr : activeTestRuns){
            if (tr.getStatus()==TestRun.RUNNING)
                running=true;
            else if (tr.getStatus()==TestRun.NOT_STARTED)
                notStarted=true;
        }

        if(notStarted){
            consensusText.setText( "Tests not started"); 
            consensusImage=questImg;
        }
        else if (running){
            consensusText.setText( "Tests running..."); 
            consensusImage=wheelImg;
        }
        else{
            int res=getConsensusFromTestRuns();
            if (res==ITestResult.POSITIVE){
                consensusText.setText( "Consensus: POSITIVE"); 
                consensusImage=crossImg;
            }
            else if (res==ITestResult.NEGATIVE){
                consensusText.setText( "Consensus: NEGATIVE"); 
                consensusImage=checkImg;
            }
            else if (res==ITestResult.INCONCLUSIVE){
                consensusText.setText( "Consensus: INCONCLUSIVE"); 
                consensusImage=warnImg;
            }
        }
        
        consensusCanvas.update();
        consensusCanvas.redraw();
        
    }

    
    /**
     * A simple consensus voting.
     * TODO: Implement custom solutions for this.
     * @return
     */
    private int getConsensusFromTestRuns() {

        int numpos=0;
        int numneg=0;
        int numinc=0;

        if (activeTestRuns==null)
            return ITestResult.INCONCLUSIVE;
            
        for (TestRun tr : activeTestRuns){
            //Only count non-informative and included testruns
            if ((!(tr.getTest().isInformative())) 
                    &&  (!(tr.getTest().isExcluded()))){
                
                if (tr.getStatus()==TestRun.FINISHED){
                    if (tr.getConsensusStatus()==ITestResult.POSITIVE)
                        numpos++;
                    else if (tr.getConsensusStatus()==ITestResult.NEGATIVE)
                        numneg++;
                    else if (tr.getConsensusStatus()==ITestResult.INCONCLUSIVE)
                        numinc++;
                }

            }
        }

        //If no positive results:
        if (numpos==0)
            return ITestResult.NEGATIVE;

        //If at least one but equal:
        else if (numpos==numneg)
            return ITestResult.INCONCLUSIVE;

        //If at least one but more pos than neg:
        else if (numpos>numneg)
            return ITestResult.POSITIVE;

        //In all other cases:
        else
            return ITestResult.NEGATIVE;
        
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
                DSView.this.fillContextMenu(manager);
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
        manager.add(autoRunAction);
        manager.add(runAction);
        manager.add(clearAction);
        manager.add(new Separator());
        manager.add(includeAction);
        manager.add(excludeAction);
        manager.add(new Separator());
        manager.add(expandAllAction);
        manager.add(collapseAllAction);
        manager.add(new Separator());
        manager.add(helpAction);
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }
    
    private void updateActionStates() {

        if (activeTestRuns!=null && activeTestRuns.size()>0)
            runAction.setEnabled( true );
        else
            runAction.setEnabled( false );
        
        if (isAutorun()){
            autoRunAction.setImageDescriptor( Activator.getImageDecriptor( "icons/fastforward_dis2.png" ));
        }else{
            autoRunAction.setImageDescriptor(Activator.getImageDecriptor( "icons/fastforward.png" ));
        } 
        
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
        runAction.setImageDescriptor(Activator.getImageDecriptor( "icons/smallRun.gif" ));
        runAction.setDisabledImageDescriptor( Activator.getImageDecriptor( "icons/smallRun_dis.gif" ));

        autoRunAction = new Action() {
            public void run() {
                setAutorun( !autorun);
                updateActionStates();
                if (autorun){
//                    setAutoRunPropListener();
                    doRunAllTests();
                }else{
//                    clearAutoRunPropListener();
                }
            }
        };
        autoRunAction.setText("Toggle AutoTest");
        autoRunAction.setToolTipText("Turns on/off automatic running of tests on structural changes");
        autoRunAction.setImageDescriptor(Activator.getImageDecriptor( "icons/fastforward.png" ));
        autoRunAction.setDisabledImageDescriptor( Activator.getImageDecriptor( "icons/fastforward_dis2.png" ));

        clearAction = new Action() {
            public void run() {
                doClearAllTests();
            }
        };
        clearAction.setText("Clear all Tests");
        clearAction.setToolTipText("Clear all active tests");
        clearAction.setImageDescriptor(Activator.getImageDecriptor( "icons/broom.png" ));

        excludeAction = new Action() {
            public void run() {
                doExcludeSelectedTests();
            }
        };
        excludeAction.setText("Exclude test");
        excludeAction.setToolTipText("Exclude selected test(s)");
        excludeAction.setImageDescriptor(Activator.getImageDecriptor( "icons/item_delete.gif" ));
        excludeAction.setDisabledImageDescriptor(Activator.getImageDecriptor( "icons/item_delete_dis.gif" ));

        includeAction = new Action() {
            public void run() {
                doIncludeSelectedTests();
            }
        };
        includeAction.setText("Include test");
        includeAction.setToolTipText("Include selected test(s)");
        includeAction.setImageDescriptor(Activator.getImageDecriptor( "icons/item_add.gif" ));
        includeAction.setDisabledImageDescriptor(Activator.getImageDecriptor( "icons/item_add_dis.gif" ));

        
        collapseAllAction = new Action() {
            public void run() {
                viewer.collapseAll();
            }
        };
        collapseAllAction.setText("Collapse all");
        collapseAllAction.setToolTipText("Collapse all tests");
        collapseAllAction.setImageDescriptor(Activator.getImageDecriptor( "icons/collapseall.gif" ));

        expandAllAction = new Action() {
            public void run() {
                viewer.expandAll();
            }
        };
        expandAllAction.setText("Expand all");
        expandAllAction.setToolTipText("Expand all tests to reveal hits");
        expandAllAction.setImageDescriptor(Activator.getImageDecriptor( "icons/expandall.gif" ));

        refreshAction = new Action() {
            public void run() {
                viewer.refresh();
            }
        };
        refreshAction.setText("Refresh");
        refreshAction.setToolTipText("Force a refresh of all tests' status");
        refreshAction.setImageDescriptor(Activator.getImageDecriptor( "icons/refresh2.png" ));

        helpAction = new Action() {
            public void run() {
                IWorkbenchHelpSystem helpSystem = PlatformUI.getWorkbench()
                                                            .getHelpSystem();
                helpSystem.displayHelpResource("/" + Activator.PLUGIN_ID +
                                               "/html/maintopic.html");
            }
        };
        helpAction.setText("Help");
        helpAction.setToolTipText("Open help for teh Decision Support");
        helpAction.setImageDescriptor(Activator.getImageDecriptor( "icons/help.gif" ));

    }
    

    protected void doIncludeSelectedTests() {

        IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
        for (Object obj : sel.toList()){
            if ( obj instanceof IDSTest ) {
                IDSTest dstest = (IDSTest) obj;
                dstest.setExcluded( false );
                viewer.refresh(dstest);
            }
            else if ( obj instanceof TestRun ) {
                TestRun testrun = (TestRun) obj;
                testrun.setStatus( TestRun.NOT_STARTED);
                testrun.getTest().setExcluded( false );
                viewer.refresh(testrun);
            }
        }
        
    }

    protected void doExcludeSelectedTests() {

        IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
        for (Object obj : sel.toList()){
            if ( obj instanceof IDSTest ) {
                IDSTest dstest = (IDSTest) obj;
                dstest.setExcluded( true );
                viewer.refresh(dstest);
            }
            else if ( obj instanceof TestRun ) {
                TestRun testrun = (TestRun) obj;
                testrun.setStatus( TestRun.EXCLUDED);
                testrun.getTest().setExcluded( true );
                if (testrun.getMatches()!=null)
                    testrun.getMatches().clear();
                viewer.refresh(testrun);
            }
        }
        
    }

    protected void doClearAllTests() {

        IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                            .getActivePage().getActiveEditor();
        if ( editor instanceof JChemPaintEditor ) {
            JChemPaintEditor jcp = (JChemPaintEditor) editor;
            doClearAllTests( jcp );
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

        //Store active selection
        storedSelection=(IStructuredSelection) viewer.getSelection();

        //Get the molecule from the editor
        //Assumption: All testruns operate on the same molecule
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

        //Wait for all running jobs to cancel
        for (BioclipseJob<List<ITestResult>> job : runningJobs){
            logger.debug("Waiting for Job: " + job.getName() + " to finish...");
            try {
                job.join();
            } catch ( InterruptedException e ) {
            }
            logger.debug("Job: " + job.getName() + " finished.");
        }

        //We need to clear previous tests if already run
        if (isExecuted()==true){
            doClearAllTests( jcp );
            activeTestRuns=editorTestMap.get( part );
//            partActivated( jcp );
            viewer.refresh();
        }
        setExecuted( true );

        IDSManager ds=Activator.getDefault().getJavaManager();
        for (final TestRun tr : activeTestRuns){

            if (tr.getTest().getTestErrorMessage().length()<1){

                if (tr.getStatus()==TestRun.EXCLUDED || tr.getTest().isExcluded()){
                    viewer.refresh(tr);
                    logger.debug( "===== Test: " + tr + " skipped since excluded.");
                }
                else{
                    
                    logger.debug( "===== Testrun: " + tr + " started" );
                    tr.setStatus( TestRun.RUNNING );
                    tr.setMolecule( mol );
                    viewer.refresh(tr);
                    viewer.setExpandedState( tr, true );

                    runTestAsJobs( mol, ds, tr ); 
                }
            }
            else{
                logger.debug("The test: " + tr.getTest() + " has an error so not run.");
            }
        }

        logger.debug( "===== All testruns started" );
    }

    private void runTestAsJobs( final ICDKMolecule mol, IDSManager ds, final TestRun tr ) {

            try {

                
                //Start up a job with the test
                BioclipseJob<List<ITestResult>> job = 
                    ds.runTest( tr.getTest().getId(), mol, 
                    new BioclipseJobUpdateHook<List<ITestResult>>(tr.getTest().getName()));
                
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

                            for (ITestResult result : matches){
                                    result.setTestRun( tr );
                                    tr.addResult(result);
                            } 
                            tr.setMatches( matches );
                            logger.debug( "===== " + tr + " finished" );
                            if (tr.getTest().getTestErrorMessage()!="")
                                tr.setStatus( TestRun.ERROR );
                            else
                                tr.setStatus( TestRun.FINISHED );
                            
                            viewer.refresh( tr );
                            viewer.setExpandedState( tr, true );
                            updateConsensusView();
                            
                            //If we previously stored a selection, set it now
                            selectIfStoredSelection(tr);
                            
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
                
                viewer.refresh( tr );
                updateConsensusView();

            }

    }


    private void selectIfStoredSelection(TestRun tr) {

        if (storedSelection==null) return;
        
        for (Object obj : storedSelection.toList()){
            //Select this testrun if same id as stored one
            if ( obj instanceof TestRun ) {
                TestRun storedtr = (TestRun) obj;
                if (storedtr.getTest().getId().equals( tr.getTest().getId() )){
                    viewer.setSelection( new StructuredSelection(tr) );
                }
            }
            //If this testrun has no matches, do not try to select them
            else if (tr.getMatches()==null || tr.getMatches().size()<=0)
                return;
            //Select the first ITestResult if it has same TestRun.Test as stored
            else if ( obj instanceof ITestResult ) {
                ITestResult storedtres=(ITestResult)obj;
                if (storedtres.getTestRun().getTest().getId().equals( tr.getTest().getId() )){
//                    viewer.setSelection( new StructuredSelection(tr.getMatches().get( 0 )) );
                    //TODO: fire explicit property to make this highlight?
                }
            }
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

    private IWorkbenchPart getSupportedEditor( IWorkbenchPart part ) {
        if ( part instanceof JChemPaintEditor ) {
            System.out.println("We have a JCP editor for TestsView!");
            return part;
        }
        else if ( part instanceof MoleculesEditor ) {
            //TODO: when does this happen?
            return part;
        }
        else if ( part instanceof MultiPageMoleculesEditorPart ) {
            System.out.println("We have a MPE editor for TestsView");
            MultiPageMoleculesEditorPart editor = (MultiPageMoleculesEditorPart)part;
            Object obj = editor.getAdapter(JChemPaintEditor.class);
            if (obj== null){
                System.out.println("     MPE editor for TestsView did not have JCP page to provide");
                return null;
            }
            System.out.println("     MPE editor for TestsView provided JCP page!");
            JChemPaintEditor jcp=(JChemPaintEditor)obj;
            return jcp;
        }

        System.out.println("No supported editor for TestsView");

        //Not supported editor
        return null;
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
        }else{
         //ok, we have nothing.
            viewer.setInput(TestHelper.readTestsFromEP().toArray());
        }
        updateActionStates();
        viewer.expandAll();
        
        //Also update consensus part
        updateConsensusView();
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

            //First, try to look up in map
            IPropertyChangeListener jcplistener = editorListenerMap.get( jcp);

            //If not found in map, create a new
            if (jcplistener==null){

                jcplistener = new IPropertyChangeListener() {
                    public void propertyChange( PropertyChangeEvent event ) {

                        if(event.getProperty().equals( JChemPaintEditor.
                                                       STRUCUTRE_CHANGED_EVENT )) {

                            // editor model has changed
                            // do stuff...
                            logger.debug
                            ("TestsView reacting: JCP editor model has changed");

                            //                        storedSelection=(IStructuredSelection) viewer.getSelection();

                            doClearAllTests( jcp );
                            if (isAutorun())
                                doRunAllTests();

                        }
                    }
                };

                jcp.addPropertyChangedListener(jcplistener);
                editorListenerMap.put( jcp, jcplistener );
            }
            
            doClearAllTests( jcp );

            return;
        }

        else if ( part instanceof MoleculesEditor ) {
//            MoleculesEditor moleditor = (MoleculesEditor) part;

            showMessage( "MOLTABLE NOT YET SUPPORTED!" );
            logger.debug("MOLTABLE NOT YET SUPPORTED!");

            return;
        }

    }

    private void doClearAllTests( JChemPaintEditor jcp ) {

        List<TestRun> newTestRuns=new ArrayList<TestRun>();

        IDSManager ds = Activator.getDefault().getJavaManager();
        
        try {
            for (String testid : Activator.getDefault().getJavaManager().getTests()){
                IDSTest test = ds.getTest( testid );
                TestRun newTestRun=new TestRun(jcp,test);
                if (test.getTestErrorMessage()!=null 
                        && test.getTestErrorMessage().length()>0){
                    newTestRun.setStatus( TestRun.ERROR );
                }
                else if (test.isExcluded()){
                    newTestRun.setStatus( TestRun.EXCLUDED );
                }

                newTestRuns.add( newTestRun );
            }
        } catch ( BioclipseException e ) {
            e.printStackTrace();
        }
        
        editorTestMap.put( jcp, newTestRuns );
        activeTestRuns=newTestRuns;
        setExecuted( false );
        updateActionStates();
        updateView();
    }
    
    public DSSingleReportModel waitAndReturnReportModel(){
        
        //Wait for all jobs to finish
        for (BioclipseJob<List<ITestResult>> job : runningJobs){
            logger.debug("Waiting for Job: " + job.getName() + " to finish...");
            try {
                job.join();
            } catch ( InterruptedException e ) {
            }
            logger.debug("Job: " + job.getName() + " finished.");
        }
        
        if (activeTestRuns==null || activeTestRuns.size()<=0){
            logger.error( "No active testruns to make a chart from." );
            return null;
        }

        //Set up and return ReportModel
        DSSingleReportModel reportmodel=new DSSingleReportModel();

        //Get mol from first testrun
        TestRun first = activeTestRuns.get( 0 );
        ICDKMolecule mol = first.getMolecule();

        //Get name
        String name = (String) mol.getAtomContainer().getProperty( CDKConstants.TITLE );
        if (name==null)
            name="N/A";
        reportmodel.setCompoundName( name );
        
        ICDKManager cdk = net.bioclipse.cdk.business.Activator
                                              .getDefault().getJavaCDKManager();
        try {
            //Generate SMILES
            String smi=cdk.calculateSMILES( mol );
            reportmodel.setSMILES( smi );

            //Generate Mass
            double mw=cdk.calculateMass( mol );
            reportmodel.setMw( mw );

            //Generate structure image
            byte[] structureImage = ReportHelper.createImage(mol, null, 200,200);
            reportmodel.setQueryStructure( structureImage );
        } catch ( BioclipseException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
        //Add all testmodels to ReportModel
        for (TestRun tr : activeTestRuns){
            AbstractTestReportModel testreportmodel=tr.getTest().getReportmodel();
            if (testreportmodel!=null){
                testreportmodel.setName( tr.getTest().getId() );
                if (testreportmodel!=null){
                    testreportmodel.setTestrun( tr );
                    reportmodel.addTestModel( testreportmodel );
                }
            }
        }
        
        return reportmodel;
        
    }

    
    /* ================================
     * Below is for part lifecycle events
     *====================================  */

    /**
     * 
     */
    public void partActivated( IWorkbenchPart part ) {
      logger.debug("Part:" + part.getTitle() + " activated");
        if (!( part instanceof IEditorPart )) return;
        
        if ( part instanceof IDSViewNoCloseEditor ) {
            //Do not react on this; no clear, no new tests
            return;
        }
        
        IWorkbenchPart ppart=getSupportedEditor( part );
        if (ppart==null){
            activeTestRuns=null;
            updateView();
            return;
        }

        if (editorTestMap.keySet().contains( ppart )){
            activeTestRuns=editorTestMap.get( ppart );
        }else {
            addNewTestRuns(ppart);
        }
        updateView();

        if (activeTestRuns!=null){
            for (TestRun tr : activeTestRuns){
                selectIfStoredSelection( tr );
            }
        }
    }



    public void partBroughtToTop( IWorkbenchPart part ) {
//        logger.debug("Part:" + part.getTitle() + " brought to top");
        if (!( part instanceof IEditorPart )) return;

        if ( part instanceof IDSViewNoCloseEditor ) {
            //Do not react on this; no clear, no new tests
            return;
        }

        IWorkbenchPart ppart=getSupportedEditor( part );
        if (ppart==null) return;

        if (editorTestMap.keySet().contains( ppart )){
            activeTestRuns=editorTestMap.get( ppart );
        }else {
            addNewTestRuns(ppart);
        }
        updateView();
    }


    public void partClosed( IWorkbenchPart part ) {
//      logger.debug("Part:" + part.getTitle() + " closed");
        if (!( part instanceof IEditorPart )) return;

        
        IWorkbenchPart ppart=getSupportedEditor( part );
        if (ppart==null) return;

        editorListenerMap.remove( part );
        if (editorTestMap.keySet().contains( ppart )){
            editorTestMap.remove( ppart );
            updateView();
        }
    }

    /**
     * Occurs when lost focus. Does not mean it is not topmost editor, but 
     * could mean an unsupported editor is topmost. Need to verify this and 
     * clean in that case.
     */
    public void partDeactivated( IWorkbenchPart part ) {
        logger.debug("Part:" + part.getTitle() + " deactivated");
        storedSelection=(IStructuredSelection) viewer.getSelection();
    }

    public void partOpened( IWorkbenchPart part ) {
//        logger.debug("Part:" + part.getTitle() + " opened");
    }

    public void setAutorun( boolean autorun ) {

        this.autorun = autorun;
    }

    public boolean isAutorun() {

        return autorun;
    }

    

}
