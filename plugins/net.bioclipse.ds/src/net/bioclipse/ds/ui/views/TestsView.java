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
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.TestHelper;
import net.bioclipse.ds.model.TestRun;
import net.bioclipse.ds.model.impl.DSException;
import net.bioclipse.jobs.BioclipseUIJob;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
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

    /**
     * The constructor.
     */
    public TestsView() {
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

        //Init with available tests
        viewer.setInput(TestHelper.readTestsFromEP().toArray());

        // Create the help context id for the viewer's control
        PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "com.genettasoft.warningsystem.ui.viewer");
        makeActions();
        hookContextMenu();
        contributeToActionBars();

        editorTestMap=new HashMap<IWorkbenchPart, List<TestRun>>();
        
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
        manager.add(new Separator());
//        drillDownAdapter.addNavigationActions(manager);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(runAction);
        manager.add(new Separator());
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void makeActions() {
        runAction = new Action() {
            public void run() {
                
                logger.debug("Running tests...");
                
                if (activeTestRuns==null || activeTestRuns.size()<=0){
                    showMessage( "No active testruns to run" );
                    return;
                }

                //Get the molecule from the editor
                //Asumption: All testruns operate on the same molecule
                IEditorPart part=activeTestRuns.get( 0 ).getEditor();
                
                ICDKMolecule mol=null;
                if ( part instanceof JChemPaintEditor ) {
                    JChemPaintEditor jcp = (JChemPaintEditor) part;
                    mol=jcp.getCDKMolecule();
                }else{
                    showError("The editor: " + part + " is not " +
                        "supported to run DS tests on.");
                    return;
                }

                
//                try {

                IDSManager ds=Activator.getDefault().getJavaManager();
                for (final TestRun tr : activeTestRuns){
                    
                    logger.debug( "===== Testrun: " + tr + " started" );
                    
                    try {
                        ds.runTest( tr.getTest().getId(), mol, new BioclipseUIJob<List<ITestResult>>(){

                            @Override
                            public void runInUI() {
                                List<ITestResult> matches = getReturnValue();

                                for (ITestResult match : matches){
                                    match.setTestRun( tr );
                                } 
                                //FIXME: adapt for ErrorResult herec and in UI
                                tr.setMatches( matches );
                                tr.setRun( true );
                                
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

                logger.debug( "===== All testruns started" );

                    
//                    TestHelper.runTests(activeTestRuns);
//                } catch ( BioclipseException e ) {
//                    logger.error( "TEST failed: " + e.getMessage() );
//                    showError("TEST failed: " + e.getMessage());
//                }
//                viewer.refresh();

                logger.debug("Running tests completed.");

            }
        };
        runAction.setText("Run Tests");
        runAction.setToolTipText("Runs the WarningTests on the active content");
        runAction.setImageDescriptor(Activator.getImageDecriptor( "icons/testrun.gif" ));
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

//        logger.debug("Should update view here. NOT IMPL.");
        
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        editorTestMap.put( jcp, newTestRuns );
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
