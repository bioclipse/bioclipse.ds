package net.bioclipse.ds.dblookup.bursi;

import net.bioclipse.ds.business.BaseDSPropertyCalculator;


public class DBNearestMatchCalculator extends BaseDSPropertyCalculator{

    @Override
    public String getPropertyName() {
        return "net.bioclipse.ds.dblookup.bursi.dbnearest";
    }

    @Override
    public String getTestID() {
        return "dblookup.nearest.bursi";
    }
    
}