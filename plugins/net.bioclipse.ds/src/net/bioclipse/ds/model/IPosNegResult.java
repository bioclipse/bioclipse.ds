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
