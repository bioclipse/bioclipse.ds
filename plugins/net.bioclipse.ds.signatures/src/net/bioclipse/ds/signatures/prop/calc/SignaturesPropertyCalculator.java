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
 * 
 * @author ola
 *
 */
public class SignaturesPropertyCalculator 
                            implements IPropertyCalculator<Signatures> {

    Logger logger = Logger.getLogger( SignaturesPropertyCalculator.class );

    public Signatures calculate( ICDKMolecule molecule ) {
        
        ISignaturesManager signatures= Activator.getDefault()
            .getJavaSignaturesManager();

        try {
            return signatures.generate( molecule );
        } catch ( Exception e ) {
            logger.warn( "Failed to calculate Signatures for mol: " + molecule);
        }
        return null;
    }

    public String getPropertyName() {

        return "net.bioclipse.signature";
    }

    public Signatures parse( String value ) {
        String[] values = value.split( "," );
        List<String> signs = Arrays.asList( values );
        for (String s : signs)
            s=s.trim();
        Signatures result = new Signatures(signs);
        return result;
    }

    public String toString( Object value ) {

        if(value instanceof Signatures) {
            Signatures sign = (Signatures)value;
            return sign.toString();
        }
        return "";
    }}
