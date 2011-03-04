package net.bioclipse.ds.sdk.pdewizard;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.bioclipse.ds.sdk.cdk.CDKHelper;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.pde.ui.templates.BaseOptionTemplateSection;
import org.eclipse.pde.ui.templates.StringOption;
import org.eclipse.pde.ui.templates.TemplateOption;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FileOption extends TemplateOption {
	private Text text;
	private Label labelControl;
	private boolean ignoreListener;
	private int fStyle;

	private Button button;
	private Label entriesLabel;
	private Label propertiesLabel;

	
	private final static int F_DEFAULT_STYLE = SWT.SINGLE | SWT.BORDER;

	/**
	 * The constructor.
	 * 
	 * @param section
	 *            the parent section
	 * @param name
	 *            the unique option name
	 * @param label
	 *            the translatable label of the option
	 */
	public FileOption(BaseOptionTemplateSection section, String name, String label) {
		super(section, name, label);
		fStyle = F_DEFAULT_STYLE;
		setRequired(true);
	}

	/**
	 * Update the text widget style to be read only
	 * Added to default style (does not override)
	 * @param readOnly
	 */
	public void setReadOnly(boolean readOnly) {
		if (readOnly) {
			fStyle = F_DEFAULT_STYLE | SWT.READ_ONLY;
		} else {
			fStyle = F_DEFAULT_STYLE;
		}
	}

	/**
	 * A utility version of the <samp>getValue() </samp> method that converts
	 * the current value into the String object.
	 * 
	 * @return the string version of the current value.
	 */
	public String getText() {
		if (getValue() != null)
			return getValue().toString();
		return null;
	}

	/**
	 * A utility version of the <samp>setValue </samp> method that accepts
	 * String objects.
	 * 
	 * @param newText
	 *            the new text value of the option
	 * @see #setValue(Object)
	 */
	public void setText(String newText) {
		setValue(newText);
	}

	/**
	 * Implements the superclass method by passing the string value of the new
	 * value to the widget
	 * 
	 * @param value
	 *            the new option value
	 */
	public void setValue(Object value) {
		super.setValue(value);
		if (text != null) {
			ignoreListener = true;
			String textValue = getText();
			text.setText(textValue != null ? textValue : ""); //$NON-NLS-1$
			ignoreListener = false;
		}
	}

	/**
	 * Creates the string option control.
	 * 
	 * @param parent
	 *            parent composite of the string option widget
	 * @param span
	 *            the number of columns that the widget should span
	 * @param button 
	 */
	public void createControl(Composite parent, int span) {
		labelControl = createLabel(parent, 1);
		labelControl.setEnabled(isEnabled());
		text = new Text(parent, fStyle);
		if (getValue() != null)
			text.setText(getValue().toString());

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = span - 1;
		text.setLayoutData(gd);
		text.setEnabled(isEnabled());
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (ignoreListener)
					return;
				FileOption.super.setValue(text.getText());
				getSection().validateOptions(FileOption.this);
			}
		});


		Label label = new Label(parent, SWT.NONE);

		button = new Button(parent, SWT.NONE);
		button.setText("Browse...");

		GridData gd2 = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd2.horizontalSpan = span - 1;
		button.setLayoutData(gd2);

		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				//Open browse button
				FileDialog dlg= new FileDialog(e.display.getActiveShell(), SWT.OPEN);
				dlg.setFilterExtensions(new String[]{"sdf"});
				String ret = dlg.open();

				text.setText(ret);
				FileOption.super.setValue(text.getText());
				getSection().validateOptions(FileOption.this);
				fireParseThread();
				
			}
		});
		button.setEnabled(isEnabled());

		Label l1 = new Label(parent, SWT.NONE);
		l1.setText("Entries in file:");
		
		entriesLabel = new Label(parent, SWT.NONE);
		entriesLabel.setText("");
		GridData gd3 = new GridData(GridData.FILL_HORIZONTAL);
		entriesLabel.setLayoutData(gd3);

		Label l2 = new Label(parent, SWT.NONE);
		l2.setText("Available properties:");

		propertiesLabel = new Label(parent, SWT.NONE);
		propertiesLabel.setText("");
		GridData gd4 = new GridData(GridData.FILL_HORIZONTAL);
		propertiesLabel.setLayoutData(gd4);


		
	}

	protected void fireParseThread() {

		entriesLabel.setText("Parsing...");
		propertiesLabel.setText("");

        new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				String f = (String) ((DSTemplate)getSection()).getOptionByName(DSTemplate.KEY_DATA_FILE).getValue();
				//TODO parse file and get number of entries + properties from first entry
				//Store these as options for later models

				//Prepared for progress monitor...
				int nomols = -1;
				List<String> availableProps = null;
				try {

					nomols = CDKHelper.numberOfEntriesInSDF(f, new NullProgressMonitor());
					System.out.println("Mols in file: " + nomols);
					availableProps=CDKHelper.getAvailableProperties(f);
					System.out.println("Properties: " + availableProps);

				} catch (IOException e1) {
				}
				
				final int nomolsD = nomols;
				final List<String> availPropsD = availableProps;
				
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						
						//Register these options in template
						((DSTemplate)getSection()).setNoMols(nomolsD);
						((DSTemplate)getSection()).setAvailProps(availPropsD);

						if (nomolsD<=0)
							entriesLabel.setText("N/A");
						else
							entriesLabel.setText("" + nomolsD);

						if (availPropsD==null || availPropsD.size()==0)
							propertiesLabel.setText("N/A");
						else
							propertiesLabel.setText(availPropsD.toString());

					}
				});
			}
		}).start();

		
	}

	/**
	 * A string option is empty if its text field contains no text.
	 * 
	 * @return true if there is no text in the text field.
	 */
	public boolean isEmpty() {
		return getValue() == null || getValue().toString().length() == 0;
	}

	/**
	 * Implements the superclass method by passing the enabled state to the
	 * option's widget.
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (labelControl != null) {
			labelControl.setEnabled(enabled);
			text.setEnabled(enabled);
		}
	}
}
