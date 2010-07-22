/* *****************************************************************************
 * Copyright (c) 2009-2010 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.report;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import net.bioclipse.ds.Activator;
import net.bioclipse.ds.model.ITestResult;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

/**
 * 
 * @author ola
 *
 */
public class StatusHelper {

    private static Image questImg;
    private static Image warnImg;
    private static Image crossImg;
    private static Image checkImg;
    private static Image wheelImg;

    public static String statusToString(int status){
        
        if (status==ITestResult.POSITIVE)
            return "POSITIVE";
        else if (status==ITestResult.NEGATIVE)
            return"NEGATIVE";
        else if (status==ITestResult.INCONCLUSIVE)
            return"INCONCLUSIVE";
        else if (status==ITestResult.INFORMATIVE)
            return"INFORMATIVE";
        else if (status==ITestResult.ERROR)
            return"ERROR";
        else
            return"N/A";
        
    }

    public static int stringToStatus(String value){

        if (value.equalsIgnoreCase( "positive" ))
            return ITestResult.POSITIVE;
        else if (value.equalsIgnoreCase( "negative" ))
            return ITestResult.NEGATIVE;
        else if (value.equalsIgnoreCase( "INCONCLUSIVE" ))
            return ITestResult.INCONCLUSIVE;
        else if (value.equalsIgnoreCase( "INFORMATIVE" ))
            return ITestResult.INFORMATIVE;

        //Default: error
        return ITestResult.ERROR;
        
    }

    public static Image statusToImageData( int consensus ) {

        if (checkImg==null)
            readImages();
        
        if (consensus==ITestResult.POSITIVE)
            return crossImg;
        else if (consensus==ITestResult.NEGATIVE)
            return checkImg;
        else if (consensus==ITestResult.INCONCLUSIVE)
            return questImg;

        return warnImg;
    }
    
    private static void readImages(){
        

        //Initialize and cache consensus images
        try {
            wheelImg= readImage( "/icons48/wheel.png" );
            questImg= readImage(  "/icons48/question.png" );
            warnImg= readImage( "/icons48/warn.png" );
            crossImg= readImage( "/icons48/cross.png" );
            checkImg= readImage( "/icons48/check.png" );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    private static BufferedImage readImage(String relativePath) throws IOException{
        
        //Get absolute path
        Bundle bundle = org.eclipse.core.runtime.Platform.getBundle(
                                                           Activator.PLUGIN_ID); 
        URL url = FileLocator.find(bundle, 
                                   new Path(relativePath), null);
        String absPath = FileLocator.toFileURL(url).getPath();
        
		FileInputStream fis=null;
		try {
			fis = new FileInputStream(new File(absPath));
			BufferedImage image = ImageIO.read(fis);
			fis.close();
			return image;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;


//        //Read the absolute path as file
//        File myFile=new File(absPath);
//        FileInputStream is;
//            is = new FileInputStream(myFile);
//        long lengthi=myFile.length();
//        byte[] imagedata=new byte[(int)lengthi];
//        is.read(imagedata);
//        is.close();
//        
//        return imagedata;

    }
    
}
