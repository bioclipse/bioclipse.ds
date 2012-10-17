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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import net.bioclipse.ds.DSConstants;
import net.bioclipse.ds.model.ITestResult;

/**
 * 
 * @author ola
 *
 */
public class ColorHelper {
 
    private static final Logger logger = Logger.getLogger(ColorHelper.class);
    
    private static Map<Integer, Color> blueRedScale;

    /**
     * Return a color from blue to red via yellow
     * @param resValue A double between -1 and 1
     * @return
     */
    public static Color getRainbowColor( double resValue ) {

    	if (resValue<-1 || resValue>1){
    		logger.error("Value is " + resValue + 
    		" but must be between -1 and 1");
    		return Color.BLACK;
    	}
    	
        if (blueRedScale!=null)
            return blueRedScale.get( resValue );
        
        double red = 0;
        double green = 0;
        double blue = 0;
        
        if (resValue<0){
        	red=0;
        	if (resValue<(-0.5)){
        		blue=255;
        		green=(255*2*(resValue+1));
        	}
        	else {
        		green=255;
        		blue=255-2*255*(resValue+0.5);
        	}
        }else{
        	blue=0;
        	if (resValue<0.5){
        		green=255;
        		red=(255*2*resValue);
        	}
        	else {
        		red=255;
        		green=2*255*(1-resValue);
        	}

        }
          

//        double red = Math.round(resValue*255);
//        if (red<0) red=0;
//
//        double green=0;
//        if (resValue<0)
//        	green=Math.round(resValue*255+255);
//        else
//        	green=Math.round(-resValue*255+255);
//
//        double blue = Math.round(-resValue*255);
//        if (blue<0) blue=0;
//        
        Color color = new Color( (int)red, (int)green, (int)blue, 
        		DSConstants.OVAL_ALPHA );

//        System.out.println("Value=" + resValue + " genrated color: " + color);

        return color;

    }

    /**
     * Return a color based on POS/NEG/INCONCLUSIVE
     * @param resValue An integer as defined in ITestResult
     * @return
     */
    public static Color getPosNegIncDiscreteColor( int resValue ) {
    	
    	if (resValue==ITestResult.POSITIVE)
    		return Color.RED;
    	if (resValue==ITestResult.NEGATIVE)
    		return Color.GREEN;

    	return Color.ORANGE;
        
    }
    
    public static void main(String[] args) {
		
    	for (double i = -1; i < 1; i=i+0.1){
    		System.out.println(i + " scales to: " + getRainbowColor(i));
    	}
    	
	}

}
