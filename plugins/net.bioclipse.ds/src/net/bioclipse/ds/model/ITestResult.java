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
 * A base interface for all test results
 * @author ola
 *
 */
public interface ITestResult {

    
    /**
     * The name of this match to be displayed in e.g. UI
     * @return
     */
    public String getName();

    public void setName( String name );

    /**
     * The parent TestRun. Required in e.g. TreeViewer.
     * @return
     */
    public TestRun getTestRun();

    public void setTestRun( TestRun testRun );

}
