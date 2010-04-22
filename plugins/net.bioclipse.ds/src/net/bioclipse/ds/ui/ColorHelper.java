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
import java.util.HashMap;
import java.util.Map;

import net.bioclipse.ds.DSConstants;

/**
 * 
 * @author ola
 *
 */
public class ColorHelper {
    
    private static Map<Integer, Color> blueRedScale;

    /**
     * Return a color from blue to red
     * @param resValue A scaled integer between 1 and 100
     * @return
     */
    public static Color getBlueRedColor( int resValue ) {
        
        if (blueRedScale!=null)
            return blueRedScale.get( resValue );
        
        
        //Set up map from scaled value 1-100 to a color
        //Blue is 0000FF, Red is FF0000, 
        blueRedScale=new HashMap<Integer, Color>();
        for (int i=0; i<99; i++){
            int red=255-i*(255/100);
            int blue=i*(255/100);
            Color color=new Color( red, 0, blue, DSConstants.OVAL_ALPHA );
            blueRedScale.put( i+1, color );
        }
        
        return blueRedScale.get( resValue );
    }

}
