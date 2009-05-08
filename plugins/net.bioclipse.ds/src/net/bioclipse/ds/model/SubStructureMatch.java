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

import org.eclipse.ui.views.properties.IPropertySource;


public class SubStructureMatch extends SimpleResult implements ISubstructureMatch{

    private List<Integer> matchingAtoms;

    public List<Integer> getMatchingAtoms() {
        return matchingAtoms;
    }
    
    public void setMatchingAtoms( List<Integer> matchingAtoms ) {
        this.matchingAtoms = matchingAtoms;
    }
    
    @Override
    public String toString() {
        String ret="SubstructureMath: Name=" + getName() + ", Matching atoms: ";
        for (int i : getMatchingAtoms()){
            ret=ret+ i + ",";
        }
        return ret;
    }
    
    
    public Object getAdapter( Class adapter ) {

        if (adapter.isAssignableFrom(IPropertySource.class)) {
            return new SubStructureMatchPropertySource(this);
        }
        
        return super.getAdapter( adapter );
    }


}
