package net.bioclipse.ds.ui;

import java.util.List;

import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.cdk.jchempaint.view.ChoiceGenerator;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;

public class GeneratorHelper {

    public static void turnOffAllExternalGenerators(JChemPaintEditor jcp) {
    	//Switch off all other DS-generators!
    	List<IGenerator<IAtomContainer>> generators = ChoiceGenerator.getGeneratorsFromExtension();

    	RendererModel model = jcp.getWidget().getRenderer2DModel();

    	for(IGenerator generator: generators) {
    		List<IGeneratorParameter<?>> params = generator.getParameters();
    		if(params.isEmpty()) continue;
    		for (IGeneratorParameter param : params){
    			if (param.getDefault() instanceof Boolean) {
    				IGeneratorParameter<Boolean> bp= (IGeneratorParameter<Boolean>)param;
    				model.set(bp.getClass(), false);
    			}
    		}
    	}				
    }

}
