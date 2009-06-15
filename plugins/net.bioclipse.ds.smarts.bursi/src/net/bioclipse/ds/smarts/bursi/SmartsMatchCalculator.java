package net.bioclipse.ds.smarts.bursi;

import net.bioclipse.ds.business.BaseDSPropertyCalculator;


public class SmartsMatchCalculator extends BaseDSPropertyCalculator{

    @Override
    public String getPropertyName() {
        return "Bursi Toxicophores";
    }

    @Override
    public String getTestID() {
        return "smarts.bursi";
    }
    
}