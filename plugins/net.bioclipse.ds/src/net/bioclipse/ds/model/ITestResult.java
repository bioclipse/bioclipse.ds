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
    
    /**
     * The parent TestRun. Required in e.g. TreeViewer.
     * @return
     */
    public TestRun getTestRun();

}
