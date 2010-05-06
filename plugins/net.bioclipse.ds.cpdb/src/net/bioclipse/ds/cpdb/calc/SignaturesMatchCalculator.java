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
package net.bioclipse.ds.cpdb.calc;

import org.apache.log4j.Logger;

import net.bioclipse.ds.prop.calc.BaseDSPropertyCalculator;



public class SignaturesMatchCalculator extends BaseDSPropertyCalculator{

    private static final Logger logger = Logger.getLogger(
                                               SignaturesMatchCalculator.class);
    
    @Override
    public String getPropertyName() {
        return "CPDB Signatures";
    }

    @Override
    public String getTestID() {
        return "cpdb.signatures";
    }
    
}
