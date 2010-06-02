package net.bioclipse.ds.ui.views;

import java.util.HashMap;
import java.util.Map;

import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.cdk.ui.sdfeditor.editor.MultiPageMoleculesEditorPart;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
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
    
    private Map<ICDKMolecule, IPropertyChangeListener> editorListenerMap;

	public Eventview() {
		editorListenerMap=new HashMap<ICDKMolecule, IPropertyChangeListener>();
	}

	
	@Override
	public void createPartControl(Composite parent) {

        getSite().getWorkbenchWindow().getPartService().addPartListener(this);

		//TODO: What if we already have an editor open on view creation?
        IEditorPart editor=getSite().getPage().getActiveEditor();
        
        if (editor!=null)
        	handleEditor(editor);
		
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
		logger.debug("Part brouight to top: " + partRef.getId() + " detected in Eventview.");
		//TODO: Switch view model here
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		logger.debug("Part Closed: " + partRef.getId() + " detected in Eventview.");
		//TODO: Remove listeners here
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
//		logger.debug("Part Deactivated: " + partRef.getId() + " detected in Eventview.");
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		logger.debug("Part Hidden: " + partRef.getId() + " detected in Eventview.");
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
//		logger.debug("Part Input changed: " + partRef.getId() + " detected in Eventview.");
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		logger.debug("Part Opened: " + partRef.getId() + " detected in Eventview.");
		handlePartRef(partRef);
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
//		logger.debug("Part Visible: " + partRef.getId() + " detected in Eventview.");
	}
	
	@Override
	public void dispose() {
		//Remove all listeners here
		
		super.dispose();
	}
	
	
	
	private void handlePartRef(IWorkbenchPartReference partRef){
		if (!(partRef instanceof IEditorReference)) return;
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
		if (editor!=null)
			handleEditor(editor);
		
	}
	
	private void handleEditor(IEditorPart editor){
		
		JChemPaintEditor jcp=null;
		if (editor instanceof JChemPaintEditor) {
			registerJCPListeners((JChemPaintEditor) editor);			
		}
		else if (editor instanceof MultiPageMoleculesEditorPart) {
			MultiPageMoleculesEditorPart moltable = (MultiPageMoleculesEditorPart) editor;

			moltable.addPageChangedListener(new IPageChangedListener() {

				@Override
				public void pageChanged(PageChangedEvent event) {
					Object obj = event.getSelectedPage();
					System.out.println("Moltable changed page to: " + obj);
					if (obj instanceof JChemPaintEditor) {
						JChemPaintEditor jcp = (JChemPaintEditor) obj;
						registerJCPListeners(jcp);
					}
					else {
						logger.debug("No JCP visible anymore.");
					}
				}
			});
        }
	}

	
	
	/**
	 * Register listeners on the active JCPeditor. This should only be done once per 
	 * JCP instance or, even better, once per underlying CDKMolecule (if possible).
	 * 
	 * @param jcp
	 */
	private void registerJCPListeners(JChemPaintEditor jcp) {

		//If editor already is registered, skip adding property listeners
		if (editorListenerMap.keySet().contains(jcp.getCDKMolecule())){
			logger.debug("   Skipped registering listeners, cdkmol already in map.");
			return;
		}

		IPropertyChangeListener jcplistener = new IPropertyChangeListener() {
			public void propertyChange( PropertyChangeEvent event ) {

				if(event.getProperty().equals( JChemPaintEditor.
						STRUCUTRE_CHANGED_EVENT )) {
					logger.debug
					("EventView reacting: JCP editor model has changed");
				}
				else if(event.getProperty().equals( JChemPaintEditor.
						MODEL_LOADED )) {
					JChemPaintEditor jcp=(JChemPaintEditor)event.getSource();
					ICDKMolecule cdkmol = jcp.getCDKMolecule();
					logger.debug ("EventView reacting: JCP model is loaded. Molecule: " + cdkmol);
				}
			}
		};
		jcp.addPropertyChangedListener(jcplistener);
		editorListenerMap.put(jcp.getCDKMolecule(), jcplistener);
	}

}
