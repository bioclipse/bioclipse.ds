package net.bioclipse.ds.cpdb.calc;

import net.bioclipse.ds.prop.calc.DSConsensusCalculator;

/**
 * 
 * @author ola
 *
 */
public class CarcinogenicityConsensusCalculator extends DSConsensusCalculator {
    
    

    @Override
    public String getPropertyName() {
        return "Carcinogenicity Consensus";
    }
    
    @Override
    protected String getTestID() {
        return "cpdb.consensus";
    }
    
    protected String getEndpoint(){
        return "net.bioclipse.ds.carcinogenicity";
    }


}
