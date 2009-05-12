package net.bioclipse.ds.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.openscience.cdk.interfaces.IAtom;

import net.bioclipse.core.domain.props.BasicPropertySource;


public class SubStructureMatchPropertySource extends BasicPropertySource 
                                                implements IPropertySource2{

    protected static final String NAME = "Name";
    protected static final String TEST = "Test";
    protected static final String ATOMS = "Atoms";

    private Object SimplePropertiesTable[][] =
    {
            { NAME, new TextPropertyDescriptor(NAME,"Name")},
            { TEST, new TextPropertyDescriptor(TEST,"Test")},
            { ATOMS, new TextPropertyDescriptor(ATOMS,"Matching atoms")}
    };

    public SubStructureMatchPropertySource(SubStructureMatch item) {
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
            descriptor.setCategory("General");
            getProperties().add((IPropertyDescriptor)descriptor);
        }   

        addToValueMap(NAME,item.getName());
        addToValueMap(TEST,item.getTestRun().getTest().getName());

        String atoms="";
        for (IAtom atom : item.getAtomContainer().atoms()){
            atoms=atoms+ item.getAtomContainer().getAtomNumber( atom ) + ", ";
        }
        if (atoms.length()>=2){
            //remove last comma
            atoms=atoms.substring( 0, atoms.length()-2 );
        }
        addToValueMap(ATOMS,atoms);

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
