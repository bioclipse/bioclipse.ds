package net.bioclipse.ds.ui.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculesEditor;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.Activator;
import net.bioclipse.ds.model.ISubstructureMatch;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.TestHelper;
import net.bioclipse.ds.model.TestRun;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

public class TestsView extends ViewPart implements IPartListener, ISelectionChangedListener{

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
        
        //Listen to selections in the viewer
        viewer.addSelectionChangedListener( this );

        // Create the help context id for the viewer's control
        PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "com.genettasoft.warningsystem.ui.viewer");
        makeActions();
        hookContextMenu();
        contributeToActionBars();

        editorTestMap=new HashMap<IWorkbenchPart, List<TestRun>>();
        
        //Listen for part lifecycle events to react on editors
        getSite().getWorkbenchWindow().getPartService().addPartListener(this);
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
                
                System.out.println("Running tests...");
                
                if (activeTestRuns==null || activeTestRuns.size()<=0){
                    showMessage( "No active testruns to run" );
                    return;
                }
                
                TestHelper.runTests(activeTestRuns);
                viewer.refresh();

                System.out.println("Running tests completed.");

            }
        };
        runAction.setText("Run Tests");
        runAction.setToolTipText("Runs the WarningTests on the active content");
        runAction.setImageDescriptor(Activator.getImageDecriptor( "icons/testrun.gif" ));
    }

    private void showMessage(String message) {
        MessageDialog.openInformation(
                                      viewer.getControl().getShell(),
                                      "Warning Test",
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
        
        if (activeTestRuns!=null && activeTestRuns.size()>0){
            viewer.setInput( activeTestRuns.toArray() );
            viewer.refresh();
        }else{
            
        }

        System.out.println("Should update view here. NOT IMPL.");
        
    }

    /**
     * We have a new editor. Create a new TestRun for the molecules it contains 
     * and add to local model map.
     * @param part 
     */
    private void addNewTestRuns(IWorkbenchPart part) {

        List<TestRun> newTestRuns=new ArrayList<TestRun>();
        
        if ( part instanceof JChemPaintEditor ) {
            JChemPaintEditor jcp = (JChemPaintEditor) part;
            
            //JCP contains only one mol
            IMolecule mol = jcp.getCDKMolecule();
            
            for (IDSTest test : TestHelper.readTestsFromEP()){
                TestRun newTestRun=new TestRun(mol,test);
                newTestRuns.add( newTestRun );
            }
            
            editorTestMap.put( part, newTestRuns );

            return;
        }

        else if ( part instanceof MoleculesEditor ) {
//            MoleculesEditor moleditor = (MoleculesEditor) part;

            showMessage( "MOLTABLE NOT YET SUPPORTED!" );
            System.out.println("MOLTABLE NOT YET SUPPORTED!");

            return;
        }

    }

    
    /* ================================
     * Below is for part lifecycle events
     *====================================  */

    /**
     * 
     */
    public void partActivated( IWorkbenchPart part ) {
        if (!(isSupportedEditor(part))) return;
        System.out.println("Part:" + part.getTitle() + " activated");

        if (editorTestMap.keySet().contains( part )){
            activeTestRuns=editorTestMap.get( part );
        }else {
            addNewTestRuns(part);
        }
        updateView();
    }


    public void partBroughtToTop( IWorkbenchPart part ) {
        if (!(isSupportedEditor(part))) return;
        System.out.println("Part:" + part.getTitle() + " brought to top");
        
        if (editorTestMap.keySet().contains( part )){
            activeTestRuns=editorTestMap.get( part );
        }else {
            addNewTestRuns(part);
        }
        updateView();
    }


    public void partClosed( IWorkbenchPart part ) {
        if (!(isSupportedEditor(part))) return;
        System.out.println("Part:" + part.getTitle() + " closed");
        
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
        System.out.println("Part:" + part.getTitle() + " deactivated");
    }

    public void partOpened( IWorkbenchPart part ) {
        if (!(isSupportedEditor(part))) return;
        System.out.println("Part:" + part.getTitle() + " opened");
    }

    
    public void selectionChanged( SelectionChangedEvent event ) {

        //Verify JCP is active part
        IEditorPart part=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (!( part instanceof JChemPaintEditor )) return;
        JChemPaintEditor jcp=(JChemPaintEditor)part;

        //Check selection is not Text
        if (!( event.getSelection() instanceof IStructuredSelection )){
            colorAtomsInJCP( jcp, new ArrayList<Integer>() );
            return;
        }

        //We are interested in collecting atoms to highlight
        List<Integer> toHighlight=new ArrayList<Integer>();
        
        IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
        for (Object obj : ssel.toList()){
            if ( obj instanceof ISubstructureMatch) {
                ISubstructureMatch match=(ISubstructureMatch) obj;
                toHighlight.addAll( match.getMatchingAtoms() );
            }
            else if ( obj instanceof TestRun ) {
                //Add all matches if a testrun is clicked
                TestRun run = (TestRun)obj;
                if (run.getMatches()!=null){
                    for (ITestResult match : run.getMatches()){
                        if ( match instanceof ISubstructureMatch ) {
                            ISubstructureMatch submatch = (ISubstructureMatch) match;
                            toHighlight.addAll( submatch.getMatchingAtoms() );
                        }
                    }
                }
            }
        }

        if (toHighlight.isEmpty()){
            colorAtomsInJCP( jcp, new ArrayList<Integer>() );
            return;
        }

        colorAtomsInJCP(jcp, toHighlight);
        
    }
    
    private void colorAtomsInJCP( JChemPaintEditor jcp, List<Integer> atomIndices ) {

        //TODO: implement
     
    }

}