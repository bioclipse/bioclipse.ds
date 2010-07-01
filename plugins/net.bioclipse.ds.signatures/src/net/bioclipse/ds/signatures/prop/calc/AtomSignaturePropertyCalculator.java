/* *****************************************************************************
 * Copyright (c) 2010 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/

package net.bioclipse.ds.signatures.prop.calc;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.business.IPropertyCalculator;
import net.bioclipse.ds.signatures.Activator;
import net.bioclipse.ds.signatures.business.ISignaturesManager;

/**
 * Abstract class for calculating atom signatures properties for a molecule.
 * 
 * @author ola
 *
 */
public abstract class AtomSignaturePropertyCalculator 
                            implements IPropertyCalculator<AtomSignatures> {

    Logger logger = Logger.getLogger( AtomSignaturePropertyCalculator.class );

    public AtomSignatures calculate( ICDKMolecule molecule ) {
        
        ISignaturesManager signatures= Activator.getDefault()
            .getJavaSignaturesManager();

        try {
            return signatures.generate(molecule, getHeight());
        } catch ( Exception e ) {
            logger.error( "Failed to calculate AtomSignatures height " + getHeight() + " for mol: " + molecule);
        }
        return null;
    }

    /**
     * Split Comma-separated signatures into a String
     */
    public AtomSignatures parse( String value ) {

    	String[] lst = value.split(",");
    	List<String> list = Arrays.asList(lst);
        return new AtomSignatures(list);
    }

    /**
     * Serialize to comma-separated string
     */
    public String toString( Object value ) {
    	return ((AtomSignatures)value).toString();
    }

    /**
     * The hight of the signatures
     * @return
     */
    protected abstract int getHeight();

}
