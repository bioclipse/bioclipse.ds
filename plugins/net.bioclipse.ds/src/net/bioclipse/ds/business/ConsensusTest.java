package net.bioclipse.ds.business;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;


public class ConsensusTest extends AbstractDSTest implements IDSTest{

    private static final Logger logger = Logger.getLogger(ConsensusTest.class);

    public void initialize( IProgressMonitor monitor ) throws DSException {
    }

    public List<? extends ITestResult> runWarningTest( IMolecule molecule,
                                                       IProgressMonitor monitor ) {
        
        logger.error("This method: ConsensusTest.runWarningTest should not be called.");

        return null;
    }
    

}
