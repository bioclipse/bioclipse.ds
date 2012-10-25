package net.bioclipse.ds.model.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.bioclipse.core.domain.props.BasicPropertySource;
import net.bioclipse.ds.model.ITestResult;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Also display molecule props in propview
 * 
 * @author Ola Spjuth
 *
 */
public class MoleculeResultPropertySource extends BasicPropertySource 
                                                implements IPropertySource2{

    protected static final String NAME = "Name";
    protected static final String TEST = "Test";
    protected static final String CLASSIFICATION = "Classification";

    private Object SimplePropertiesTable[][] =
    {
            { NAME, new PropertyDescriptor(NAME,"Name")},
            { TEST, new PropertyDescriptor(TEST,"Test")},
            { CLASSIFICATION, new PropertyDescriptor(CLASSIFICATION,CLASSIFICATION)},
    };

    public MoleculeResultPropertySource(ExternalMoleculeMatch item) {
        super( item );

        // clean the table
        setProperties(new ArrayList<IPropertyDescriptor>());
        setValueMap(new HashMap<String, String>());

        // setup the new properties

        // the general ones first
        for (int i=0;i<SimplePropertiesTable.length;i++) {        
            // Add each property supported.
            PropertyDescriptor descriptor = (PropertyDescriptor)SimplePropertiesTable[i][1];
            descriptor.setCategory("General");
            getProperties().add((IPropertyDescriptor)descriptor);
        }   

        addToValueMap(NAME,item.getName());
        addToValueMap(TEST,item.getTestRun().getTest().getName());
        if (item.getClassification()==ITestResult.POSITIVE)
            addToValueMap(CLASSIFICATION,"POSITIVE");
        else if (item.getClassification()==ITestResult.NEGATIVE)
            addToValueMap(CLASSIFICATION,"NEGATIVE");
        else if (item.getClassification()==ITestResult.INCONCLUSIVE)
            addToValueMap(CLASSIFICATION,"INCONCLUSIVE");
        else if (item.getClassification()==ITestResult.INFORMATIVE)
            addToValueMap(CLASSIFICATION,"INFORMATIVE");
        else if (item.getClassification()==ITestResult.ERROR)
            addToValueMap(CLASSIFICATION,"ERROR");
        

        //Here comes the match props in their categories
        if (item.getProperties()!=null){
        	for (String category : item.getProperties().keySet()){
        		Map<String, String> catprops = item.getProperties().get(category);

        		for (String name : catprops.keySet()){
    				String value = catprops.get(name);

    				//Treat this as a special property
        			if (name.equals("EXTENDED_IN_BROWSER")){
        				OpenBrowserPropertyDescriptor descriptor = 
        						new OpenBrowserPropertyDescriptor(
        								category+"_EXTENDED", "More information", value);
        				descriptor.setCategory(category);
        				getProperties().add(descriptor);
        				addToValueMap(category+"_EXTENDED", "Click for more information"); //Value is here a local URL
        			}else{
        				PropertyDescriptor descriptor = new PropertyDescriptor(
        						category+"_" + name, name);
        				descriptor.setCategory(category);
        				getProperties().add(descriptor);
        				addToValueMap(category+"_" + name,value);
        			}

        		}


        	}
        }
        

    }    
    
    
    /**
     * Validate strings are non-empty or else add "N/A"
     * @param keyString
     * @param valueString
     */
    private void addToValueMap(String keyString, String valueString) {
      if (keyString==null || keyString=="") return;
      
      if (valueString==null || valueString=="")
        getValueMap().put(keyString,"N/A");
      else
        getValueMap().put(keyString,valueString);
    }

}
