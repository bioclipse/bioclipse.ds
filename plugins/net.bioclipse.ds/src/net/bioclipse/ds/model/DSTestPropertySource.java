/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.model;

import java.util.ArrayList;
import java.util.HashMap;

import net.bioclipse.core.domain.props.BasicPropertySource;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;


public class DSTestPropertySource extends BasicPropertySource 
                                                implements IPropertySource2{

    protected static final String NAME = "Name";
    protected static final String DESCRIPTION = "Description";
    protected static final String PARAMETERS = "Parameters";
    protected static final String EXCLUDED = "Excluded";

    private Object SimplePropertiesTable[][] =
    {
            { NAME, new TextPropertyDescriptor(NAME,"Name")},
            { DESCRIPTION, new TextPropertyDescriptor(DESCRIPTION,"Hits")},
            { PARAMETERS, new TextPropertyDescriptor(PARAMETERS,PARAMETERS)},
    };

    public DSTestPropertySource(AbstractDSTest item) {
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
        addToValueMap(DESCRIPTION,item.getDescription());
        String paramstr="";
        for (String key : item.getParameters().keySet()){
            paramstr=paramstr+ key + item.getParameters().get( key ) +", ";
        }
        paramstr=paramstr.substring( 0, paramstr.length()-2 );
        addToValueMap(PARAMETERS, paramstr);
        if (item.isExcluded())
            addToValueMap(EXCLUDED, "true");
        else
            addToValueMap(EXCLUDED, "false");
            
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
