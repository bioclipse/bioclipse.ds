/* Copyright (c) 2009  Arvid Berg <arvid.berg@farmbio.uu.se>
 *               2009  Egon Willighagen <egonw@user.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.ds.sdk.pdewizard;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import net.bioclipse.ds.sdk.Activator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginModelFactory;
import org.eclipse.pde.core.plugin.IPluginReference;
import org.eclipse.pde.internal.core.bundle.BundlePluginBase;
import org.eclipse.pde.internal.core.ibundle.IBundle;
import org.eclipse.pde.internal.core.text.plugin.PluginNode;
import org.eclipse.pde.internal.core.text.plugin.PluginObjectNode;
import org.eclipse.pde.internal.ui.IHelpContextIds;
import org.eclipse.pde.ui.IFieldData;
import org.eclipse.pde.ui.templates.ComboChoiceOption;
import org.eclipse.pde.ui.templates.OptionTemplateSection;
import org.eclipse.pde.ui.templates.PluginReference;
import org.eclipse.pde.ui.templates.StringOption;
import org.eclipse.pde.ui.templates.TemplateOption;

public class DSTemplate extends OptionTemplateSection {

	//Endpoints
	private static final String KEY_NEW_ENDPOINT_ID = "newEndpointID";
	private static final String KEY_NEW_ENDPOINT_NAME = "newEndpointName";
	private static final String KEY_NEW_ENDPOINT_DESCRIPTION = "newEndpointDescription";

	//Data file
	static final String KEY_DATA_FILE = "sdFile";

	//models
	public static final String KEY_SELECTED_MODELS = "selectedModels";

	//exact matches
	private static final String EXACT_RESPONSE_PROPERTY_NAME = "EXACT_RESPONSE_PROPERTY_NAME";
	private static final String EXACT_RESPONSE_POSITIVE_VALUE = "EXACT_RESPONSE_POSITIVE_VALUE";
	private static final String EXACT_RESPONSE_NEGATIVE_VALUE = "EXACT_RESPONSE_NEGATIVE_VALUE";

	//nearest
	private static final String NEAREST_RESPONSE_NEGATIVE_VALUE = "NEAREST_RESPONSE_NEGATIVE_VALUE";
	private static final String NEAREST_RESPONSE_POSITIVE_VALUE = "NEAREST_RESPONSE_POSITIVE_VALUE";
	private static final String NEAREST_RESPONSE_PROPERTY_NAME = "NEAREST_RESPONSE_PROPERTY_NAME";
	public static final String NEAREST_TANIMOTO = "nn_tanimoto";

	//qsar
	private static final String QSAR_RESPONSE_PROPERTY_NAME = "responseProperty";
	public static final String QSAR_LIBSVM_GRID = "GRID SEARCH";
	public static final String QSAR_SIGNATURE_HEIGHT = "signagure_height";
	private static final String QSAR_MODEL_TYPE = "QSAR_MODEL_TYPE";

	
	//And things we need here..
	private Wizard wizard;
	private Map<Integer,List<TemplateOption>> optionControl;
	
	private int noMols;
	private List<String> availProps;
	

	public Map<Integer, List<TemplateOption>> getOptionControl() {
		return optionControl;
	}
	public Wizard getWizard() {
		return wizard;
	}
	public void setWizard(Wizard wizard) {
		this.wizard = wizard;
	}

	public DSTemplate() {
		super();
		setPageCount(5);
		createOptions();
	}
	@Override
	public void addPages(Wizard wizard) {
		
		this.wizard=wizard;
		
		WizardPage page = createPage(0);
		page.setTitle("Endpoint and data");
		page.setDescription("Create endpoints and select a data file (SD-file)");
		page.setPageComplete(false);
		wizard.addPage(page);
		
		WizardPage page2 = createPage(1);
		page2.setTitle("Model selection");
		page2.setDescription("Select the models to build");
		page2.setPageComplete(false);
		wizard.addPage(page2);

		//
		WizardPage page3 = createPage(2);
		page3.setTitle("Exact matches");
		page3.setDescription("Set parameters for exact matches");
		wizard.addPage(page3);

		WizardPage page4 = createPage(3);
		page4.setTitle("Near neighbours");
		page4.setDescription("Set parameters for near neighbours");
		wizard.addPage(page4);

		WizardPage page5 = createPage(4);
		page5.setTitle("QSAR model");
		page5.setDescription("Set parameters for the QSAR model");
		wizard.addPage(page5);

//		WizardPage page6 = createPage(5);
//		page6.setTitle("Structural alerts");
//		page6.setDescription("Set parameters for the QSAR model");
//		wizard.addPage(page6);

		markPagesAdded();
	}
	
	
	

	private void createOptions() {

		optionControl = new HashMap<Integer, List<TemplateOption>>();
		
		//Page 1
		addOption(KEY_NEW_ENDPOINT_ID, "Endpoint ID", null, 0);
		addOption(KEY_NEW_ENDPOINT_NAME, "Endpoint Name", null, 0);
		addOption(KEY_NEW_ENDPOINT_DESCRIPTION, "Endpoint Description", null, 0);

		FileOption option = new FileOption(this, KEY_DATA_FILE, "Data file");
		registerOption(option, null, 0);

		//Page 2
		ModelsOption modelOption = new ModelsOption(this, KEY_SELECTED_MODELS, "Select models");
		registerOption(modelOption, null, 1);

		
		//Page 3 - EXACT
		DSComboChoiceOption o = new DSComboChoiceOption(this, EXACT_RESPONSE_PROPERTY_NAME, "Response property", new String[0][0]);
		registerOption(o, null, 2);
		o.setRequired(false);
		validateOptions(o);
		TemplateOption o2 = addOption(EXACT_RESPONSE_POSITIVE_VALUE, "Positive value", null, 2);
		validateOptions(o2);
		o2.setRequired(false);
		TemplateOption o3 = addOption(EXACT_RESPONSE_NEGATIVE_VALUE, "Negative value", null, 2);
		validateOptions(o3);
		o3.setRequired(false);
		
		List<TemplateOption> pagetemplates= new ArrayList<TemplateOption>();
		pagetemplates.add(o);
		pagetemplates.add(o2);
		pagetemplates.add(o3);
		optionControl.put(2,pagetemplates);


		//Page 4 - NEAREST
		DSComboChoiceOption p4o1 = new DSComboChoiceOption(this, NEAREST_RESPONSE_PROPERTY_NAME, "Response property", new String[0][0]);
		registerOption(p4o1, null, 3);
		p4o1.setRequired(false);
		validateOptions(p4o1);

		TemplateOption p4o2 = addOption(NEAREST_RESPONSE_POSITIVE_VALUE, "Positive value", null, 3);
		p4o2.setRequired(false);
		validateOptions(p4o2);
		TemplateOption p4o3 = addOption(NEAREST_RESPONSE_NEGATIVE_VALUE, "Negative value", null, 3);
		p4o3.setRequired(false);
		validateOptions(p4o3);
		TemplateOption p4o4 = addOption(NEAREST_TANIMOTO, "Tanimoto (0-1)", null, 3);
		validateOptions(p4o4);
		p4o4.setRequired(false);
		
		List<TemplateOption> page4Templates= new ArrayList<TemplateOption>();
		page4Templates.add(p4o1);
		page4Templates.add(p4o2);
		page4Templates.add(p4o3);
		page4Templates.add(p4o4);
		optionControl.put(3,page4Templates);

		
		//Page 5 - QSAR
		DSComboChoiceOption p5o = new DSComboChoiceOption(this, QSAR_RESPONSE_PROPERTY_NAME, "Response property", new String[0][0]);
		registerOption(p5o, null, 4);
		p5o.setRequired(false);
		validateOptions(p5o);
		
		String[][] qsarType = new String[2][2];
		qsarType[0][0] = "Regression model";
		qsarType[0][1] = "Regression model";
		qsarType[1][0] = "Classification model";
		qsarType[1][1] = "Classification model";
		ComboChoiceOption p5o2 = new ComboChoiceOption(this, QSAR_MODEL_TYPE, "Model type", qsarType);
		registerOption(p5o2, "Classification", 4);
		p5o2.setRequired(false);

		List<TemplateOption> page5Templates= new ArrayList<TemplateOption>();
		page5Templates.add(p5o2);
		optionControl.put(4,page5Templates);

		
		
		//Page 6
//		addOption("wee", "Structural alerts option", null, 5);
		
	}

	@Override
	protected void initializeFields(IFieldData data) {
		String packageName = getFormattedPackageName(data.getId());
		initFields(packageName);
	}
	public void initializeFields(IPluginModelBase model) {
		String packageName = getFormattedPackageName(model.getPluginBase().getId());
		initFields(packageName);
	}

	private void initFields(String packageName) {
			if(packageName == null || packageName.length()<=0) return;

			initializeOption(KEY_NEW_ENDPOINT_ID, packageName + ".endpoint");
			initializeOption(KEY_NEW_ENDPOINT_NAME, getStringOption(KEY_PLUGIN_NAME));
			initializeOption(KEY_PACKAGE_NAME, packageName);
			
//			initializeOption(KEY_RESPONSE_PROPERTY_NAME, r);
	}
	@Override
	protected URL getInstallURL() {
		return Activator.getDefault().getBundle().getEntry("/");
	}

	@Override
	protected String getTemplateDirectory() {
		return super.getTemplateDirectory();
	}

	@Override
	public boolean isDependentOnParentWizard() {
		return true;
	}
	@Override
	public String getSectionId() {
		return "dstemplate";
	}

	@Override
	protected ResourceBundle getPluginResourceBundle() {
		return Platform.getResourceBundle(Activator.getDefault().getBundle());
	}

	@Override
	protected void updateModel(IProgressMonitor monitor) throws CoreException {
		
		System.out.println("WHAT WE GOT:");

		System.out.println(getStringOption(KEY_NEW_ENDPOINT_ID));
		System.out.println(getStringOption(KEY_NEW_ENDPOINT_NAME));
		System.out.println(getStringOption(KEY_NEW_ENDPOINT_DESCRIPTION));

		System.out.println(getOptionByName(KEY_DATA_FILE).getValue());

		System.out.println(getOptionByName(KEY_SELECTED_MODELS).getValue());

		System.out.println(getOptionByName(EXACT_RESPONSE_PROPERTY_NAME).getValue());
		System.out.println(getStringOption(EXACT_RESPONSE_POSITIVE_VALUE));		
		System.out.println(getStringOption(EXACT_RESPONSE_NEGATIVE_VALUE));		

		System.out.println(getStringOption(NEAREST_RESPONSE_NEGATIVE_VALUE));		
		System.out.println(getStringOption(NEAREST_RESPONSE_POSITIVE_VALUE));
		System.out.println(getOptionByName(NEAREST_RESPONSE_PROPERTY_NAME).getValue());
		System.out.println(getStringOption(NEAREST_TANIMOTO));		
		
		System.out.println(getOptionByName(QSAR_RESPONSE_PROPERTY_NAME).getValue());
		System.out.println(getOptionByName(QSAR_MODEL_TYPE).getValue());
		System.out.println(getStringOption("qsar grid: "+ QSAR_LIBSVM_GRID));		//problem
		System.out.println(getStringOption("qsar height: "+ QSAR_SIGNATURE_HEIGHT));		//problem
		
		System.out.println("Now creating plugin...");
		
		
		IPluginBase plugin = model.getPluginBase();
		IPluginModelFactory factory = model.getPluginFactory();

		IPluginExtension extension = createExtension(
					"net.bioclipse.decisionsupport", true);

		//The endpoint
		IPluginElement element = factory.createElement(extension);
		element.setName("endpoint");
		element.setAttribute("id", getStringOption(KEY_NEW_ENDPOINT_ID));
		element.setAttribute("name", getStringOption(KEY_NEW_ENDPOINT_NAME));
		element.setAttribute("description", getStringOption(KEY_NEW_ENDPOINT_DESCRIPTION));
		extension.add(element);

		//===============
		//The exact model
		//===============
		@SuppressWarnings("unchecked")
		List<String> selectedModels=(List<String>) getOptionByName(KEY_SELECTED_MODELS).getValue();
		if (selectedModels.contains("net.bioclipse.ds.matcher.SDFPosNegExactMatchSignatures")){
			element = factory.createElement(extension);
			element.setName("test");
			element.setAttribute("endpoint", getStringOption(KEY_NEW_ENDPOINT_ID));
			element.setAttribute("id", getStringOption(KEY_NEW_ENDPOINT_ID) + ".exact.signatures");
			element.setAttribute("name", getStringOption(KEY_NEW_ENDPOINT_NAME) + " exact matches");
			element.setAttribute("class", "net.bioclipse.ds.matcher.SDFPosNegExactMatchSignatures");
			extension.add(element);

			IPluginElement child = factory.createElement(element);
			child.setName("resource");
			child.setAttribute("name", "file");
			child.setAttribute("path", getOptionByName(KEY_DATA_FILE).getValue().toString());
			element.add(child);

			child = factory.createElement(element);
			child.setName("parameter");
			child.setAttribute("name", "responseProperty");
			child.setAttribute("value", getOptionByName(EXACT_RESPONSE_PROPERTY_NAME).getValue().toString());
			element.add(child);

			child = factory.createElement(element);
			child.setName("parameter");
			child.setAttribute("name", "positiveValue");
			child.setAttribute("value", getStringOption(EXACT_RESPONSE_POSITIVE_VALUE));
			element.add(child);

			child = factory.createElement(element);
			child.setName("parameter");
			child.setAttribute("name", "negativeValue");
			child.setAttribute("value", getStringOption(EXACT_RESPONSE_NEGATIVE_VALUE));
			element.add(child);
			
		}

		if (selectedModels.contains("net.bioclipse.ds.matcher.SDFPosNegNearestMatchFP")){
			 //TODO
		}

		if (selectedModels.contains("qsar.libsvm.atomsign")){
			 //TODO
		}
		 



		plugin.add(extension);

	}


	TemplateOption getOptionByName(String optionName) {
		
		for (int page = 0; page<getPageCount(); page++){
			for (TemplateOption opt : getOptions(page)){
				if (optionName.equals(opt.getName()))
					return opt;
			}
		}

		return null;
		
	}

	
	public String[] getNewFiles() {
		
		//TODO: UPDATE HERE SO BUILD.PROPS is ok
		
		return new String[0];
	}

	public String getUsedExtensionPoint() {
		return "net.bioclipse.decisionsupport";
	}

	public IPluginReference[] getDependencies(String schemaVersion) {
		return createDependencies(
				 "org.eclipse.ui",
				 "org.eclipse.core.runtime",
				 "net.bioclipse.core",
				 "net.bioclipse.ds",
				 "net.bioclipse.cdk.ui.sdfeditor",
				 "net.bioclipse.cdk.business",
				 "net.bioclipse.ds.common",
				 "net.bioclipse.ds.ui",
				 "net.bioclipse.ds.libsvm"
		);
	}

	private IPluginReference[] createDependencies(String... plugins) {
		List<IPluginReference> deps = new ArrayList<IPluginReference>();
		for(String dep:plugins) {
			deps.add(new PluginReference(dep,null,0));
		}
		return deps.toArray(new IPluginReference[deps.size()]);
	}

	protected String getFormattedPackageName(String id) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < id.length(); i++) {
			char ch = id.charAt(i);
			if (buffer.length() == 0) {
				if (Character.isJavaIdentifierStart(ch))
					buffer.append(Character.toLowerCase(ch));
			} else {
				if (Character.isJavaIdentifierPart(ch) || ch == '.')
					buffer.append(ch);
			}
		}
		return buffer.toString().toLowerCase(Locale.ENGLISH);
	}

    @Override
    public void execute(IProject project, IPluginModelBase model,
            IProgressMonitor monitor) throws CoreException {
        IPluginBase pluginBase = model.createPluginBase();
        if (pluginBase instanceof BundlePluginBase) {
            IBundle bundle = ((BundlePluginBase) pluginBase).getBundle();
            bundle.setHeader("Import-Package", "org.apache.log4j");
            model.createPluginBase().getId();
            String packageName = getFormattedPackageName(
                model.getPluginBase().getId()
            );
        }
        super.execute(project, model, monitor);
    }

    
	/**
	 * Validate options given a template option
	 */
	public void validateOptions(TemplateOption source) {
		if (source.isRequired() && source.isEmpty()) {
			flagMissingRequiredOption(source);
			return;
		}
		super.validateOptions(source);
	}
	public void getSelectedModels() {
		// TODO Auto-generated method stub
		
	}

	public List<Integer> getPagesForSelectedOptions() {
		
		@SuppressWarnings("unchecked")
		List<String> o = (List<String>) getOptionByName(DSTemplate.KEY_SELECTED_MODELS).getValue();
		if (o==null) return null; //No options selected

		List<Integer> ret = new ArrayList<Integer>();
		for (String ms : o){
			
			int pagenr = ModelsOption.getPageNumber(ms);
			ret.add(pagenr);

		}

		Collections.sort(ret);
		return ret;
		
	}

	public int getNoMols() {
		return noMols;
	}
	public void setNoMols(int noMols) {
		this.noMols = noMols;
	}
	public List<String> getAvailProps() {
		return availProps;
	}
	public void setAvailProps(List<String> availProps) {
		this.availProps = availProps;

		String[][] props = new String[availProps.size()][2];
		for (int i = 0; i<availProps.size(); i++){
			props[i][0] = availProps.get(i);
			props[i][1] = availProps.get(i);
		}
		
		DSComboChoiceOption co = (DSComboChoiceOption) getOptionByName(EXACT_RESPONSE_PROPERTY_NAME);
		co.setChoices(props);
//		co.selectOptionChoice(props[0][0]);
		co.setValue(props[0][0]);
		validateOptions(co);
		
		co = (DSComboChoiceOption) getOptionByName(NEAREST_RESPONSE_PROPERTY_NAME);
		co.setChoices(props);
//		co.selectOptionChoice(props[0][0]);
		co.setValue(props[0][0]);
		validateOptions(co);
		
		co = (DSComboChoiceOption) getOptionByName(QSAR_RESPONSE_PROPERTY_NAME);
		co.setChoices(props);
		co.setValue(props[0][0]);
		validateOptions(co);
		
	}

}
