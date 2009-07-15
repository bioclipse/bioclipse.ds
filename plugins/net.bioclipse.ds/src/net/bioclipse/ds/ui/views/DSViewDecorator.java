/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.ui.views;

import net.bioclipse.ds.Activator;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.TestRun;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;


public class DSViewDecorator implements ILabelDecorator {

    private Image cachedInactiveDSTest;
    private Image cachedErrorInDSTest;

    public Image decorateImage( Image image, Object element ) {

        Image toReturn=null;

        if ( element instanceof IDSTest ) {
            IDSTest dstest = (IDSTest) element;

            //Error in test
            if (dstest.getTestErrorMessage()!=null 
                    && dstest.getTestErrorMessage().length()>1){
                if (cachedErrorInDSTest==null){
                    cachedErrorInDSTest=Activator.
                    getImageDecriptor( "/icons/fatalerror.gif" ).createImage();
                }
                toReturn = cachedInactiveDSTest;
            }

        }
        
        //TestRun defines its own icon
        else if ( element instanceof TestRun ) {
            TestRun tr = (TestRun) element;
            toReturn = tr.getIcon();
        }
        
        return toReturn;
    }

    public String decorateText( String text, Object element ) {

        if ( element instanceof IDSTest ) {
            IDSTest dstest = (IDSTest) element;
            if (dstest.getTestErrorMessage()!=null 
                    && dstest.getTestErrorMessage().length()>1){
                return text + " [" + dstest.getTestErrorMessage() + "]";
            }
            else return text;
        }

        //TestRun provides its own suffix
        else if ( element instanceof TestRun ) {
            TestRun tr = (TestRun) element;
            return text + tr.getSuffix();
        }

        return null;
    }

    public void addListener( ILabelProviderListener listener ) {
    }

    public void dispose() {
    }

    public boolean isLabelProperty( Object element, String property ) {
        return false;
    }

    public void removeListener( ILabelProviderListener listener ) {
    }

}
