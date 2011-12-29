/*******************************************************************************
 * Copyright (c) 2010 Ola Spjuth
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth
 ******************************************************************************/
package net.bioclipse.ds.model.result;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.openscience.cdk.interfaces.IAtom;

/**
 * Class to assign a Double value per atom in a substructure.
 * @author ola
 *
 */
public class SubStructureDoubleResult extends SubStructureMatch implements IDoubleResult {

    
    private Map<IAtom, Double> atomValues; 
    DecimalFormat formatter;
    
    private double value;
    

    /**
     * Constructor. resultStatus is optional.
     */
    public SubStructureDoubleResult(String name, int resultStatus) {
        super( name, resultStatus );
    }

    public Map<IAtom, Double> getAtomValues() {
        return atomValues;
    }
    public void setAtomValues( Map<IAtom, Double> atomValues ) {
        this.atomValues = atomValues;
    }

    /**
     * Assign a value to an IAtom
     */
    public void putAtomValue( IAtom atomToAdd, Double value ) {
        if (atomValues==null) atomValues=new HashMap<IAtom, Double>();
        atomValues.put( atomToAdd, value );
    }

	@Override
	public double getValue() {
		return value;
	}

	@Override
	public void setValue(double value) {
		this.value=value;
	}

}
