/* *****************************************************************************
 * Copyright (c) 2010 Ola Spjuth - ospjuth@users.sf.net
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.model.result;

 import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.bioclipse.ds.Activator;
import net.bioclipse.ds.DSPrefs;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;
import org.openscience.cdk.renderer.generators.parameter.AbstractGeneratorParameter;

/**
 * A generator to draw circles with color; 
 * Red=Positive, Greem=Negative, Orange=Inconclusive.
 * 
 * Takes 2 GeneratorParameters:
 *  Boolean visible - defines if generator shopuld be used
 * 		and 
 * 	Map<Integer, Integer> atomMap - maps from atom indices to a result for this atom
 * 
 * @author ola
 *
 */
public class PosNegIncColorGenerator implements IGenerator<IAtomContainer> {

    private static final Logger logger = Logger.getLogger(PosNegIncColorGenerator.class);

    public PosNegIncColorGenerator() {

    }
    
	/**
	 * Adds the ability to turn the generator on/off via a Handler.
	 * False by default.
	 */
    public static class Visibility extends
    AbstractGeneratorParameter<Boolean> {
        public Boolean getDefault() {
            return false;
        }
    }
    private static IGeneratorParameter<Boolean> visible = new Visibility();

	public static void setVisible(Boolean visible1) {
		visible.setValue(visible1);
	}

	/**
	 * Define values for per atom index for coloring
	 */
    public static class AtomMap extends
    AbstractGeneratorParameter<Map<Integer, Integer>> {
        public Map<Integer, Integer> getDefault() {
            return Collections.EMPTY_MAP;
        }
    }
    private static IGeneratorParameter<Map<Integer, Integer>> atomMap = new AtomMap();

	public static void setVisible(Map<Integer, Integer> map) {
		atomMap.setValue(map);
	}

    
    /**
     * Set up the colored M2D circles based on calculated properties
     */
    public IRenderingElement generate( IAtomContainer ac,
                                       RendererModel model ) {

        ElementGroup group = new ElementGroup();

        //If not visible, do not paint
        if (visible.getValue()==false)
        	return group;
        //If no property, do not paint
        if (atomMap.getValue().size()<=0){
        	logger.error("A PosNegColorGenerator is used, but AtomMap was empty.");
        	return group;
        }

        //Read prefs for rendering params and compute real values
        IPreferenceStore store=Activator.getDefault().getPreferenceStore();
        int circleRadiusPref = store.getInt( DSPrefs.CIRCLE_RADIUS );
        double circleRadius=(double)circleRadiusPref / 10;
        if (circleRadius<=0 || circleRadius >1)
            circleRadius=0.4;

        for(int i = 0;i<ac.getAtomCount();i++) {  //Loop over all atoms
            for (Integer ii : atomMap.getValue().keySet()){   //Loop over list of atom indices with a result
                if (ii.intValue()==i){
                    IAtom atom = ac.getAtom( i );

                    int resValue=atomMap.getValue().get( ii );

                    Color drawColor=ColorHelper
                            .getPosNegIncDiscreteColor(resValue );

                    group.add( new OvalElement( atom.getPoint2d().x,
                    		atom.getPoint2d().y,
                    		circleRadius,true, drawColor ));

                }
            }
        }

        
        return group;
    }

    /**
     * We have two parameters: visible (Boolean) defines if this Generator 
     * should draw the object, and property, which defines what should be drawn.
     */
    public List<IGeneratorParameter<?>> getParameters() {
        return Arrays.asList(
                new IGeneratorParameter<?>[] {
                	visible, atomMap
                }
            );
    }
}
