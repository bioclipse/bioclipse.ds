package net.bioclipse.ds.sdk.pdewizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.pde.ui.templates.BaseOptionTemplateSection;
import org.eclipse.pde.ui.templates.TemplateOption;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class ModelsOption extends TemplateOption {

	private Label labelControl;

	/**
	 * The constructor of the option.
	 * 
	 * @param section
	 *            the parent section
	 * @param name
	 *            the unique name
	 * @param label
	 *            the presentable label of the option
	 */
	public ModelsOption(BaseOptionTemplateSection section, String name, String label) {
		super(section, name, label);
		setRequired(true);
	}


	/**
	 * Implementation of the superclass method that updates the option's widget
	 * with the new value.
	 * 
	 * @param value
	 *            the new option value
	 */
	public void setValue(Object value) {
		super.setValue(value);
	}

	/**
	 * Creates the boolean option control. Option reserves the right to modify
	 * the actual widget used as long as the user can modify its boolean state.
	 * 
	 * @param parent
	 *            the parent composite of the option widget
	 * @param span
	 *            the number of columns that the widget should span
	 */
	public void createControl(Composite parent, int span) {
		labelControl = createLabel(parent, 1);
		labelControl.setEnabled(isEnabled());

		CheckboxTreeViewer projectsList = new CheckboxTreeViewer(parent, SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = new PixelConverter(projectsList.getControl()).convertWidthInCharsToPixels(25);
		gridData.heightHint = new PixelConverter(projectsList.getControl()).convertHeightInCharsToPixels(10);
		projectsList.getControl().setLayoutData(gridData);
		projectsList.setContentProvider(new ITreeContentProvider() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java
			 * .lang.Object)
			 */
			public Object[] getChildren(Object parentElement) {
				return null;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.jface.viewers.IStructuredContentProvider#getElements
			 * (java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return getAvailableModels();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java
			 * .lang.Object)
			 */
			public boolean hasChildren(Object element) {
				return false;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.jface.viewers.ITreeContentProvider#getParent(java
			 * .lang.Object)
			 */
			public Object getParent(Object element) {
				return null;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse
			 * .jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}

		});

		projectsList.setLabelProvider(new LabelProvider(){
			
			@Override
			public String getText(Object element) {
				if (element instanceof String[]) {
					String[] strs = (String[]) element;
					return strs[1];
				}

				return super.getText(element);
			}
			
		});

		projectsList.addCheckStateListener(new ICheckStateListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged
			 * (org.eclipse.jface.viewers.CheckStateChangedEvent)
			 */
			public void checkStateChanged(CheckStateChangedEvent event) {
				Object element = event.getElement();
				String[] cElement=(String[]) element;

				//get and cast existing values
				Object vals=getValue();
				List<String> cvals=null;
				if (vals==null)
					cvals=new ArrayList<String>();
				else{
					cvals = (List<String>) vals;
				}

				//set new value
				boolean checked = event.getChecked();
				
				//Look up options for the selected page and set required if checked
				int pageno=getPageNumber(cElement[0]);
				List<TemplateOption> pageOptions = ((DSTemplate)getSection()).getOptionControl().get(pageno);
				if (pageOptions!=null){
					for (TemplateOption to : pageOptions){
						to.setRequired(checked);
						getSection().validateOptions(to);
					}
				}

				if (checked){
					cvals.add(cElement[0]);
				}
				else{
					cvals.remove(cElement[0]);
				}
				
				if (cvals.size()==0)
					setValue(null);	//Marks as missing required field
				else
					setValue(cvals);
				
				getSection().validateOptions( ModelsOption.this);
				getSection().getPage(0).getWizard().getContainer().updateButtons();

			}
		});

		projectsList.setInput(this);
		projectsList.setComparator(new ViewerComparator());
		
	
	}
	

	@Override
	public boolean isEmpty() {
		if (getValue()==null) return true;
		else return false;
	}


	public static int getPageNumber(String modelID) {

		int cnt=2;  //Forst model starts on page 2
		for (Object a : getAvailableModels()){
			String[] model=(String[]) a;
			if (model[0].equals(modelID)) return cnt;
			cnt++;
		}
		
		return -1;
	}


	
	
	protected static Object[] getAvailableModels() {
		
		//Define the available tests
		
		//2 classes
		String[][] classificationModels = new String[3][2];
		classificationModels[0][0] = "net.bioclipse.ds.matcher.SDFPosNegExactMatchSignatures";
		classificationModels[0][1] = "Exact Match (molecular signature)";

		classificationModels[1][0] = "net.bioclipse.ds.matcher.SDFPosNegNearestMatchFP";
		classificationModels[1][1] = "Nearest neighbour (CDK fingerprint)";
		
		classificationModels[2][0] = "qsar.libsvm.atomsign";
		classificationModels[2][1] = "QSAR (libsvm, atom signatures)";

		
		return classificationModels;
	}

}


