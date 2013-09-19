/* *****************************************************************************
 * Copyright (c) 2009-2010 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.ui.views;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList; 
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.CDKMoleculeUtils;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.business.IJChemPaintManager;
import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.cdk.ui.sdfeditor.editor.MultiPageMoleculesEditorPart;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.PropertyViewHelper;
import net.bioclipse.ds.ui.Activator;
import net.bioclipse.ds.business.IDSManager;
import net.bioclipse.ds.model.Endpoint;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.TestRun;
import net.bioclipse.ds.model.result.AtomResultMatch;
import net.bioclipse.ds.model.result.SimpleResult;
import net.bioclipse.ds.report.DSSingleReportModel;
import net.bioclipse.ds.ui.DSContextProvider;
import net.bioclipse.ds.ui.GeneratorHelper;
import net.bioclipse.ds.ui.VotingConsensus;
import net.bioclipse.jobs.BioclipseJob;
import net.bioclipse.jobs.BioclipseJobUpdateHook;
import net.bioclipse.nm.business.INmManager;
import net.bioclipse.nm.domain.Material;
import net.bioclipse.nm.ui.editors.NMEditor;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.part.*;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;

import quicktime.std.movies.AtomContainer;

/**
 * 
 * @author ola
 *
 */
public class DSView extends ViewPart implements IPartListener2, IPropertyChangeListener {

    private static final Logger logger = Logger.getLogger(DSView.class);

    public static final String VIEW_ID="net.bioclipse.ds.ui.views.DSView";
    
    //Set to true in order to always collapse properties view after a new 
    //DSView selection event
    public static final boolean COLLAPSE_PROPERTIES_VIEW = false;
       
    private static Image questImg;
    private static Image warnImg;
    private static Image crossImg;
    private static Image checkImg;
    private static Image wheelImg;
    private static Image equalImg;

    
    private TreeViewer viewer;
    private Action runAction;


    //Kepp track of existing mappings from editor to TestRun
    private Map<ICDKMolecule, List<TestRun>> molTestMap;

    //Kepp track of existing mappings from molecule to TestRun
    private Map<ICDKMolecule, IPropertyChangeListener> molListenerMap;

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

    private String selectedProperty;

    private Action autoRunAction;
    
    private boolean autorun;

    //The currently shown image in consensusView 
    private Image consensusImage;

    private Text consensusText;

    private Canvas consensusCanvas;

    private Action helpAction;

    private IContextProvider contextProvider;

	private JChemPaintEditor lastJCP;

	private Action filterOutErrorAction;
	private Action filterOutEmptyAction;
	private Action installModelsAction;
	
    private static DSView instance;
    
    /**
     * The constructor.
     */
    public DSView() {
    }

    /*
     * Getters and setters
     */

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

    public void setAutorun( boolean autorun ) {
        this.autorun = autorun;
    }
    public boolean isAutorun() {
        return autorun;
    }

    /*
     * Methods
     */

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
        ColumnViewerToolTipSupport.enableFor(viewer);

        viewer.setContentProvider(new DSViewContentProvider());
//        viewer.setLabelProvider(new DecoratingLabelProvider(new DSViewLabelProvider(),new DSViewDecorator()));
        viewer.setLabelProvider(new DSViewLabelProvider());
        viewer.setComparator(new DSViewerComparator());
        viewer.addFilter( new HideNotVisbleFilter() );
        viewer.addSelectionChangedListener( new ISelectionChangedListener(){

            public void selectionChanged( SelectionChangedEvent event ) {

            	updateActionStates();
                Object obj = ((IStructuredSelection)event.getSelection()).getFirstElement();

                //Add the case for clicking a string with link to open models UI 
                if (obj instanceof String) {
                	String str = (String)obj;
                	if (str.startsWith("link:Install models")){
                		installModelsAction.run();
                	}
										
				}

                JChemPaintEditor jcp=getJCPfromActiveEditor();
                if (jcp==null) return;

                //Turn off all ext generators who has a boolean parameter
                //Not the best, but nothing else to do currently
                //TODO: Improve on this!
                GeneratorHelper.turnOffAllExternalGenerators(jcp);

                //Collapse propertyview after some ms of waiting
                if(System.getProperty("DS_COLLAPSE","false").equalsIgnoreCase("true")){
                	Display.getDefault().timerExec(900, new Runnable() {
                		@Override
                		public void run() {
                			PropertyViewHelper.collapseAll();
                		}
                	});
                }

                if ( obj instanceof ITestResult ) {
                	ITestResult tr = (ITestResult) obj;

                    Class<? extends IGeneratorParameter<Boolean>> visibilityParam = tr.getGeneratorVisibility();
                    Class<? extends IGeneratorParameter<Map<Integer, Number>>> atomMapParam = tr.getGeneratorAtomMap();
                    
                    if (visibilityParam==null){
                    	logger.debug("The selected TestResult does not provide a generatorVisibility.");
                    	return;
                    }
                    

                    RendererModel model = jcp.getWidget().getRenderer2DModel();

                    //And turn only the selected on
                    model.set(visibilityParam, true);
					logger.debug("Turned on Generator: " + tr.getGeneratorVisibility());

					if (atomMapParam!=null){
						if (tr instanceof AtomResultMatch) {
							AtomResultMatch atomResMatch = (AtomResultMatch) tr;
		                    model.set(atomMapParam, atomResMatch.getResultMap());
	    					logger.debug("  ...and AtomMapGeneratorParameter is used with content. Should now be displayed.");
						}else{
	    					logger.debug("  ...however, an AtomMapGeneratorParameter is available but TestResult is not AtomResultMatch.");
						}
						
					}
					else{
    					logger.debug("  ...however, no AtomMapGeneratorParameter is available.");
                    }
                    
                    
                }
            }

        });


        GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
        viewer.getTree().setLayoutData(gridData);
        
        viewer.setInput(new String[]{"Initializing..."});

        // Create the help context id for the viewer's control
        PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), VIEW_ID);
        makeActions();
        hookContextMenu();
        contributeToActionBars();

        /*
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
        equalImg= Activator.getImageDecriptor( "icons48/equal.png" ).createImage();

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
        */

        //Initialize instance variables
        molTestMap=new HashMap<ICDKMolecule, List<TestRun>>();
		molListenerMap=new HashMap<ICDKMolecule, IPropertyChangeListener>();
        runningJobs=new ArrayList<BioclipseJob<List<ITestResult>>>();
        
        //Turn off autorun by default
        setAutorun( false );
        updateActionStates();


        Job job=new Job("Initializing decision support tests"){
            @Override
            protected IStatus run( IProgressMonitor monitor ) {

                IDSManager ds = net.bioclipse.ds.Activator.getDefault().getJavaManager();

                try {
					monitor.beginTask( "Initializing decision support tests", ds.getTests().size()+1 );
                monitor.worked( 1 );
                for (String testID : ds.getTests()){
                	IDSTest test = ds.getTest( testID );
                	monitor.subTask( "Initializing test: " + testID );
                	logger.debug( "Initializing test: " + testID );
                	try {
                		if (!test.isInitialized()){
                			test.initialize( monitor );

                			//If no exception, assume all is well
                			test.setInitialized(true);
                		}
                	} catch (Exception e) {
                		String ermsg=e.toString();
                		if (e.getMessage()!=null)
                			ermsg=e.getMessage();
                		
                		logger.error("Failed initializing test " + 
                				test.getName() + " Reason: " + ermsg);
                		test.setTestErrorMessage("Error: "+ermsg);
                		
                		LogUtils.debugTrace(logger, e);
                	}
                }

                    Display.getDefault().asyncExec( new Runnable(){

                        public void run() {

                            //Init viewer with available endpoints
                            IDSManager ds = net.bioclipse.ds.Activator.getDefault().getJavaManager();
                            try {
                            	if (ds.getFullEndpoints().size()>1) //There is always the uncategorized EP...
                            		viewer.setInput(ds.getFullEndpoints().toArray());
                            	else{
                            		String[] msg = new String[2];
                            		msg[0]="   No models available.";
                            		msg[1]="link:Install models...";// from menu: 'Install > DS Models...'";
//                            		viewer.setSorter(null);
                            		viewer.setInput(msg);
                            	}
                                viewer.expandToLevel(2);
                            } catch ( BioclipseException e ) {
                                LogUtils.handleException( e, logger, Activator.PLUGIN_ID );
                                viewer.setInput(new String[]{"Error initializing tests"});
                            }

                            updateConsensusView();
                            updateActionStates();
                            
                            //Listen for part lifecycle events to react on editors
                            getSite().getWorkbenchWindow().getPartService().addPartListener(DSView.getInstance());

                            //Make viewer post selection to Eclipse
                            getSite().setSelectionProvider(viewer);
                            
                            //If editor is open, react on it
                            if (getSite()==null) return;
                            if (getSite().getWorkbenchWindow()==null) return;
                            if (getSite().getWorkbenchWindow().getActivePage()==null) return;

                            IEditorPart openEditor = getSite().getWorkbenchWindow()
                                                 .getActivePage().getActiveEditor();

                            if (openEditor!=null){
                            	activateEditor(openEditor);
                            	
                                if (isAutorun()){
//                                    doRunAllTests();
                                	//TODO: Implement
                                }
                            }
                        }
                        
                    });
                    
                } catch (BioclipseException e1) {
                	LogUtils.handleException( e1, logger, Activator.PLUGIN_ID );

                	Display.getDefault().asyncExec( new Runnable(){
                		public void run() {
                			viewer.setInput(new String[]{"Error initializing tests"});
                		}
                	});

                	return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                			"All tests could not be initalized: " + e1.getMessage());
                }

                monitor.done();
                return Status.OK_STATUS;
            }

        };
        job.setUser( false );
        job.schedule();
        
    }

    @SuppressWarnings("unused")
	private void updateConsensusView() {

    	//TODO: Remove if consensus part returns
    	if (true) return;

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
            int res=VotingConsensus.getConsensusFromTestRuns(activeTestRuns);
            if (res==ITestResult.POSITIVE){
                consensusText.setText( "Consensus: POSITIVE"); 
                consensusImage=warnImg;
            }
            else if (res==ITestResult.NEGATIVE){
                consensusText.setText( "Consensus: NEGATIVE"); 
                consensusImage=checkImg;
            }
            else if (res==ITestResult.INCONCLUSIVE){
                consensusText.setText( "Consensus: INCONCLUSIVE"); 
                consensusImage=equalImg;
            }
        }
        
        consensusCanvas.update();
        consensusCanvas.redraw();
        
    }


    /**
     * Clean up part listener
     */
    @Override
    public void dispose() {
        super.dispose();
        getSite().getWorkbenchWindow().getPartService().removePartListener(this);
        
        //unregister all listeners
        for (ICDKMolecule mole : molListenerMap.keySet()){
            IPropertyChangeListener li = molListenerMap.get( mole );
            if ( mole instanceof JChemPaintEditor ) {
                JChemPaintEditor jcp = (JChemPaintEditor) mole;
                jcp.removePropertyChangedListener( li);
            }
        }
        molListenerMap.clear();
        molListenerMap=null;
        
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu", "net.bioclipse.ds.context");
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
        manager.add(installModelsAction);
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(autoRunAction);
        manager.add(runAction);
        manager.add(clearAction);
        manager.add(filterOutErrorAction);
        manager.add(filterOutEmptyAction);
        manager.add(new Separator());
        manager.add(includeAction);
        manager.add(excludeAction);
        manager.add(new Separator());
        manager.add(expandAllAction);
        manager.add(collapseAllAction);
        manager.add(new Separator());
        manager.add(installModelsAction);
        manager.add(helpAction);
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }
    
    private void updateActionStates() {

        if (activeTestRuns!=null && activeTestRuns.size()>0){
            runAction.setEnabled( true );
            autoRunAction.setEnabled( true );
            clearAction.setEnabled( true );
        }
        else{
            runAction.setEnabled( false );
            autoRunAction.setEnabled( false );
            clearAction.setEnabled( false );
        }
        
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

        filterOutErrorAction = new Action() {
            public void run() {
            	
            	ViewerFilter ef=null;
            	for (int i = 0; i<viewer.getFilters().length;i++){
            		if (viewer.getFilters()[i] instanceof HideErrorsFilter) {
						ef = (HideErrorsFilter) viewer.getFilters()[i];
					}
            	}
            	
            	if (ef==null){
            		viewer.addFilter(new HideErrorsFilter());
            		filterOutErrorAction.setImageDescriptor(Activator.getImageDecriptor( "icons/errorsHidden.png" ));
            	}else{
            		viewer.removeFilter(ef);
            		filterOutErrorAction.setImageDescriptor(Activator.getImageDecriptor( "icons/errorsShown.png" ));
            	}
            	
            }
        };
        filterOutErrorAction.setText("Toggle Show Errors");
        filterOutErrorAction.setToolTipText("Turns on/off if results with errors should be displayed");
		filterOutErrorAction.setImageDescriptor(Activator.getImageDecriptor( "icons/errorsShown.png" ));

        filterOutEmptyAction = new Action() {
            public void run() {
            	
            	ViewerFilter ef=null;
            	for (int i = 0; i<viewer.getFilters().length;i++){
            		if (viewer.getFilters()[i] instanceof HideEmptyFilter) {
						ef = (HideEmptyFilter) viewer.getFilters()[i];
					}
            	}
            	
            	if (ef==null){
            		viewer.addFilter(new HideEmptyFilter());
            		filterOutEmptyAction.setImageDescriptor(Activator.getImageDecriptor( "icons/emptyHidden.png" ));
            	}else{
            		viewer.removeFilter(ef);
            		filterOutEmptyAction.setImageDescriptor(Activator.getImageDecriptor( "icons/emptyShown.png" ));
            	}
            	
            }
        };
        filterOutEmptyAction.setText("Toggle Show Empty results");
        filterOutEmptyAction.setToolTipText("Turns on/off if empty or inclonclusive results should be displayed");
        filterOutEmptyAction.setImageDescriptor(Activator.getImageDecriptor( "icons/emptyShown.png" ));

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
                                               "/html/usersguide.html");
            }
        };
        helpAction.setText("Help");
        helpAction.setToolTipText("Open help for the Decision Support");
        helpAction.setImageDescriptor(Activator.getImageDecriptor( "icons/help.gif" ));

        installModelsAction = new Action() {
            public void run() {

            	ICommandService cmdService = (ICommandService) getSite().getService(
            		    ICommandService.class);
            	IHandlerService handlerService = (IHandlerService) getSite().getService(
            			IHandlerService.class);

            	Command repoCmd = cmdService
            			.getCommand("net.bioclipse.ui.install.ShowRepositoryCatalog");
            	
            	try {
            		IParameter rparam = repoCmd.getParameter("org.eclipse.equinox.p2.ui.discovery.commands.RepositoryParameter");
            		IParameter rsparam = repoCmd.getParameter("net.bioclipse.ui.install.commands.RepositoryStrategyParameter");
            		Parameterization parm1 = new Parameterization(rparam, "http://pele.farmbio.uu.se/bioclipse/dsmodels");
            		Parameterization parm2 = new Parameterization(rsparam, "net.bioclipse.ui.install.discovery.DSModelsDiscoveryStrategy");

            		ParameterizedCommand parmCommand = new ParameterizedCommand(
            				repoCmd, new Parameterization[] { parm1,parm2 });

            		handlerService.executeCommand(parmCommand, null);
            	} catch (ExecutionException e) {
            		// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotDefinedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotEnabledException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotHandledException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}            	
            	
            }
        };
        installModelsAction.setText("Download models...");
        installModelsAction.setToolTipText("Download and install models");
        installModelsAction.setImageDescriptor(Activator.getImageDecriptor( "icons/download-models.png" ));


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
        JChemPaintEditor jcp = getJCPfromActiveEditor();
        if (jcp != null){
            doClearAndSetUpNewTestRuns( jcp.getCDKMolecule() );
        }else{
            logger.warn( "ClearAllTests NOT IMPLEMENTED FOR EDITOR: " + editor );
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
        JChemPaintEditor jcp = getJCPfromActiveEditor();
        NMEditor nme =null;
        
        if (jcp==null){

        	IEditorPart ed = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();       	
        	if (ed instanceof NMEditor) {
				nme = (NMEditor) ed;
			}
        	else{
        		showError("The current editor is not supported to run DS tests on.");
        		return;
        	}
        }
        
        //We need to remove explicit hydrogens
        IJChemPaintManager jcpmanager=net.bioclipse.cdk.jchempaint.Activator
        .getDefault().getJavaManager();
        
        jcpmanager.removeExplicitHydrogens();
 
        //Get the molecule from JCP
        ICDKMolecule pmol=null;
        Material n;
		if (jcp!=null)
        	pmol=jcp.getCDKMolecule();
        else if (nme!=null){
        	n=nme.getMaterial();
        	
        	ICDKManager cdk = net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();
        	pmol = cdk.newMolecule();

        	INmManager nm = net.bioclipse.nm.Activator.getDefault().getJavaNmManager();

        	try {
        		pmol.setProperty("bc.nm", nm.asNMX(n));
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        			
        }

        final ICDKMolecule mol = pmol;
        
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
            doClearAndSetUpNewTestRuns( mol );
//            partActivated( jcp );
            viewer.refresh();
        }
        setExecuted( true );

        IDSManager ds=net.bioclipse.ds.Activator.getDefault().getJavaManager();
        for (final TestRun tr : activeTestRuns){

            if (tr.getTest().getTestErrorMessage().length()<1){

                if (tr.getStatus()==TestRun.EXCLUDED || tr.getTest().isExcluded()){
                    viewer.refresh(tr);
                    logger.debug( "===== Test: " + tr + " skipped since excluded.");
                }
                else{
                    
                    //Start by cloning the molecule. This is to avoid threading 
                    //issues in CDK.
//                    try {
//                        IAtomContainer clonedAC=(IAtomContainer) mol
//                                                    .getAtomContainer().clone();
//                        
//                        ICDKMolecule clonedMol=new CDKMolecule(clonedAC);
//                        
//                        preprocessClonedMol(clonedMol);
                        
                        logger.debug( "== Testrun: " + tr.getTest().getName() + " started" );
                        tr.setStatus( TestRun.RUNNING );
                        tr.setMolecule( mol );
                        viewer.refresh(tr);
                        viewer.setExpandedState( tr, true );

                        runTestAsJobs( mol, ds, tr , mol); 

//                    } 
//                    catch ( CloneNotSupportedException e ) {
//                        LogUtils.handleException( e, logger, Activator.PLUGIN_ID);
//                    }
                    
                }
            }
            else{
                logger.debug("The test: " + tr.getTest() + " has an error so not run.");
            }
        }

        logger.debug( "===== All testruns started" );
    }

    /**
     * Preprocess the molecule:
     * 
     * a) Remove explicit hydrogens
     * 
     * @param clonedMol
     */
    private void preprocessClonedMol( ICDKMolecule clonedMol ) {
        
        ICDKManager cdk = net.bioclipse.cdk.business.Activator
        .getDefault().getJavaCDKManager();
        
        cdk.removeExplicitHydrogens( clonedMol );

        
    }

    private void runTestAsJobs( final ICDKMolecule clonedMol, IDSManager ds, 
                                final TestRun tr, final ICDKMolecule originalMol) {

            try {

                //Start up a job with the test
                BioclipseJob<List<ITestResult>> job = 
                    ds.runTest( tr.getTest().getId(), clonedMol, 
                    new BioclipseJobUpdateHook<List<ITestResult>>(tr.getTest().getName()));
                
            job.addJobChangeListener( new IJobChangeListener(){

                public void aboutToRun( IJobChangeEvent event ) {
                }

                public void awake( IJobChangeEvent event ) {
                }

                @SuppressWarnings("unchecked")
                public void done( IJobChangeEvent event ) {

                    final BioclipseJob<List<ITestResult>> job=(BioclipseJob<List<ITestResult>>) event.getJob();
                    List<ITestResult> result = null;
                    try{
                        result = job.getReturnValue();
                    }catch(ClassCastException e){
                        result=new ArrayList<ITestResult>();
                        result.add( new SimpleResult( e.getMessage(), SimpleResult.ERROR ) );
                    }

                    final List<ITestResult> matches = result;

                    //Update viewer in SWT thread
                    Display.getDefault().asyncExec( new Runnable(){
                        public void run() {

                            logger.debug("Matcher '" + tr.getTest().getName() 
                                        + "' finished with " + matches.size() + " hits");
                            
                            //Copy properties from result into original molecule
                            //from the cloned
                            for (Object obj : clonedMol.getAtomContainer()
                                    .getProperties().keySet()){
//                                System.out.println("OBJ found: " + obj);
//                                System.out.println("Existing: " + originalMol.getAtomContainer().getProperties());
                                if (!originalMol.getAtomContainer()
                                        .getProperties().containsKey( obj )){
                                    originalMol.getAtomContainer()
                                    .getProperties().put( 
                                               obj, clonedMol.getAtomContainer()
                                               .getProperties().get( obj ) );
//                                    System.out.println("DS-RES set on:" + clonedMol.getAtomContainer().hashCode() + "="+ clonedMol.getAtomContainer()
//                                                       .getProperties().get( obj ));
                                }
                            }
                            
                            //We also need to clone any ISubStructureMatches Atoms in AC since they are based on a clone
//                            for (ITestResult result : matches){
//                                if ( result instanceof SubStructureMatch) {
//                                    SubStructureMatch ssmatch = (SubStructureMatch) result;
//                                    IAtomContainer matchAC = ssmatch.getAtomContainer();
//                                    IAtomContainer newAC=new NNAtomContainer();
//                                    for (IAtom matchedAtom : matchAC.atoms()){
//                                        int clonedAtomno=clonedMol.getAtomContainer().getAtomNumber( matchedAtom );
//                                        IAtom newAtom = originalMol.getAtomContainer().getAtom( clonedAtomno );
//                                        newAC.addAtom( newAtom );
//                                    }
//                                    ssmatch.setAtomContainer( newAC );
//                                }
//                            }
                            
                            

                            for (ITestResult result : matches){
                                    result.setTestRun( tr );
                                    tr.addResult(result);
                            } 
                            tr.setMatches( matches );
//                            logger.debug( "===== " + tr + " finished" );
                            if (tr.getTest().getTestErrorMessage()!="")
                                tr.setStatus( TestRun.ERROR );
                            else
                                tr.setStatus( TestRun.FINISHED );
                            
                            viewer.refresh(  );
                            viewer.setExpandedState( tr, true );
                            updateConsensusView();

                            //We need to turn on external generators
                            IEditorPart part = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
                            if ( part instanceof JChemPaintEditor ) {
                                JChemPaintEditor jcp=(JChemPaintEditor)part;
                                jcp.getWidget().setUseExtensionGenerators( true );
                                //manually update jcpeditor
                                jcp.update();
                            }else if ( part instanceof MultiPageMoleculesEditorPart ) {
                                MultiPageMoleculesEditorPart mpe = (MultiPageMoleculesEditorPart)part;
                                mpe.getMoleculesPage().setUseExtensionGenerators( true );
                                mpe.getMoleculesPage().refresh();
                            }


                            
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
                }}
            );
            
            job.schedule();

            //Store ref to job in list
            runningJobs.add(job);



            
            } catch ( Exception e ) {
                logger.error( "Error running test: " + tr.getTest() + 
                              ": " + e.getMessage());
                LogUtils.debugTrace( logger, e );
                
                tr.setStatus( TestRun.ERROR );
                
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



    private void updateView() {
                
    	if (viewer.getControl().isDisposed()) return;
        viewer.refresh();
//        viewer.expandToLevel( 1 );
        viewer.expandAll();
        updateActionStates();
        
        //Also update consensus part
        updateConsensusView();
    }



    /**
     * Wait for all jobs to finish, then return ReportModel
     * @return
     */
    public DSSingleReportModel waitAndReturnReportModel(IProgressMonitor monitor){
        
        //Wait for all jobs to finish
  
//Should probably use this approach instead: TODO
//    	Job.getJobManager().join(family, monitor);
        for (BioclipseJob job : runningJobs.toArray(new BioclipseJob[0])){
            logger.debug("Waiting for Job: " + job.getName() + " to finish...");
            monitor.subTask("Waiting for Job: " + job.getName() + " to finish...");
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

        //Get mol from first testrun
        ICDKMolecule mol = null;
        for (TestRun tr : activeTestRuns){
        	mol=tr.getMolecule();
        	if (mol!=null)
        		break;
        }

        if (mol==null){
            logger.error( "No molecule to make a chart from." );
            return null;
        }

        IDSManager ds =net.bioclipse.ds.Activator.getDefault().getJavaManager();
        //Set up and return ReportModel
        DSSingleReportModel reportmodel=null;
		try {
			reportmodel = new DSSingleReportModel(mol, ds.getFullEndpoints());
		} catch (BioclipseException e) {
			e.printStackTrace();
		}

        return reportmodel;

        
    }
    
    
    public void fireExternalRun() {
        doRunAllTests();
    }


    public String getCurrentResultProperty() {
        return selectedProperty;
    }        
    
    @Override
    public Object getAdapter( Class key ) {
    
        if (key.equals(IContextProvider.class)) {
            if (contextProvider==null)
                contextProvider=new DSContextProvider();
            return contextProvider;
          }
        return super.getAdapter( key );
    }
    
    /*==============================
     * Remake of part handling events
     ==============================*/

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
//		logger.debug("Part activated: " + partRef.getId() + " detected in Eventview.");
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		logger.debug("Part brought to top: " + partRef.getId() + " detected in Eventview.");
		activatePartRef(partRef);
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		logger.debug("Part Closed: " + partRef.getId() + " detected in Eventview.");

		 IEditorPart editor = getSupportedEditor(partRef);
		 JChemPaintEditor jcp=getJCPfromEditor(editor);
		 


		 //We are currently unable to unregister listeners for ICDKMolecules in MolTable
		 //TODO: Unregister last mol in moltable (get last JCP page)
		 if (jcp!=null){

	         //Turn off all ext generators who has a boolean parameter
	         //Not the best, but nothing else to do currently
	         //TODO: Improve on this!
	         GeneratorHelper.turnOffAllExternalGenerators(jcp);

			 jcp.removePropertyChangedListener(this);
			 logger.debug("Removed prop-listener from JCP");

			 ICDKMolecule mol=jcp.getCDKMolecule();

			 if (molTestMap.keySet().contains( mol )){
				 molTestMap.remove( mol );
			 }
			 
			 //Cancel all running tests
			 for (BioclipseJob<List<ITestResult>> job : runningJobs){
				 //Ask job to cancel
				 job.cancel();
				 logger.debug("Job: " + job.getName() + " asked to cancel.");
			 }
			 
		 }
		 
		 //If no more editor, clear all in view
		 if (getSite()==null) return;
		 if (getSite().getWorkbenchWindow()==null) return;
		 if (getSite().getWorkbenchWindow().getActivePage()==null) return;
		 if (getSite().getWorkbenchWindow().getActivePage().getActiveEditor()==null){
			 deactivateView();
			 return;
		 }
		 
		 updateView();

	}


	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
//		logger.debug("Part Deactivated: " + partRef.getId() + " detected in Eventview.");
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		logger.debug("Part Hidden: " + partRef.getId() + " detected in Eventview.");
		deactivatePartRef(partRef);
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
//		logger.debug("Part Input changed: " + partRef.getId() + " detected in Eventview.");
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
//		logger.debug("Part Opened: " + partRef.getId() + " detected in Eventview.");
		//TODO: Cache results on load?
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
//		logger.debug("Part Visible: " + partRef.getId() + " detected in Eventview.");
	}
	
	
    /*==============================
     * Remake of part handling logic
     ==============================*/

	private void deactivateView() {
		activeTestRuns=null;
		IDSManager ds = net.bioclipse.ds.Activator.getDefault().getJavaManager();
		try {
			for (Endpoint ep : ds.getFullEndpoints()){
				if (ep.getTestruns()!=null)
					ep.getTestruns().clear();
			}
		} catch (BioclipseException e) {
			e.printStackTrace();
		}
		
		//Also turn off generators
		if (lastJCP!=null)
			GeneratorHelper.turnOffAllExternalGenerators(lastJCP);
		else
			logger.debug("Could not turn off ext generators: lastJCP is null!");
		
		updateView();
	}

	
	private void activatePartRef(IWorkbenchPartReference partRef){
		IEditorPart editor = getSupportedEditor(partRef);
		if (editor!=null)
			activateEditor(editor);
		
	}
	
	private void deactivatePartRef(IWorkbenchPartReference partRef){
		IEditorPart editor = getSupportedEditor(partRef);
		if (editor!=null)
			deactivateEditor(editor);
		
	}


	private IEditorPart getSupportedEditor(IWorkbenchPartReference partRef) {
		if (!(partRef instanceof IEditorReference)) return null;

		IEditorReference editorRef = (IEditorReference) partRef;
		
		IEditorPart editor=null;
		if (editorRef.getId().startsWith("net.bioclipse.cdk.ui.editors.jchempaint")) {
			logger.debug("Handled part is JChemPaintEditor");
			editor= editorRef.getEditor(false);
		}
        else if ( editorRef.getId().equals("net.bioclipse.cdk.ui.sdfeditor")) {
            editor = editorRef.getEditor(false);
			logger.debug("Handled part is MultiPageMoleculesEditorPart");
        }
        else if (editorRef.getId().startsWith("net.bioclipse.nm.ui.editors.nmeditor")) {
			logger.debug("Handled part is NMEditor");
			editor= editorRef.getEditor(false);
		}
		return editor;
	}
	
	
	private void deactivateEditor(IEditorPart editor){
		if (editor instanceof JChemPaintEditor) {
			JChemPaintEditor jcp = (JChemPaintEditor) editor;
			jcp.removePropertyChangedListener(this);
			logger.debug("Removed prop-listener from JCP");
		}
		else if (editor instanceof MultiPageMoleculesEditorPart) {
			MultiPageMoleculesEditorPart moltable = (MultiPageMoleculesEditorPart) editor;
			//TODO: how do we handle a hidden moltable?
        }
	}
	
	private void activateEditor(IEditorPart editor){
		
		if (editor instanceof JChemPaintEditor) {
			JChemPaintEditor jcp = (JChemPaintEditor) editor;
			jcp.addPropertyChangedListener(this);
			logger.debug("Activating JCP: Added prop-listener");

			ICDKMolecule mol = jcp.getCDKMolecule();
			
			if (mol==null){
				logger.debug("Molecule is null, probably not loaded yet.");
				return;
			}
			
			logger.debug("Mol exists, looking up in cache.");
			
			//Use cached if exists, else set up new
			if (molTestMap.containsKey(mol)){
				logger.debug("Using cached results.");
				
				doSetUpTestRuns(mol);
				
				updateView();
			}else{
				logger.debug("Clear and creating new results.");
				doClearAndSetUpNewTestRuns(mol);
			}
		}
		else if (editor instanceof MultiPageMoleculesEditorPart) {
			MultiPageMoleculesEditorPart moltable = (MultiPageMoleculesEditorPart) editor;
			if (!moltable.isJCPVisible()){
				logger.debug("JCP is not the visible page. Deactivate view.");
				 if (getSite()==null) return;
				 if (getSite().getWorkbenchWindow()==null) return;
				 if (getSite().getWorkbenchWindow().getActivePage()==null) return;
				 deactivateView();
			}else{
				//TODO: Handle case when switch to MolTable with JCP open
				//This should mean we have a cached mol.
				logger.debug("JCP is the visible page. Probably cached.");

				Object obj = moltable.getSelectedPage();
				if (obj instanceof JChemPaintEditor) {

					JChemPaintEditor jcp = (JChemPaintEditor) obj;
					lastJCP=jcp;
					jcp.addPropertyChangedListener(DSView.getInstance());
					logger.debug("Added prop-listener to JCP");

					ICDKMolecule mol = jcp.getCDKMolecule();
					if (mol==null){
						logger.debug("Molecule is null, not loaded yet?");
						return;
					}

					//Use cached if exists, else set up new
					if (molTestMap.containsKey(mol)){

						doSetUpTestRuns(mol);

						updateView();
					}else{
						doClearAndSetUpNewTestRuns(mol);
					}
				}else{
					logger.error("JCP is visible but class is not JCP!");
				}

			}
			

			moltable.addPageChangedListener(new IPageChangedListener() {

				@Override
				public void pageChanged(PageChangedEvent event) {
					Object obj = event.getSelectedPage();
					logger.debug("Moltable changed page to: " + obj);
					if (obj instanceof JChemPaintEditor) {
						JChemPaintEditor jcp = (JChemPaintEditor) obj;
						jcp.addPropertyChangedListener(DSView.getInstance());
						logger.debug("Added prop-listener to JCP");
						lastJCP=jcp;

						ICDKMolecule mol = jcp.getCDKMolecule();
						if (mol==null){
							logger.debug("Molecule is null, not loaded yet?");
							return;
						}
						
						//Use cached if exists, else set up new
						if (molTestMap.containsKey(mol)){
							
							doSetUpTestRuns(mol);
							
							updateView();
						}else{
							doClearAndSetUpNewTestRuns(mol);
							if (autorun)
								runAction.run();
						}

					}
					else {
						logger.debug("No JCP page visible anymore in moltable.");
						deactivateView();
						
						//Also turn off generators
//						if (lastJCP!=null)
//							GeneratorHelper.turnOffAllExternalGenerators(lastJCP);
//						else
//							logger.debug("Could not turn off ext generators: lastJCP is null!");

						return;
					}
				}
			});
        }
		else if (editor instanceof NMEditor){
			NMEditor nmeditor = (NMEditor)editor;
			
			Material material = nmeditor.getMaterial();
			
			ICDKManager cdk = net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();
			ICDKMolecule mol = cdk.newMolecule();
			
			INmManager nm = net.bioclipse.nm.Activator.getDefault().getJavaNmManager();
			
			try {
				mol.setProperty("bc.nm", nm.asNMX(material));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			doClearAndSetUpNewTestRuns(mol);
			
			
			
		}
	}

    private JChemPaintEditor getJCPfromActiveEditor() {
    
    	if (getSite()==null) return null;
        if (getSite().getWorkbenchWindow()==null) return null;
        if (getSite().getWorkbenchWindow().getActivePage()==null) return null;

        IEditorPart editor = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
        return getJCPfromEditor(editor);
    }
    
    private JChemPaintEditor getJCPfromEditor(IEditorPart editor) {
    	
    	if (editor instanceof JChemPaintEditor) {
    		return (JChemPaintEditor)editor;			
    	}
    	else if (editor instanceof MultiPageMoleculesEditorPart) {
    		MultiPageMoleculesEditorPart moltable = (MultiPageMoleculesEditorPart) editor;
    		if (moltable.isJCPVisible()){
				Object obj = moltable.getSelectedPage();
				if (obj instanceof JChemPaintEditor) {
					return (JChemPaintEditor) obj;
				}
    		}
    	}

    	return null;
	}

	

	
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		
		logger.debug("Property changed!!!");
		
		if(event.getProperty().equals( JChemPaintEditor.
				STRUCUTRE_CHANGED_EVENT )) {
			JChemPaintEditor jcp=(JChemPaintEditor)event.getSource();
			ICDKMolecule cdkmol = jcp.getCDKMolecule();
			JCPModelChanged(cdkmol);
		}
		else if(event.getProperty().equals( JChemPaintEditor.
				MODEL_LOADED )) {
			JChemPaintEditor jcp=(JChemPaintEditor)event.getSource();
			ICDKMolecule cdkmol = jcp.getCDKMolecule();
			JCPModelLoaded(cdkmol);
		}
	}    


	private void JCPModelLoaded(ICDKMolecule cdkmol){
		logger.debug ("EventView reacting: JCP model is loaded. Molecule: " + cdkmol);
		doSetUpTestRuns(cdkmol);
		//TODO: VERIFY
		if (autorun && !executed)
			runAction.run();
	}

	private void JCPModelChanged(ICDKMolecule cdkmol){
		logger.debug
		("EventView reacting: JCP editor model has changed");
		doClearAndSetUpNewTestRuns(cdkmol);
		//TODO: VERIFY
		if (autorun && !executed)
			runAction.run();
	}


	
	/*
	 * Below are subject to verification/update
	 */
	

    private void doClearAndSetUpNewTestRuns( ICDKMolecule mol ) {

        List<TestRun> newTestRuns=new ArrayList<TestRun>();
        
        IDSManager ds = net.bioclipse.ds.Activator.getDefault().getJavaManager();

        //Get the endpoints
        try {
        
            for (Endpoint ep : ds.getFullEndpoints()){

                //First remove any old TestRuns
                if (ep.getTestruns()!=null)
                    ep.getTestruns().clear();

                if (ep.getTests()!=null){

                    //Now, create new TestRuns from the tests
                    for (IDSTest test : ep.getTests()){

                        TestRun newTestRun=new TestRun(mol,test);

                        if (test.getTestErrorMessage()!=null 
                                && test.getTestErrorMessage().length()>0){
                            newTestRun.setStatus( TestRun.ERROR );
                        }
                        else if (test.isExcluded()){
                            newTestRun.setStatus( TestRun.EXCLUDED );
                        }                

                        newTestRuns.add( newTestRun ); 
                        ep.addTestRun(newTestRun);
                    }
                }
            }

        } catch ( BioclipseException e1 ) {
            LogUtils.handleException( e1, logger, Activator.PLUGIN_ID);
        }
        
        molTestMap.put( mol, newTestRuns );
        activeTestRuns=newTestRuns;
        setExecuted( false );
        updateView();
    }    
    
    
    
    private void doSetUpTestRuns( ICDKMolecule mol ) {

    	//Use cached, if no cached, clear and create new
		activeTestRuns=molTestMap.get(mol);
		if (activeTestRuns==null){
			doClearAndSetUpNewTestRuns(mol);
			return;
		}
		
        //For all endpoints, clear old and add these testruns
        IDSManager ds = net.bioclipse.ds.Activator.getDefault().getJavaManager();
        try {
			for (Endpoint ep : ds.getFullEndpoints()){

			    //First remove any old TestRuns
			    if (ep.getTestruns()!=null)
			        ep.getTestruns().clear();

			    if (ep.getTests()!=null){
			        //Loop over all tests in this Endpoint
			        for (IDSTest epTest : ep.getTests()){
			            //For the active testruns, locate those who are of this test
			            for (TestRun tr : activeTestRuns){
			                if (tr.getTest().getId().equals( epTest.getId() )){
			                    ep.addTestRun( tr );
//			                    logger.debug("Added cached TestRun: " + tr);
			                }
			            }

			        }
			    }
			}
		} catch (BioclipseException e) {
			e.printStackTrace();
		}
		
        updateView();
		
    }

	public void externalRefresh() throws BioclipseException {
        IDSManager ds = net.bioclipse.ds.Activator.getDefault().getJavaManager();
        viewer.setInput(ds.getFullEndpoints().toArray());
        doClearAllTests();
        updateView();
	}



}
