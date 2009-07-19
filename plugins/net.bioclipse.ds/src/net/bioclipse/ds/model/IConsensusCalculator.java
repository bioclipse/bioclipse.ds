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


/**
 * 
 * An interface for calculators that are able to calculate a consensus 
 * ITestResult.int from a list of ITestResults.int
 * 
 * @author ola
 *
 */
public interface IConsensusCalculator {

    public int calculate( List<Integer> classifications );

    String getId();

    void setId( String id );

    String getName();

    void setName( String name );

    String getDescription();

    void setDescription( String description );
    
}
