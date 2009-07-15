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
package net.bioclipse.ds.business;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;


public class ConsensusTest extends AbstractDSTest implements IDSTest{

    private static final Logger logger = Logger.getLogger(ConsensusTest.class);

    public void initialize( IProgressMonitor monitor ) throws DSException {
    }

    @Override
    protected List<? extends ITestResult> doRunTest( ICDKMolecule cdkmol,
                                                     IProgressMonitor monitor ) {

        logger.error("This method: ConsensusTest.runWarningTest should not be called.");
        return null;
    }

    

}
