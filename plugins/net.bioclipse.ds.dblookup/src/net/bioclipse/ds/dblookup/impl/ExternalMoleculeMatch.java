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
package net.bioclipse.ds.dblookup.impl;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.model.SimpleResult;

/**
 * 
 * @author ola
 *
 */
public class ExternalMoleculeMatch extends SimpleResult{

    private ICDKMolecule matchedMolecule;
    
    public ExternalMoleculeMatch(String name, ICDKMolecule matchedMolecule, 
                                 int status) {
        super(name,status);
        this.matchedMolecule = matchedMolecule;
    }

    public ICDKMolecule getMatchedMolecule() {
        return matchedMolecule;
    }
    
    @Override
    public Object getAdapter( Class adapter ) {
        
        if (adapter==IMolecule.class){
            return matchedMolecule;
        }
    
        return super.getAdapter( adapter );
    }
    
}
