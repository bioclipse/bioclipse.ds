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

import net.bioclipse.cdk.domain.CDKMoleculePropertySource;

import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.views.properties.IPropertySource;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

public class SimpleResult implements ITestResult{

    private TestRun testRun;
    private String name;
    
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
        return null;
    }

    /**
     * No substructure color by default. Subclasses may override.
     */
    public Color getHighlightingColor( IAtom atom ) {
        return null;
    }

    

}
