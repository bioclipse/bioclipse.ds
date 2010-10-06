package net.bioclipse.ds.ahr.calc;

import net.bioclipse.ds.prop.calc.DSConsensusCalculator;

/**
 * 
 * @author ola
 *
 */
public class AHRConsensusCalculator extends DSConsensusCalculator {
    
    

    @Override
    public String getPropertyName() {
        return "AHR Consensus";
    }
    
    @Override
    protected String getTestID() {
        return "ahr.consensus";
    }
    
    protected String getEndpoint(){
        return "net.bioclipse.ds.ahr";
    }


}
