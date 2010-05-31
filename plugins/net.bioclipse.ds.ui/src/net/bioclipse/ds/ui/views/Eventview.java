package net.bioclipse.ds.ui.views;

import java.util.HashMap;
import java.util.Map;

import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.cdk.ui.sdfeditor.editor.MultiPageMoleculesEditorPart;

import org.apache.log4j.Logger;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;

/**
 * 
 * Aim: detect events for a view listening to events on molecular editors.
 * 
 * - Editor with JCP visible is opened (JCP/MolTable)
 * - Changes in structure in visible JCP
 * - and all related events/editor operations
 * 
 * 
 * 
 * @author ola
 *
 */
public class Eventview extends ViewPart implements IPartListener2{

    private static final Logger logger = Logger.getLogger(DSView.class);
    
    private Map<JChemPaintEditor, IPropertyChangeListener> editorListenerMap;

	public Eventview() {
		editorListenerMap=new HashMap<JChemPaintEditor, IPropertyChangeListener>();
	}

	
	@Override
	public void createPartControl(Composite parent) {

        getSite().getWorkbenchWindow().getPartService().addPartListener(this);

		//TODO: What if we already have an editor open on view creation?
		
	}

	@Override
	public void setFocus() {
//		logger.debug("Focus gained in Eventview.");
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
//		logger.debug("Part activated: " + partRef.getId() + " detected in Eventview.");
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
//		logger.debug("Part brouight to top: " + partRef.getId() + " detected in Eventview.");
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
//		logger.debug("Part Closed: " + partRef.getId() + " detected in Eventview.");
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
//		logger.debug("Part Deactivated: " + partRef.getId() + " detected in Eventview.");
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
//		logger.debug("Part Hidden: " + partRef.getId() + " detected in Eventview.");
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
//		logger.debug("Part Input changed: " + partRef.getId() + " detected in Eventview.");
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		logger.debug("Part Opened: " + partRef.getId() + " detected in Eventview.");

		handleEditor(partRef);

	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
//		logger.debug("Part Visible: " + partRef.getId() + " detected in Eventview.");
	}
	
	
	
	
	
	
	
	private void handleEditor(IWorkbenchPartReference partRef){

		
		if (!(partRef instanceof IEditorReference)) return;
		IEditorReference editorRef = (IEditorReference) partRef;
		
		JChemPaintEditor jcp=null;
		if (editorRef.getId().startsWith("net.bioclipse.cdk.ui.editors.jchempaint")) {
			logger.debug("Part Opened is JChemPaintEditor");
			jcp=(JChemPaintEditor) editorRef.getEditor(false);
		}
        else if ( editorRef.getId().equals("net.bioclipse.cdk.ui.sdfeditor")) {
            MultiPageMoleculesEditorPart editor = (MultiPageMoleculesEditorPart)editorRef.getEditor(false);
			logger.debug("Part Opened is MultiPageMoleculesEditorPart");
            
            if (editor.isJCPVisible()){
    			logger.debug("MultiPageMoleculesEditorPart has JCP page open");
                //JCP is active
                Object obj = editor.getAdapter(JChemPaintEditor.class);
                if (obj!= null){
                    jcp=(JChemPaintEditor)obj;
                }
            }else{
    			logger.debug("MultiPageMoleculesEditorPart has not the JCP page open");
            }
        }
		
		if (jcp!=null)
			registerJCPListeners(jcp);
			
	}

	
	
	/**
	 * Register listeners on teh active JCPeditor. This should only be done once per 
	 * JCP instance or, even better, once per underlying CDKMolecule (if possible).
	 * 
	 * @param jcp
	 */
	private void registerJCPListeners(JChemPaintEditor jcp) {

		//If editor already is registered, skip adding property listeners
		if (editorListenerMap.keySet().contains(jcp)) return;

		IPropertyChangeListener jcplistener = new IPropertyChangeListener() {
			public void propertyChange( PropertyChangeEvent event ) {

				if(event.getProperty().equals( JChemPaintEditor.
						STRUCUTRE_CHANGED_EVENT )) {
					logger.debug
					("EventView reacting: JCP editor model has changed");
				}
				else if(event.getProperty().equals( JChemPaintEditor.
						MODEL_LOADED )) {
					logger.debug
					("EventView reacting: JCP model is loaded");

				}
			}
		};
		jcp.addPropertyChangedListener(jcplistener);
	}

}
