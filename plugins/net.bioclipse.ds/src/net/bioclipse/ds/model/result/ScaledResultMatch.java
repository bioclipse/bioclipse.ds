package net.bioclipse.ds.model.result;

import java.util.HashMap;
import java.util.Map;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;

import net.bioclipse.ds.DSConstants;

/**
 * 
 * @author ola
 */
public class ScaledResultMatch extends AtomResultMatch{
    

    public ScaledResultMatch(String name, int resultStatus) {
		super(name, resultStatus);
	}

	@Override
	public Class<? extends IGeneratorParameter<Boolean>> getGeneratorVisibility() {
    	return (Class<? extends IGeneratorParameter<Boolean>>)BlueRedColorScaleGenerator.Visibility.class;
    }

    @Override
    public Class<? extends IGeneratorParameter<Map<Integer, Integer>>> getGeneratorAtomMap() {
    	return (Class<? extends IGeneratorParameter<Map<Integer, Integer>>>)BlueRedColorScaleGenerator.AtomMap.class;
    }

}
