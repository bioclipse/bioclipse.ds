package net.bioclipse.ds.model.result;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.IRenderingVisitor;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator.Scale;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;
import org.openscience.cdk.renderer.generators.parameter.AbstractGeneratorParameter;

import net.bioclipse.cdk.renderer.blur.*;

public class GlowGenerator implements IGenerator<IAtomContainer> {

	public static class Visibility extends
	AbstractGeneratorParameter<Boolean> {
		public Boolean getDefault() {

            return true;
		}
	}

    private IGeneratorParameter<Boolean> visible = new Visibility();
	
	public static class AtomMap extends
	AbstractGeneratorParameter<Map<Integer, Number>> {
		public Map<Integer, Number> getDefault() {
			return Collections.emptyMap();
		}
	}
	
    private IGeneratorParameter<Map<Integer, Number>> atomMap = new AtomMap();

    public static class StencilSize extends AbstractGeneratorParameter<Integer> {

        @Override
        public Integer getDefault() {

            return 10;
        }
    }

    private IGeneratorParameter<Integer> stencilSize = new StencilSize();
    
    public enum Coloring {
        NEAR_NEIGHBOR, STARDROP, DEFAULT
    }

    public static class ColoringType extends
                    AbstractGeneratorParameter<Coloring> {

        @Override
        public Coloring getDefault() {

            return Coloring.DEFAULT;
        }
    }

    private IGeneratorParameter<Coloring> coloringType = new ColoringType();

	@Override
	public IRenderingElement generate(IAtomContainer ac, RendererModel model) {

        if ( !model.get( Visibility.class ) ) {
            return EMPTY;
        }
        double radius=20;
        double scale =3;
		Map<Integer,Number> atomMap = model.get(AtomMap.class);
		if (atomMap==null || atomMap.size()<=0)
            return EMPTY;

		// for each atom
		ElementGroup group = new ElementGroup();
		ElementGroup grayGroup = new ElementGroup();
		ElementGroup colorGroup = new ElementGroup();
		group.add(grayGroup);
		group.add(colorGroup);
		Color defaultColor = new Color(0xDCDCDC);
//		Color defaultColor = new Color(0x0CDC0C);
		
		for(int i = 0;i< ac.getAtomCount();i++) {
			IAtom atom = ac.getAtom(i);
			Number value = atomMap.get(i);
			// generate gray circle
			grayGroup.add(circleElement(atom, model, defaultColor,radius*scale));
			// generate color circle based on value
			if(value!=null) {
                Color color = getColoring(model.get(ColoringType.class), value.doubleValue() );
				colorGroup.add(circleElement(atom, model, color,radius));
			}
		}
		
        return new BlurRenderingElement( group, model.get( StencilSize.class ) );
	}
	
	
	static final IRenderingElement EMPTY = new IRenderingElement() {
		
		@Override
		public void accept(IRenderingVisitor visitor) {
		}
	};
	
    Color getColoring( Coloring type, double value ) {

        assert (value <= 1 && value >= -1);
        
        //Transform value to be between 0 and 1, since implementation was written for this
        double unitValue = (value +1 )/2;
        
        switch ( type ) {
            case NEAR_NEIGHBOR:
                return nearNeighborColoring( unitValue);
            case STARDROP:
                return stardropColoring( unitValue );
            case DEFAULT:
            default:
                return defaultColoring( unitValue );
        }
    }


	// base color (220,220,220) 0xDCDCDC
	Color nearNeighborColoring(double value) {
		float red = (float) (((0.0-220.0/255.0)/(0.0-1.0))*(value-1.0)+220.0/255.0);
		float blue = (float) (((255.0/255.0-220.0/255.0)/(0.0-1.0))*(value-1.0)+220.0/255.0);
		float green = (float) (((0.0-220.0/255.0)/(0.0-1.0))*(value-1.0)+220.0/255.0);
		Color c = new Color(red, green, blue);		

//		System.out.println("Value: " + value + " > " + c.toString());
		
		return c;
	}
	
	// base color (0,255,0) 0x00FF00
	Color stardropColoring(double value) {
		float green = 0.0f;
		float red = 0.0f;
		float blue= 0.0f;
		if (value<0.25){
			red = 0.0f;
			blue = 254;
			green = (float) (((0-255.0/255.0)/(0.0-0.25))*(value-0.25)+255.0/255.0);
		}
		else if ( (value<0.5) && (value>=0.25) ){
			red = 0.0f;
			blue = (float) (((255.0/255.0-0.0/255.0)/(0.25-0.5))*(value-0.5)+0.0/255.0);
			green = 255.0f;
		}
		else if ( (value<0.75) && (value>=0.5) ){
			red = (float) (((0.0/255.0-255.0/255.0)/(0.5-0.75))*(value-0.75)+255.0/255.0);
			blue = 0.0f;
			green = 255.0f;
		}
		else{
			red = 255.0f;
			blue = 0.0f;
			green = (float) (((255.0-0.0/255.0)/(0.75-1.0))*(value-1.0)+0.0/255.0);						
		}
		return new Color((int)red, (int)green, (int)blue);
	}
	
	Color defaultColoring(double value) {
		float green = 0.0f;
		float red = 0.0f;
		float blue= 0.0f;
		if (value<0.5){
			red = (float) (((0-220.0/255.0)/(0.0-0.5))*(value-0.5)+220.0/255.0);
			blue = (float) (((255.0/255.0-220.0/255.0)/(0.0-0.5))*(value-0.5)+220.0/255.0);
			green = (float) (((0-220.0/255.0)/(0.0-0.5))*(value-0.5)+220.0/255.0);
		}
		else{
			red = (float) (((255.0/255.0-220.0/255.0)/(1.0-0.5))*(value-0.5)+220.0/255.0);
			blue = (float) (((0.0-220.0/255.0)/(1.0-0.5))*(value-0.5)+220.0/255.0);
			green = (float) (((0.0-220.0/255.0)/(1.0-0.5))*(value-0.5)+220.0/255.0);						
		}
		return new Color(red, green, blue);
	}
	
	
	IRenderingElement circleElement(IAtom atom,RendererModel model,Color color, double radiusIN) {
//		final double RADIUS = 20;
		boolean filled = true;
		Point2d p = atom.getPoint2d();
		
		double radius = radiusIN / model.get(Scale.class);
		
		return new OvalElement(p.x,p.y,radius,filled,color);
	}
	
	public List<IGeneratorParameter<?>> getParameters() {
		return Arrays.asList(
				new IGeneratorParameter<?>[] {
						visible, atomMap,stencilSize,coloringType
				}
		);
	}
}
