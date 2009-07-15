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
package net.bioclipse.ds.impl.result;

import java.text.DecimalFormat;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.domain.IMolecule;

/**
 * 
 * @author ola
 *
 */
public class ExternalMoleculeMatch extends SimpleResult{

    private ICDKMolecule matchedMolecule;
    private float similarity;
    private DecimalFormat twoDForm;
    
    public ExternalMoleculeMatch(String name, ICDKMolecule matchedMolecule, 
                                 float similarity, int status) {
        this(name, matchedMolecule, status);
        this.similarity=similarity;
    }

    public ExternalMoleculeMatch(String name, ICDKMolecule matchedMolecule,
                                 int status) {
        super(name,status);
        this.matchedMolecule = matchedMolecule;
        twoDForm = new DecimalFormat("#.##");
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

    
    public float getSimilarity() {
        return similarity;
    }

    
    @Override
    public String getName() {
    
        String name=super.getName();
        if (similarity!=0)
            name=name+ " [tanimoto=" + twoDForm.format( similarity ) +"]";
        return name;
        
    }
    
}
