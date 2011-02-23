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
import net.bioclipse.ds.model.result.PosNegIncColorGenerator.AtomMap;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.elements.WedgeLineElement;
import org.openscience.cdk.renderer.elements.WedgeLineElement.Direction;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.CompactAtom;
import org.openscience.cdk.renderer.generators.parameter.AbstractGeneratorParameter;

/**
 * A generator to draw circles with color on a scale between blue > yellow > red.
 * 
 * @author ola
 *
 */
public class BlueRedColorScaleGenerator implements IGenerator<IAtomContainer> {

	private static final Logger logger = Logger.getLogger(BlueRedColorScaleGenerator.class);

	public BlueRedColorScaleGenerator() {

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
	AbstractGeneratorParameter<Map<Integer, Number>> {
		public Map<Integer, Number> getDefault() {
			return Collections.EMPTY_MAP;
		}
	}
	private static IGeneratorParameter<Map<Integer, Number>> atomMap = new AtomMap();

	public static void setVisible(Map<Integer, Number> map) {
		atomMap.setValue(map);
	}


	/**
	 * Set up the colored M2D circles based on calculated properties
	 */
	public IRenderingElement generate( IAtomContainer ac,
			RendererModel model ) {

		ElementGroup group = new ElementGroup();

		if (visible.getValue()==false)
			return group;
		//If no atommap, do not paint
		if (atomMap.getValue().size()<=0){
			logger.error("A BlueRedColorScaleGenerator is used, but AtomMap was empty.");
			return group;
		}

		//Read prefs for rendering params and compute real values
		IPreferenceStore store=Activator.getDefault().getPreferenceStore();
		int circleRadiusPref = store.getInt( DSPrefs.CIRCLE_RADIUS );
		double circleRadius=(double)circleRadiusPref / 10;
		if (circleRadius<=0 || circleRadius >1)
			circleRadius=0.6;


		for(int i = 0;i<ac.getAtomCount();i++) {  //Loop over all atoms
			IAtom atom = ac.getAtom( i );

			Color drawColor=Color.BLACK;	//Error color
			double resValue=0;

			//If we have a result value for this atom, use this
			if (atomMap.getValue().keySet().contains(i)){
				resValue=atomMap.getValue().get( i ).doubleValue();
			}

			//Get rainbow color, green from default value 0
			System.out.print("Atom " + i  + " ");
			drawColor=ColorHelper.getRainbowColor( resValue );

			if(drawColor != null){
				group.add( new OvalElement( atom.getPoint2d().x,
						atom.getPoint2d().y,
						circleRadius,true, drawColor ));

			}
			
			//Get neighbours
//			for (IAtom catom : ac.getConnectedAtomsList(atom)){
//				catom.getPoint2d();  //Coordinate for neighbour atom
//				
//				//Create a vertex centered on atom with 
//				group.add( new WedgeLineElement( atom.getPoint2d().x,
//						atom.getPoint2d().y, catom.getPoint2d().x,
//						catom.getPoint2d().y, 8, true, Direction.toFirst, Color.red ));
//			}

		}


		return group;
	}

	public List<IGeneratorParameter<?>> getParameters() {
		return Arrays.asList(
				new IGeneratorParameter<?>[] {
						visible, atomMap
				}
		);
	}
}
