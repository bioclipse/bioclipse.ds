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

import org.apache.log4j.Logger;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.business.IPropertyCalculator;
import net.bioclipse.ds.signatures.Activator;
import net.bioclipse.ds.signatures.business.ISignaturesManager;

/**
 * 
 * @author ola
 *
 */
public class MolecularSignaturePropertyCalculator 
                            implements IPropertyCalculator<String> {

    Logger logger = Logger.getLogger( MolecularSignaturePropertyCalculator.class );

    public String calculate( ICDKMolecule molecule ) {
        
        ISignaturesManager signatures= Activator.getDefault()
            .getJavaSignaturesManager();

        try {
            return signatures.generateMoleculeSignature( molecule );
        } catch ( Exception e ) {
            logger.warn( "Failed to calculate Signatures for mol: " + molecule);
        }
        return null;
    }

    public String getPropertyName() {

        return "Molecular Signature";
    }

    public String parse( String value ) {
        return value;
    }

    public String toString( Object value ) {

        if(value instanceof String) {
            return (String) value;
        }
        return "";
    }
    
}
