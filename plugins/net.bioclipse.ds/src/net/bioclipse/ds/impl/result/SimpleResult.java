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
package net.bioclipse.ds.impl.result;

import net.bioclipse.ds.Activator;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.TestRun;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertySource;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

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
    
    private TestRun testRun;
    private String name;
    private int classification;
    private String detailedMessage;
    
    
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

    /**
     * No substructure by default. Subclasses may override.
     */
    public IAtomContainer getAtomContainer() {
            return NoNotificationChemObjectBuilder.getInstance().
                                               newAtomContainer();
    }

    /**
     * Red for positive, grren for negative, and yellow else. Sunclasses 
     * may override and/or provide a color per atom.
     */
    public java.awt.Color getHighlightingColor( IAtom atom ) {
        
        if (classification==ITestResult.POSITIVE)
            return java.awt.Color.RED;
        if (classification==ITestResult.NEGATIVE)
            return java.awt.Color.GREEN;
        else
            return java.awt.Color.YELLOW;
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
        else
            return incon_icon;
    }

    
    private void initIcons() {

        pos_icon=Activator.getImageDecriptor( "icons/x-red.gif" ).createImage();
        neg_icon=Activator.getImageDecriptor( "icons/check.gif" ).createImage();
        incon_icon=Activator.getImageDecriptor( "icons/warning16.gif" ).createImage();
        error_icon=Activator.getImageDecriptor( "icons/fatalerror.gif" ).createImage();
        inform_icon=Activator.getImageDecriptor( "icons/bulb.png" ).createImage();
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

}
