/* *****************************************************************************
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


public class TestRunPropertySource extends BasicPropertySource 
                                                implements IPropertySource2{

    protected static final String NAME = "Name";
    protected static final String HITS = "Hits";
    protected static final String STATUS = "Status";
    protected static final String CONSENSUS = "Consensus";

    private Object SimplePropertiesTable[][] =
    {
            { NAME, new TextPropertyDescriptor(NAME,"Name")},
            { HITS, new TextPropertyDescriptor(HITS,"Hits")},
            { STATUS, new TextPropertyDescriptor(STATUS,"Status")},
            { CONSENSUS, new TextPropertyDescriptor(CONSENSUS,CONSENSUS)},
    };

    public TestRunPropertySource(TestRun item) {
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

        addToValueMap(NAME,item.getTest().getName());
        if (item.hasMatches())
            addToValueMap(HITS,""+item.getMatches().size());
        else
            addToValueMap(HITS,"0");
        
        if (item.getStatus()==TestRun.NOT_STARTED)
            addToValueMap(STATUS,"NOT STARTED");
        else if (item.getStatus()==TestRun.RUNNING)
            addToValueMap(STATUS,"RUNNING");
        else if (item.getStatus()==TestRun.FINISHED)
            addToValueMap(STATUS,"FINISHED");
        else if (item.getStatus()==TestRun.ERROR)
            addToValueMap(STATUS,"ERROR");
        else if (item.getStatus()==ITestResult.INFORMATIVE)
            addToValueMap(STATUS,"INFORMATIVE");
        else if (item.getStatus()==TestRun.EXCLUDED)
            addToValueMap(STATUS,"EXCLUDED");
        else
            addToValueMap(STATUS,"N/A");

            addToValueMap(CONSENSUS,item.getConsensusString());
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
