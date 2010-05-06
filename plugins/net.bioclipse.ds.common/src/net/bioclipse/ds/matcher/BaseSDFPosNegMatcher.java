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
package net.bioclipse.ds.matcher;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;


/**
 * Class that defines required parameters for a Test based on SDF for 
 * positive/negative classification with a property in the SDF with two distinct
 * values
 * 
 * @author ola
 *
 */
public abstract class BaseSDFPosNegMatcher extends BaseSDFMatcher implements IDSTest{

    private static final Logger logger = Logger.getLogger(BaseSDFPosNegMatcher.class);

    //These parameters are required for all extensions using this class as impl
    private static final String RESPONSE_POS_VALUE_PARAM="positiveValue";
    private static final String RESPONSE_NEG_VALUE_PARAM="negativeValue";

    //Holds the read values from extension. 
    private String posValue;
    private String negValue;

//    @Override
//    public void initialize( IProgressMonitor monitor ) throws DSException {
//        super.initialize( monitor );
//
//        //Read the params from test (defined in extension in manifest)
//        posValue=getParameters().get( RESPONSE_POS_VALUE_PARAM );
//        negValue=getParameters().get( RESPONSE_NEG_VALUE_PARAM );
//    }
    
    /**
     * The parameters that this test requires
     */
    @Override
    List<String> getRequiredParameters() {
        List<String> ret=new ArrayList<String>();

        //The property value for positive prediction
        ret.add(RESPONSE_POS_VALUE_PARAM);

        //The property value for negative prediction
        ret.add(RESPONSE_NEG_VALUE_PARAM);
        return ret;
    }
    
    
    /**
     * This implementation validates response property values as parameters
     * @throws DSException 
     */
    @Override
    void validateResponseValue( Object obj ) throws DSException {

        if (posValue==null || negValue==null){
            posValue=getParameters().get( RESPONSE_POS_VALUE_PARAM );
            negValue=getParameters().get( RESPONSE_NEG_VALUE_PARAM );
        }
        
        if (!( obj instanceof String ))
            throw new DSException("Test " + getId() + " expected a String " +
            		" as response property value but was: " + obj);
        
        String value = (String) obj;
        if (!(posValue.equals( value ) || 
                negValue.equals( value )))
            throw new DSException("Test " + getId() + " expected " +
                                  "'" + negValue + "' or" +
                                  "'" + posValue + "' " +
                                  "as response property value but was: " + obj);
    }

    /**
     * The value of a positive response property in the SDFile
     * @return
     */
    public String getPositivePropertyValue() {
        if (posValue==null)
            posValue=getParameters().get( RESPONSE_POS_VALUE_PARAM );

        return posValue;
    }

    
    /**
     * The value of a negative response property in the SDFile
     * @return
     */
    public String getNegativePropertyValue() {
        if (negValue==null)
            negValue=getParameters().get( RESPONSE_NEG_VALUE_PARAM );

        return negValue;
    }

    /**
     * Internal conclusion calculation for this test
     * @param value
     * @return
     */
    protected int getConclusion( String value ) {

        if (value.equals( getPositivePropertyValue() ))
            return ITestResult.POSITIVE;
        else if (value.equals( getNegativePropertyValue() ))
            return ITestResult.NEGATIVE;

        logger.error("Test " + getId() + "  could not compare " +
            "response from SDFile with test parameters. Expected: " 
                     + getPositivePropertyValue() + " or " + getNegativePropertyValue() 
                     + " but was: " + value 
                     + ". Result set to INCONCLUSIVE.");
        return ITestResult.INCONCLUSIVE;
    }

 
}
