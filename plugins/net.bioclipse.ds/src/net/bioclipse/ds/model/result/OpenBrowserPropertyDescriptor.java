package net.bioclipse.ds.model.result;

import net.bioclipse.browser.editors.RichBrowserEditor;
import net.bioclipse.cdk.business.Activator;

import org.eclipse.jface.text.hyperlink.MultipleHyperlinkPresenter;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.part.NullEditorInput;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * 
 * @author ola
 *
 */
public class OpenBrowserPropertyDescriptor extends PropertyDescriptor {

	private String url;

	public OpenBrowserPropertyDescriptor(Object id, String displayName, String url) {
		super(id, displayName);
		this.url=url;
	}

	@Override
	public ILabelProvider getLabelProvider() {
		return new LabelProvider(){
			@Override
			public Image getImage(Object element) {

				Image errorIcon = Activator.imageDescriptorFromPlugin( 
						net.bioclipse.ds.Activator.PLUGIN_ID, 
						"/icons/expand.png" ).createImage();

				return errorIcon;
			}

		};
	}

	


	public CellEditor createPropertyEditor(Composite parent) {

		//Open an editor here for testing purposes
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				IEditorPart editor;
				try {
					editor = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage()
							.openEditor( new NullEditorInput(), RichBrowserEditor.EDITOR_ID );
					if (editor!=null){
						((RichBrowserEditor)editor).setURL( url);
					}
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		
		return null; 

//		return new DialogCellEditor() {
//			
//			@Override
//			protected Object openDialogBox(Control cellEditorWindow) {
//				
//				System.out.println("OPEN DIALOG BOX");
//				return null;
//			}
//		};

	}


}
