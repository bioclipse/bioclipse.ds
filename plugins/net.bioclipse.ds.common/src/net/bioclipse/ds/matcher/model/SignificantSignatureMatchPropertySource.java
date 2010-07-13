package net.bioclipse.ds.matcher.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.bioclipse.core.domain.props.BasicPropertySource;
import net.bioclipse.ds.model.ITestResult;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * 
 * @author ola
 */
public class SignificantSignatureMatchPropertySource  extends BasicPropertySource 
                                                implements IPropertySource2{

    protected static final String NAME = "Name";
    protected static final String TEST = "Test";
    protected static final String ATOMS = "Atoms";
//    protected static final String CENTER_ATOM = "Center Atom";
    protected static final String P_VALUE = "p-value";
    protected static final String ACCURACY = "Accuracy";
    protected static final String HEIGHT = "Height";
    protected static final String CLASSIFICATION = "Classification";

    private Object SimplePropertiesTable[][] =
    {
            { NAME, new TextPropertyDescriptor(NAME,"Name")},
            { TEST, new TextPropertyDescriptor(TEST,"Test")},
            { ATOMS, new TextPropertyDescriptor(ATOMS,"Matching atoms")},
//            { CENTER_ATOM, new TextPropertyDescriptor(CENTER_ATOM,CENTER_ATOM)},
            { P_VALUE, new TextPropertyDescriptor(P_VALUE, P_VALUE)},
            { ACCURACY, new TextPropertyDescriptor(ACCURACY, ACCURACY)},
            { HEIGHT, new TextPropertyDescriptor(HEIGHT,HEIGHT)},
            { CLASSIFICATION, new TextPropertyDescriptor(CLASSIFICATION,CLASSIFICATION)},
    };

    public SignificantSignatureMatchPropertySource(SignificantSignatureMatch item) {
        super( item );

        // clean the table
        setProperties(new ArrayList<IPropertyDescriptor>());
        setValueMap(new HashMap<String, String>());


        // setup the new properties

        // the general ones first
        for (int i=0;i<SimplePropertiesTable.length;i++) {        
            // Add each property supported.
            PropertyDescriptor descriptor;
            descriptor = (PropertyDescriptor)SimplePropertiesTable[i][1];
            descriptor.setCategory("Significant Signature");
            getProperties().add((IPropertyDescriptor)descriptor);
        }   

        addToValueMap(NAME,item.getName());
        addToValueMap(TEST,item.getTestRun().getTest().getName());

        addToValueMap(P_VALUE,""+item.getSignificantSignature().getpValue());
        addToValueMap(ACCURACY,""+item.getSignificantSignature().getAccuracy());
        addToValueMap(HEIGHT,""+item.getSignificantSignature().getHeight());

//        Collect atoms in list and sort it
        List<Integer> atomnumbers=item.getAtomNumbers();
        Collections.sort( atomnumbers );
        
        //Create readable string
        String atoms="";
        for (Integer i : item.getAtomNumbers()){
            atoms=atoms+ (i+1) + ", ";
        }
        if (atoms.length()>=2){
            //remove last comma
            atoms=atoms.substring( 0, atoms.length()-2 );
        }
        addToValueMap(ATOMS,atoms);

        if (item.getClassification()==ITestResult.POSITIVE)
            addToValueMap(CLASSIFICATION,"POSITIVE");
        else if (item.getClassification()==ITestResult.NEGATIVE)
            addToValueMap(CLASSIFICATION,"NEGATIVE");
        else if (item.getClassification()==ITestResult.INCONCLUSIVE)
            addToValueMap(CLASSIFICATION,"INCONCLUSIVE");
        else if (item.getClassification()==ITestResult.ERROR)
            addToValueMap(CLASSIFICATION,"ERROR");

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