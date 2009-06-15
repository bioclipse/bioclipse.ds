package net.bioclipse.ds.dblookup.bursi;

import net.bioclipse.ds.business.BaseDSPropertyCalculator;


public class DBExactMatchCalculator extends BaseDSPropertyCalculator{

    @Override
    public String getPropertyName() {
        return "Bursi Exact Match";
    }

    @Override
    public String getTestID() {
        return "dblookup.exact.bursi";
    }
    
}