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

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.ISubStructure;
import net.bioclipse.ds.Activator;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
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
public class TestRun implements ISubStructure, IColorProvider{

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
    private static Image informationImg;


    private IDSTest test;
    private IEditorPart editor;
    private List<ITestResult> results;
    private int classification;
    private ICDKMolecule molecule;
    
    
    public TestRun() {
        setClassification( NOT_STARTED );
    }
    
    public TestRun(IEditorPart editor, IDSTest test) {
        this();
        this.editor=editor;
        this.test=test;
    }

    public int getClassification() {
        return classification;
    }
    public void setClassification( int classification ) {
        this.classification = classification;
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

    /**
     * This gets serialized in the moltable cells 
     */
    @Override
    public String toString() {
        
        //No results = negative
        if (results==null || results.size()<=0){
            return "NEGATIVE";
        }

        //Serialize consensus
        TestRun tr = results.get( 0 ).getTestRun();
        return tr.getConsensusString() + " [" + results.size() + " hits]";

        
//        String ret="TestRun: Editor=" + editor +", Test=" + test + ", Status=" 
//                + getStatus() + ", Test errormsg=" + getTest().getTestErrorMessage();
//        if (results!=null)
//            ret=ret +", matches="+ results.size();
//        else
//            ret=ret +", no matches";
//        
//        return ret;
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
        
        if (classification==FINISHED){
            
            //We have matches, calculate consensus
            int consensus=getConsensusStatus();
            if (consensus==ITestResult.POSITIVE)
                return redCrossImg;
            else if (consensus==ITestResult.NEGATIVE)
                return checkImg;
            else if (consensus==ITestResult.ERROR)
                return errorImg;
            else if (consensus==ITestResult.INFORMATIVE)
                return informationImg;
            else
                return equalImg;
            
        }            
        else if (classification==ERROR){
            return errorImg;
        }
        else if (classification==RUNNING){
            return runningImg;
        }
        else if (classification==EXCLUDED){
            return excludedImg;
        }

        return notrunImg;
    }

    /**
     * Calculate a consensus result status for this testrun.
     * Default impl is just a simple voting of all results.<br>
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
        int numerr=0;
        int numinf=0;
        
        for (ITestResult res : results){
            if (res.getClassification()==ITestResult.POSITIVE)
                numpos++;
            else if (res.getClassification()==ITestResult.NEGATIVE)
                numneg++;
            else if (res.getClassification()==ITestResult.INCONCLUSIVE)
                numinc++;
            else if (res.getClassification()==ITestResult.ERROR)
                numerr++;
            else if (res.getClassification()==ITestResult.INFORMATIVE)
                numinf++;
        }

        //If at least one but more pos than neg:
        if (numerr>numneg && numerr>numpos)
            return ITestResult.ERROR;

        //If we have informative, should only be one
        else if (numinf>0)
            return ITestResult.INFORMATIVE;

        //If no positive results:
        else if (numpos==0)
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
    
    public String getConsensusString(){
        int cons=getConsensusStatus();
        
        if (cons==ITestResult.POSITIVE)
            return "POSITIVE";
        else if (cons==ITestResult.NEGATIVE)
            return"NEGATIVE";
        else if (cons==ITestResult.INCONCLUSIVE)
            return"INCONCLUSIVE";
        else if (cons==ITestResult.INFORMATIVE)
            return"INFORMATIVE";
        else if (cons==ITestResult.ERROR)
            return"ERROR";
        else
            return"N/A";
        
    }
    
    public String getSuffix(){

        if (classification==FINISHED){

            int numpos=0;
            int numneg=0;
            int numinc=0;
            int numerr=0;
            for (ITestResult res : results){
                if (res.getClassification()==ITestResult.POSITIVE)
                    numpos++;
                else if (res.getClassification()==ITestResult.NEGATIVE)
                    numneg++;
                else if (res.getClassification()==ITestResult.INCONCLUSIVE)
                    numinc++;
                else if (res.getClassification()==ITestResult.ERROR)
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
                errpart=numerr + " err";

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

        else if (classification==RUNNING){
            return " [running]";
        }

        else if (classification==EXCLUDED){
            return " [excluded]";
        }

        else if (classification==ERROR){
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
        informationImg=Activator.getImageDecriptor( "icons/information.gif" ).createImage();

    }

    
    public ICDKMolecule getMolecule() {
    
        return molecule;
    }

    
    public void setMolecule( ICDKMolecule molecule ) {
    
        this.molecule = molecule;
    }


    public Color getBackground( Object element ) {

        if (getConsensusStatus()==ITestResult.POSITIVE)
            return new Color(Display.getCurrent(), 255, 0, 0);

        if (getConsensusStatus()==ITestResult.NEGATIVE)
            return new Color(Display.getCurrent(), 0, 255, 0);

        return new Color(Display.getCurrent(), 0, 255, 255);
    }

    public Color getForeground( Object element ) {
        return null;
    }

}
