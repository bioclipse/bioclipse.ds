package net.bioclipse.ds.business;

import java.util.Collection;
import java.util.Collections;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.business.IPropertyCalculator;


public class DSConsensusCalculator implements IPropertyCalculator<String>{

    public String calculate( ICDKMolecule molecule ) {

        for(IPropertyCalculator<?> calculator:getCalculators()) {
            Object value = calculator.calculate( molecule );
            // store do consesus
        }

        return null;
    }

    public String getPropertyName() {

        // TODO Auto-generated method stub
        return null;
    }

    public String parse( String value ) {

        // TODO Auto-generated method stub
        return null;
    }

    public String toString( Object value ) {

        // TODO Auto-generated method stub
        return null;
    }

    protected Collection<IPropertyCalculator<?>> getCalculators() {
        //  TODO Auto-generated method stub
        return Collections.emptyList();
    }

}
