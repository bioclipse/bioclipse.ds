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
package net.bioclipse.ds.model.result;

import java.util.Map;

import net.bioclipse.ds.Activator;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.TestRun;
import net.bioclipse.ds.report.StatusHelper;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertySource;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;

/**
 * A base class for results with a name, a parent testrun, and a status
 * @author ola
 *
 */
public class SimpleResult implements ITestResult{

    private static Image pos_icon;
    private static Image neg_icon;
    private static Image incon_icon;
    private static Image error_icon;
    private static Image inform_icon;
	private static Image empty_icon;
    
    private TestRun testRun;
    private String name;
    private int classification;
    private String detailedMessage;
//    private String resultProperty;  //Used to link a result to a certain property on AC
    
    
    public SimpleResult(String name, int classification) {
        super();
        this.name = name;
        this.classification = classification;
    }

    public TestRun getTestRun() {
        return testRun;
    }
    
    public void setTestRun( TestRun testRun ) {
        this.testRun = testRun;
    }

    public String getName() {
        return name;
    }

    
    public void setName( String name ) {
        this.name = name;
    }

    public Object getAdapter( Class adapter ) {

        if (adapter.isAssignableFrom(IPropertySource.class)) {
            return new SimpleResultPropertySource(this);
        }
        
        return null;
    }

    public Image getIcon(){
        if (pos_icon==null || neg_icon==null || incon_icon==null)
            initIcons();
        
        if (classification==ITestResult.POSITIVE)
            return pos_icon;
        if (classification==ITestResult.NEGATIVE)
            return neg_icon;
        if (classification==ITestResult.ERROR)
            return error_icon;
        if (classification==ITestResult.INFORMATIVE)
            return inform_icon;
        if (classification==ITestResult.INCONCLUSIVE)
            return incon_icon;
        else
        	//All else are empty
            return empty_icon;
    }

    
    private void initIcons() {

//        pos_icon=Activator.getImageDecriptor( "icons/x-red.gif" ).createImage();
//        pos_icon=Activator.getImageDecriptor( "icons/ico_hit.gif" ).createImage();
//        neg_icon=Activator.getImageDecriptor( "icons/check.gif" ).createImage();
        pos_icon=Activator.getImageDecriptor( "icons/target-red.gif" ).createImage();
        neg_icon=Activator.getImageDecriptor( "icons/target-green.gif" ).createImage();
        incon_icon=Activator.getImageDecriptor( "icons/target-orange.gif" ).createImage();
        error_icon=Activator.getImageDecriptor( "icons/fatalerror.gif" ).createImage();
        inform_icon=Activator.getImageDecriptor( "icons/bulb.png" ).createImage();
        empty_icon=Activator.getImageDecriptor( "icons/target-gray.gif" ).createImage();
    }

    public int getClassification() {
        return classification;
    }

    
    public void setClassification( int resultStatus ) {
        this.classification = resultStatus;
    }

    
    public String getDetailedMessage() {
    
        return detailedMessage;
    }

    
    public void setDetailedMessage( String detailedMessage ) {
    
        this.detailedMessage = detailedMessage;
    }

    public String getSuffix(){
        return "";
    }

//    public String getResultProperty() {
//        return resultProperty;
//    }
//
//    public void setResultProperty( String propertyKey ) {
//        resultProperty=propertyKey;
//    }
    
    @Override
    public String toString() {
    	if (testRun!=null){
    		return name + " [test=" + testRun.getTest().getName() + "] Res=" 
    		+ StatusHelper.statusToString( getClassification());
    	}
    	else{
    		return StatusHelper.statusToString( getClassification());
    	}
    }

    /**
     * No default generator visibility.
     */
	@Override
	public Class<? extends IGeneratorParameter<Boolean>> getGeneratorVisibility() {
		return null;
	}

	@Override
	public Class<? extends IGeneratorParameter<Map<Integer, Number>>> getGeneratorAtomMap() {
		return null;
	}

	

}
