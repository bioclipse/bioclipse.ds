package net.bioclipse.ds.dblookup.bursi;

import net.bioclipse.ds.business.BaseDSPropertyCalculator;


public class DBNearestMatchCalculator extends BaseDSPropertyCalculator{

    @Override
    public String getPropertyName() {
        return "Bursi Nearest Neighbours";
    }

    @Override
    public String getTestID() {
        return "dblookup.nearest.bursi";
    }
    
}