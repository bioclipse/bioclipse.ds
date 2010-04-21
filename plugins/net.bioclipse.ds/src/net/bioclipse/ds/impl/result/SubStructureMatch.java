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
package net.bioclipse.ds.impl.result;

import net.bioclipse.cdk.domain.ISubStructure;

import org.eclipse.ui.views.properties.IPropertySource;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;


public class SubStructureMatch extends SimpleResult implements ISubStructure{

    public SubStructureMatch(String name, int resultStatus) {
        super( name, resultStatus );
    }

    private IAtomContainer ac;
    
    @Override
    public String toString() {
        String ret="SubstructureMatch: Name=" + getName() + ", Matching atoms: ";
        for (IAtom atom : getAtomContainer().atoms()){
            ret=ret+ getAtomContainer().getAtomNumber( atom ) + ",";
        }
        return ret;
    }
    
    
    public Object getAdapter( Class adapter ) {

        if (adapter.isAssignableFrom(IPropertySource.class)) {
            return new SubStructureMatchPropertySource(this);
        }else
            if( adapter.isAssignableFrom( ISubStructure.class )) {
                return this;
            }
        
        return super.getAdapter( adapter );
    }


    /**
     * Return the IAtomContainer with the atoms in the substructure
     */
    public IAtomContainer getAtomContainer() {
        return ac;
    }
    
    public void setAtomContainer( IAtomContainer ac ) {
        this.ac = ac;
    }
    
}
