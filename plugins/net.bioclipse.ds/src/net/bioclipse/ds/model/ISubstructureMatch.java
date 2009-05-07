package net.bioclipse.ds.model;

import java.util.List;

/**
 * A substructure result from a test
 * @author ola
 *
 */
public interface ISubstructureMatch extends ITestResult{

    /**
     * A list of atom indices to highlight
     * @return
     */
    public List<Integer> getMatchingAtoms();

}
