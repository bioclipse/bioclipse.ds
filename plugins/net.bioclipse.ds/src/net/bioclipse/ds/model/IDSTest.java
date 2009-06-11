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

import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.cdk.domain.ISubStructure;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.model.report.AbstractTestReportModel;

/**
 * A top interface for all tests
 * @author ola
 *
 */
public interface IDSTest extends ISubStructure{

    public String getId();    
    public void setId( String id );    
    public String getName();    
    public void setName( String name );    
    public String getIcon();
    public void setIcon( String icon );    
    public void addParameter( String name, String path );
    public void setPluginID( String pluginID );
    public String getPluginID();
    public boolean isExcluded();
    public void setExcluded(boolean excluded);
    public String getTestErrorMessage();
    public void setTestErrorMessage( String testErrorMessage );
    public void setInformative( boolean informative );
    public boolean isInformative();
    public AbstractTestReportModel getReportmodel();
    public void setReportmodel( AbstractTestReportModel reportmodel );

    /**
     * This is the test that is run on a molecule
     * @param molecule IMolecule, input for the test
     * @return List of ITestResults where ITestREsult may be an ErrorResult
     */
    public List<? extends ITestResult> runWarningTest(IMolecule molecule, IProgressMonitor monitor) ;
    public void initialize(IProgressMonitor monitor) throws DSException;

}
