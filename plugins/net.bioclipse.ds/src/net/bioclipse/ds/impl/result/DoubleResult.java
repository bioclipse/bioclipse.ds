package net.bioclipse.ds.impl.result;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * 
 * @author ola
 *
 */
public class DoubleResult extends SimpleResult {
    
    DecimalFormat formatter;
    
    private double value;

    public DoubleResult(String name, Double value, int classification) {
        super( name, classification );
        this.value=value;
        
        DecimalFormatSymbols sym=new DecimalFormatSymbols();
        sym.setDecimalSeparator( '.' );
        formatter = new DecimalFormat("0.000", sym);
    }
    
    @Override
    public String getName() {
        return super.getName() + ": " + formatter.format( getValue() );
    }
    
    public double getValue() {
        return value;
    }
    
    public void setValue( double value ) {
        this.value = value;
    }

}
