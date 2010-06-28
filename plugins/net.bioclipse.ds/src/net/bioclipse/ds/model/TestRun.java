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
import java.util.List;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.ISubStructure;
import net.bioclipse.ds.Activator;

import org.eclipse.help.IContext2;
import org.eclipse.help.IHelpResource;
import org.eclipse.jface.viewers.IColorProvider;
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
public class TestRun implements ISubStructure, IColorProvider, IContext2{

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
    private ICDKMolecule mol;
    private List<ITestResult> results;
    private int status;
    private ICDKMolecule molecule;
    
    
    public TestRun() {
        setStatus( NOT_STARTED );
    }
    
    public TestRun(ICDKMolecule mol, IDSTest test) {
        this();
        this.mol=mol;
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

    /**
     * This gets serialized in the moltable cells 
     */
    @Override
    public String toString() {
        
        //Serialize consensus
//        TestRun tr = results.get( 0 ).getTestRun();
//        if (results.get( 0 ).getName().equalsIgnoreCase( "consensus"))
//            return tr.getConsensusString();
//        else
        if (results!=null)
            return getTest().getName() + " - " + getConsensusString() + " [" + results.size() + " hits]";
        else
            return getTest().getName() + " - " + getConsensusString();
    }

    public boolean hasMatches() {
        if (results!=null && results.size()>0) return true;
        return false;
    }

    public Object getAdapter( Class adapter ) {

        if (adapter.isAssignableFrom(IPropertySource.class)) {
            return new TestRunPropertySource(this);
        }
        
        return null;
    }

    public IAtomContainer getAtomContainer() {
        return NoNotificationChemObjectBuilder.getInstance().
        newInstance(IAtomContainer.class);
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
     * 
     * TODO: Hook in custom calculation of consensus.
     * 
     * @return ITestResult.POSITIVE, ITestResult.NEGATIVE, 
     * ITestResult.INCONCLUSIVE, or ITestResult.ERROR
     */
    public int getConsensusStatus() {

        List<Integer> ints=new ArrayList<Integer>();
        if (results!=null){
            for (ITestResult res : results){
                ints.add( res.getClassification() );
            }
        }
        
        //Use the consensuscalculator from the test
        
        return getTest().getConsensusCalculator().calculate( ints );

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

        if (status==FINISHED){

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
//        redCrossImg=Activator.getImageDecriptor( "icons/x-red.gif" ).createImage();
        redCrossImg=Activator.getImageDecriptor( "icons/warn2.gif" ).createImage();
        equalImg=Activator.getImageDecriptor( "icons/equal.gif" ).createImage();
        errorImg=Activator.getImageDecriptor( "icons/fatalerror.gif" ).createImage();
        runningImg=Activator.getImageDecriptor( "icons/running.gif" ).createImage();
        notrunImg=Activator.getImageDecriptor( "icons/test_case.gif" ).createImage();
        excludedImg=Activator.getImageDecriptor( "/icons/exclude.png" ).createImage();
//        excludedImg=Activator.getImageDecriptor( "/icons/deactivated.gif" ).createImage();
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
            return new Color(Display.getCurrent(), 211, 66, 8);

        else if (getConsensusStatus()==ITestResult.NEGATIVE){
            return new Color(Display.getCurrent(), 100, 188, 61);
        }

        else if (getConsensusStatus()==ITestResult.INCONCLUSIVE)
            return new Color(Display.getCurrent(), 148, 153, 248);

        return new Color(Display.getCurrent(), 247, 227, 0);
    }

    public Color getForeground( Object element ) {
        return null;
    }

    /*
     * BELOW is for CONTEXT
     */
    
    public String getText() {
        return null;
    }
    
    public String getStyledText() {
        return getTest().getDescription();
    }
    
    
    public IHelpResource[] getRelatedTopics() {

        //If no web page availabel, return null
       if (getTest().getHelppage()==null || getTest().getHelppage().length()<=0)
            return null;
        
        IHelpResource res=new IHelpResource(){

            public String getHref() {
                return getTest().getPluginID() + "/"+getTest().getHelppage();
            }
            public String getLabel() {
              return getTest().getName();
            }
          };
        return new IHelpResource[]{res};
    }
    

    public String getCategory( IHelpResource topic ) {
        //TODO: implement
        return null;
    }
    
    public String getTitle() {
        return getTest().getName();
    }
    
}
