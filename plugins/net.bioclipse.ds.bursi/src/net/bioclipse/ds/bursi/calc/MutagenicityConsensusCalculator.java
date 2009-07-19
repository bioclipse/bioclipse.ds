package net.bioclipse.ds.bursi.calc;

import net.bioclipse.ds.impl.calc.DSConsensusCalculator;

/**
 * 
 * @author ola
 *
 */
public class MutagenicityConsensusCalculator extends DSConsensusCalculator {
    
    

    @Override
    public String getPropertyName() {
        return "Mutagenicity Consensus";
    }
    
    @Override
    protected String getTestID() {
        return "bursi.consensus";
    }
    
    protected String getEndpoint(){
        return "net.bioclipse.ds.mutagenicity";
    }


}
