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
import java.util.List;

import net.bioclipse.cdk.domain.ISubStructure;
import net.bioclipse.ds.Activator;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.views.properties.IPropertySource;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

/**
 * Class to associate an editor with a test
 * @author ola
 *
 */
public class TestRun implements ISubStructure{

    public static final int NOT_STARTED=0x1;
    public static final int RUNNING=0x2;
    public static final int FINISHED=0x3;
    public static final int ERROR=0x4;
    public static final int EXCLUDED=0x5;
    
    private static Image checkImg;
    private static Image equalImg;
    private static Image redCrossImg;
    private static Image errorImg;
    private static Image runningImg;
    private static Image notrunImg;
    private static Image excludedImg;

    private IDSTest test;
    private IEditorPart editor;
    private List<ITestResult> results;
    private int status;
    
    
    public TestRun() {
        setStatus( NOT_STARTED );
    }
    
    public TestRun(IEditorPart editor, IDSTest test) {
        this();
        this.editor=editor;
        this.test=test;
    }

    public int getStatus() {
        return status;
    }
    public void setStatus( int status ) {
        this.status = status;
    }

    public IDSTest getTest() {
        return test;
    }
    
    public void setTest( IDSTest test ) {
        this.test = test;
    }
    
    
    public List<ITestResult> getMatches() {
        return results;
    }
    
    public void setMatches( List<ITestResult> matches ) {
        this.results = matches;
    }

    @Override
    public String toString() {
        String ret="TestRun: Editor=" + editor +", Test=" + test + ", Status=" 
                + getStatus() + ", Test errormsg=" + getTest().getTestErrorMessage();
        if (results!=null)
            ret=ret +", matches="+ results.size();
        else
            ret=ret +", no matches";
        
        return ret;
    }

    public boolean hasMatches() {
        if (results!=null && results.size()>0) return true;
        return false;
    }

    
    public IEditorPart getEditor() {
    
        return editor;
    }

    
    public void setEditor( IEditorPart editor ) {
    
        this.editor = editor;
    }

    public Object getAdapter( Class adapter ) {

        if (adapter.isAssignableFrom(IPropertySource.class)) {
            return new TestRunPropertySource(this);
        }
        
        return null;
    }

    public IAtomContainer getAtomContainer() {
        return NoNotificationChemObjectBuilder.getInstance().
        newAtomContainer();
    }

    public java.awt.Color getHighlightingColor( IAtom atom ) {
        return java.awt.Color.YELLOW;
    }

    public void addResult( ITestResult result ) {
        if (results==null)
            results=new ArrayList<ITestResult>();
        results.add( result );
    }
    
    public Image getIcon(){
        if (checkImg==null)
            initIcons();
        
        if (status==FINISHED){
            if (!hasMatches()){
                return checkImg;
            }
            
            //We have matches, calculate consensus
            int consensus=getConsensusStatus();
            if (consensus==ITestResult.POSITIVE)
                return redCrossImg;
            else if (consensus==ITestResult.NEGATIVE)
                return checkImg;
            else
                return equalImg;
            
        }            
        else if (status==ERROR){
            return errorImg;
        }
        else if (status==RUNNING){
            return runningImg;
        }
        else if (status==EXCLUDED){
            return excludedImg;
        }

        return notrunImg;
    }

    /**
     * Calculate a consensus result status for this testrun.
     * Default impl is just a simple voting of all results.<br>
     * <ul>
     * <li>if numpos == 0 : return NEGATIVE
     * <li>else if numpos == numneg : return INCONCLUSIVE
     * <li>else if numpos > numneg : return POSITIVE
     * <li>else return NEGATIVE
     * </ul>
     * 
     * TODO: Hook in custom calculation of consensus.
     * 
     * @return ITestResult.POSITIVE, ITestResult.NEGATIVE, 
     * ITestResult.INCONCLUSIVE, or ITestResult.ERROR
     */
    public int getConsensusStatus() {

        if (results==null)
            return ITestResult.ERROR;

        int numpos=0;
        int numneg=0;
        int numinc=0;
        
        for (ITestResult res : results){
            if (res.getResultStatus()==ITestResult.POSITIVE)
                numpos++;
            else if (res.getResultStatus()==ITestResult.NEGATIVE)
                numneg++;
            else if (res.getResultStatus()==ITestResult.INCONCLUSIVE)
                numinc++;
        }

        //If no positive results:
        if (numpos==0)
            return ITestResult.NEGATIVE;

        //If at least one but equal:
        else if (numpos==numneg)
            return ITestResult.INCONCLUSIVE;

        //If at least one but more pos than neg:
        else if (numpos>numneg)
            return ITestResult.POSITIVE;

        //In all other cases:
        else
            return ITestResult.NEGATIVE;

    }
    
    public String getSuffix(){

        if (status==FINISHED){

            int numpos=0;
            int numneg=0;
            int numinc=0;
            int numerr=0;
            for (ITestResult res : results){
                if (res.getResultStatus()==ITestResult.POSITIVE)
                    numpos++;
                else if (res.getResultStatus()==ITestResult.NEGATIVE)
                    numneg++;
                else if (res.getResultStatus()==ITestResult.INCONCLUSIVE)
                    numinc++;
                else if (res.getResultStatus()==ITestResult.ERROR)
                    numerr++;
            }

            String pospart="";
            String negpart="";
            String incpart="";
            String errpart="";
            if (numpos>0)
                pospart=numpos + " pos";
            if (numneg>0)
                negpart=numneg + " neg";
            if (numinc>0)
                incpart=numinc + " inconcl";
            if (numerr>0)
                errpart=numerr + " neg";

            //Add a comma after pospart if any of the trailing has results
            if (numpos>0){
                if (numneg>0 || numinc>0 || numerr>0)
                    pospart=pospart+", ";
            }
            //Add a comma after negpart if any of the trailing has results
            if (numneg>0){
                if (numinc>0 || numerr>0)
                    negpart=negpart+", ";
            }
            //Add a comma after incpart if any of the trailing has results
            if (numinc>0){
                if (numerr>0)
                    incpart=incpart+", ";
            }
            
            if (numpos>0 || numneg>0 || numinc>0 || numerr>0)
                return " [" + pospart + negpart + incpart + errpart +"]";
            else return "";
            
        }

        else if (status==RUNNING){
            return " [running]";
        }

        else if (status==EXCLUDED){
            return " [excluded]";
        }

        else if (status==ERROR){
            if (getTest().getTestErrorMessage()!=null 
                    && getTest().getTestErrorMessage().length()>0){
                return " [" + getTest().getTestErrorMessage() +" ]";
            }
            else
                return " [Unknown error]";
        }

        //Else, no suffix
        return "";
        
    }

    private void initIcons() {
        
        checkImg=Activator.getImageDecriptor( "icons/check.gif" ).createImage();
        redCrossImg=Activator.getImageDecriptor( "icons/x-red.gif" ).createImage();
        equalImg=Activator.getImageDecriptor( "icons/equal.gif" ).createImage();
        errorImg=Activator.getImageDecriptor( "icons/fatalerror.gif" ).createImage();
        runningImg=Activator.getImageDecriptor( "icons/running.gif" ).createImage();
        notrunImg=Activator.getImageDecriptor( "icons/box-q.gif" ).createImage();
        excludedImg=Activator.getImageDecriptor( "/icons/deactivated.gif" ).createImage();
    }

}
