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


/**
 * A positive or negative result
 * @author ola
 *
 */
public interface IPosNegResult extends ITestResult{

    public static final int POSITIVE = 0x1;
    public static final int NEGATIVE = 0x2;

    /**
     * Return either IPosNegResult.POSITIVE or IPosNegResult.NEGATIVE
     * @return
     */
    public int getResult();
    
}
