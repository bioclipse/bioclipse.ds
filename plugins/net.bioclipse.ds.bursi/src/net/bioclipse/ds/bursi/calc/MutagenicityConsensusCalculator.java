package net.bioclipse.ds.bursi.calc;

import net.bioclipse.ds.impl.calc.DSConsensusCalculator;


public class MutagenicityConsensusCalculator extends DSConsensusCalculator {

    @Override
    protected String getTestID() {

        return "bursi.consensus";
    }

}
