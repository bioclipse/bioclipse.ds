package net.bioclipse.ds.model.result;


import java.util.Map;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;

/**
 * 
 * @author ola
 */
public class BlurredAtomColorMatch extends AtomResultMatch{
	
    
    public BlurredAtomColorMatch(String name, int resultStatus) {
		super(name, resultStatus);
	}
    
	public BlurredAtomColorMatch(String name, double resultvalue, int resultStatus) {
		super(name, resultvalue, resultStatus);
	}

	@Override
	public Class<? extends IGeneratorParameter<Boolean>> getGeneratorVisibility() {
    	return (Class<? extends IGeneratorParameter<Boolean>>)GlowGenerator.Visibility.class;
    }

    @Override
    public Class<? extends IGeneratorParameter<Map<Integer, Number>>> getGeneratorAtomMap() {
    	return (Class<? extends IGeneratorParameter<Map<Integer, Number>>>)GlowGenerator.AtomMap.class;
    }
}
