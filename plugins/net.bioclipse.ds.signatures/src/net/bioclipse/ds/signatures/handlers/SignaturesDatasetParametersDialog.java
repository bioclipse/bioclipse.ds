package net.bioclipse.ds.signatures.handlers;

import java.io.File;
import java.util.List;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.databinding.dialog.DialogPageSupport;
import org.eclipse.jface.databinding.dialog.TitleAreaDialogSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SignaturesDatasetParametersDialog extends TitleAreaDialog{

	SignaturesDatasetModel datasetModel;
	
	private Text txtHeight;
	private Text txtFilePath;

	private IContainer baseContainer;
	private String fileExtension;
	private List<String> properties;

	private Combo cboHeight;
	private Combo cboResponseProp;
	private Combo cboNameProp;

    public SignaturesDatasetParametersDialog(Shell parentShell) {
        super( parentShell );
        datasetModel = new SignaturesDatasetModel();
    }

    public SignaturesDatasetParametersDialog(Shell parentShell, IContainer iContainer, String fileExtension, List<String> properties) {
        this( parentShell );
        this.baseContainer=iContainer;
        this.fileExtension=fileExtension;
        this.properties=properties;
    }

    public SignaturesDatasetModel getDatasetModel() {
		return datasetModel;
	}

	public void setDatasetModel(SignaturesDatasetModel datasetModel) {
		this.datasetModel = datasetModel;
	}

	@Override
    protected Control createDialogArea( Composite parent ) {
        
        setTitle( "Create signatures dataset" );
        setMessage( "Create signatures dataset" );
//        setTitleImage(Activator.getImageDescriptor( "icons/sign.jpg" ).createImage() );
        
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setFont(parent.getFont());

        // Build the separator line
        Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL
                                            | SWT.SEPARATOR);
        GridData gdl=new GridData(SWT.FILL, SWT.TOP, true, false);
        gdl.horizontalSpan=2;
        titleBarSeparator.setLayoutData(gdl);

        //FILE param
		Label lbl=new Label(composite, SWT.NONE);
		lbl.setText("File to save: ");
		GridData gdlbl= new GridData();
		gdlbl.widthHint=100;
		lbl.setLayoutData(gdlbl);
		
		txtFilePath=new Text(composite, SWT.BORDER);
		GridData gdtxt= new GridData(GridData.FILL_HORIZONTAL);
		txtFilePath.setLayoutData(gdtxt);

//		Button browse = new Button(composite, SWT.NONE);
//		browse.setText("Browse...");
//		GridData gdbtn= new GridData();
//		browse.setLayoutData(gdbtn);
//		gdbtn.widthHint=100;
//		browse.addSelectionListener(new SelectionAdapter(){
//
//			public void widgetSelected(SelectionEvent e) {
//
//				FileDialog dialog = new FileDialog(composite.getShell(), SWT.SAVE);
//				dialog.setFilterExtensions(new String[] { "*." + fileExtension });
//				dialog.setText("Select file to save dataset");
//				if (baseContainer!=null)
//					dialog.setFilterPath(baseContainer.getLocation().toOSString());
//				String selectedFileName = dialog.open();
//
//				if (selectedFileName != null) {
//					setErrorMessage(null);
//			        setDestinationValue(selectedFileName);
//				}
//				else if (txtFilePath.getText()==null){
//					setErrorMessage("Please specify a file to save");
//				}
//
//			}
//
//		});
		
        // HEIGHT param
        Label lblNumConf=new Label(composite,SWT.NONE);
        lblNumConf.setText( "Signatures height:  " );
        lblNumConf.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

        cboHeight=new Combo(composite,SWT.NONE);
        GridData comboGD=new GridData(SWT.LEFT, SWT.NONE, true, false);
//        comboGD.horizontalSpan=2;
        cboHeight.setLayoutData(comboGD);
        cboHeight.add("1");
        cboHeight.add("2");
        cboHeight.add("3");
        cboHeight.add("4");
        cboHeight.add("5");
        cboHeight.add("6");
        cboHeight.select(2);
        
        // RESPONSE PROP
        Label lblResponseProp=new Label(composite,SWT.NONE);
        lblResponseProp.setText( "Response property:  " );
        lblResponseProp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

        cboResponseProp=new Combo(composite,SWT.NONE);
        cboResponseProp.setLayoutData(comboGD);
        for (String prop : properties)
        	cboResponseProp.add(prop);

        // NAME PROP
        Label lblNameProp=new Label(composite,SWT.NONE);
        lblNameProp.setText( "Name property:  " );
        lblNameProp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

        cboNameProp=new Combo(composite,SWT.NONE);
        cboNameProp.setLayoutData(comboGD);
    	cboNameProp.add("NONE");
        for (String prop : properties)
        	cboNameProp.add(prop);

        //Do binding from widgets to model
        bindValues();

        //Initial values in combo
    	cboNameProp.select(0); //NONE is preselected
        if (properties.size()>0)
        	cboResponseProp.select(0);  //Only preselect a response property if exists
        
		return composite;
    }

    protected void setDestinationValue(String selectedFileName) {
		txtFilePath.setText(selectedFileName);
	}


    private void bindValues() {

    	// The DataBindingContext object will manage the databindings
		// Lets bind it
		DataBindingContext ctx = new DataBindingContext();

		IObservableValue widgetValue = WidgetProperties.selection().observe(cboHeight);
		IObservableValue modelValue = BeansObservables.observeValue(datasetModel, "height");
		ctx.bindValue(widgetValue, modelValue);

		widgetValue = WidgetProperties.selection().observe(cboResponseProp);
		modelValue = BeansObservables.observeValue(datasetModel, "responseProperty");
		ctx.bindValue(widgetValue, modelValue);

		widgetValue = WidgetProperties.selection().observe(cboNameProp);
		modelValue = BeansObservables.observeValue(datasetModel, "nameProperty");
		ctx.bindValue(widgetValue, modelValue);
		
		widgetValue = WidgetProperties.text(SWT.Modify).observe(txtFilePath);
		modelValue = BeansObservables.observeValue(datasetModel, "newFile");
		UpdateValueStrategy update = new UpdateValueStrategy();
		update.setAfterConvertValidator(new FileExistsValidator());
		ctx.bindValue(widgetValue, modelValue, update, null);

		
		TitleAreaDialogSupport.create(this, ctx);
	}
	
	
}
