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

import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.model.impl.DSException;

/**
 * A top interface for all tests
 * @author ola
 *
 */
public interface IDSTest {

    public String getId();    
    public void setId( String id );    
    public String getName();    
    public void setName( String name );    
    public String getIcon();
    public void setIcon( String icon );    

    @Deprecated
    public List<ITestResult> runWarningTest(IMolecule molecule, TestRun testrun) throws DSException;

    public List<ITestResult> runWarningTest(IMolecule molecule) throws DSException;

    public void addParameter( String name, String path );
    void setPluginID( String pluginID );
    String getPluginID();
    

}
