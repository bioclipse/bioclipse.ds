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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.views.properties.IPropertySource;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * Class to associate an editor with a test
 * @author ola
 *
 */
public class TestRun implements ISubStructure{

    public static final int NOT_STARTED=0x1;
    public static final int RUNNING=0x2;
    public static final int FINISHED=0x3;
    public static final int FINISHED_WITH_ERRORS=0x4;
    
    private IDSTest test;
    private IEditorPart editor;
    private List<ITestResult> matches;
    private int status;
    private boolean excluded;
    
    
    public TestRun() {
        setStatus( NOT_STARTED );
        excluded=false;
    }
    
    public TestRun(IEditorPart editor, IDSTest test) {
        this();
        this.editor=editor;
        this.test=test;
    }

    public boolean isExcluded() {
        return excluded;
    }
    public void setExcluded( boolean excluded ) {
        this.excluded = excluded;
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
        return matches;
    }
    
    public void setMatches( List<ITestResult> matches ) {
        this.matches = matches;
    }

    @Override
    public String toString() {
        String ret="TestRun: Editor=" + editor +", Test=" + test + ", Ststus=" 
                                                                  + getStatus();
        if (matches!=null)
            ret=ret +", matches="+ matches.size();
        else
            ret=ret +", no matches";
        
        return ret;
    }

    public boolean hasMatches() {
        if (matches!=null && matches.size()>0) return true;
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
        return null;
    }

    public Color getHighlightingColor( IAtom atom ) {
        return null;
    }

    public void addMatch( ITestResult result ) {
        if (matches==null)
            matches=new ArrayList<ITestResult>();
        matches.add( result );
    }

}
