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
package net.bioclipse.ds.ui;

 import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.bioclipse.ds.Activator;
import net.bioclipse.ds.DSConstants;
import net.bioclipse.ds.ui.prefs.DSPrefs;
import net.bioclipse.ds.ui.views.DSView;

import org.eclipse.jface.preference.IPreferenceStore;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.CompactAtom;

/**
 * 
 * @author ola
 *
 */
public class BlueRedColorScaleGenerator implements IGenerator {

    public BlueRedColorScaleGenerator() {

    }
    
    /**
     * Set up the colored M2D circles based on calculated properties
     */
    public IRenderingElement generate( IAtomContainer ac,
                                       RendererModel model ) {

        ElementGroup group = new ElementGroup();

        String currentProperty=DSView.getInstance().getCurrentResultProperty();
        if (currentProperty==null) return group;

        Object o = ac.getProperty( currentProperty );
//        System.out.println("DS-RES:" + ac.hashCode() + "="+o);
        if (o==null) return group;
        
        //Read prefs for rendering params and compute real values
        IPreferenceStore store=Activator.getDefault().getPreferenceStore();
        int circleRadiusPref = store.getInt( DSPrefs.CIRCLE_RADIUS );
        double circleRadius=(double)circleRadiusPref / 10;
        if (circleRadius<=0 || circleRadius >1)
            circleRadius=1.0;

        Map<Integer, Integer> atomResMap = DSResultHelper
            .getResultsFromProperty( (String)o );


        for(int i = 0;i<ac.getAtomCount();i++) {  //Loop over all atoms
            for (Integer ii : atomResMap.keySet()){   //Loop over list of atom indices with a result
                if (ii.intValue()==i){
                    IAtom atom = ac.getAtom( i );

                    int resValue=atomResMap.get( ii );

                    Color drawColor=ColorHelper
                    .getBlueRedColor( resValue );

                    if(drawColor != null){
                        if (model.getRenderingParameter( CompactAtom.class ).getValue()){
                            group.add( new OvalElement( atom.getPoint2d().x,
                                                        atom.getPoint2d().y,
                                                        circleRadius,true, drawColor ));
                        }
                        group.add( new OvalElement( atom.getPoint2d().x,
                                                    atom.getPoint2d().y,
                                                    circleRadius,true, drawColor ));

                    }
                }
            }
        }

        
        return group;
    }

    public List<IGeneratorParameter<?>> getParameters() {
        return Collections.emptyList();
    }
}
