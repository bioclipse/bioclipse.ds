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

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * Class to associate an editor with a test
 * @author ola
 *
 */
public class TestRun implements IAdaptable{

    private IDSTest test;
    private IEditorPart editor;
    private List<ITestResult> matches;
    private boolean run;
    
    
    public TestRun() {
        setRun( false );
    }
    
    public TestRun(IEditorPart editor, IDSTest test) {
        this.editor=editor;
        this.test=test;
        setRun( false );
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

    public void setRun( boolean run ) {

        this.run = run;
    }

    public boolean isRun() {

        return run;
    }
    
    @Override
    public String toString() {
        String ret="TestRun: Editor=" + editor +", Test=" + test + ",isRun=" + isRun();
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
}
