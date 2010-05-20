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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertySource;

/**
 * 
 * @author ola
 *
 */
public class SubStructureMatch extends SimpleResult{

    private List<Integer> atomNumbers;
    
    public SubStructureMatch(String name, int resultStatus) {
        super( name, resultStatus );
        atomNumbers=new ArrayList<Integer>();
    }

    @Override
    public String toString() {
        String ret="SubstructureMatch: Name=" + getName() + ", Matching atoms: ";
        for (Integer i : atomNumbers){
            ret=ret+ (i+1) + ",";
        }
        return ret;
    }
    
    
    public Object getAdapter( Class adapter ) {

        if (adapter.isAssignableFrom(IPropertySource.class)) {
            return new SubStructureMatchPropertySource(this);
        }
        return super.getAdapter( adapter );
    }

    public List<Integer> getAtomNumbers() {
        return atomNumbers;
    }

    
    public void setAtomNumbers( List<Integer> atomNumbers ) {
        this.atomNumbers = atomNumbers;
    }


}
