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
import net.bioclipse.ds.model.ErrorResult;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.TestRun;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;


public class TestsViewLabelProvider implements ILabelProvider, IColorProvider{

    public Image getImage( Object element ) {

        ImageDescriptor desc=null;

        if ( element instanceof ITestResult ) {
            ITestResult match = (ITestResult) element;
            if ( match instanceof ErrorResult ) {
                desc=Activator.getImageDecriptor( "icons2/file_del.gif" );
            }
            else if (match.getTestRun().getStatus()==TestRun.FINISHED || 
                    match.getTestRun().getStatus()==TestRun.FINISHED_WITH_ERRORS){
                if (match.getTestRun().getMatches().size()>0){
                    desc=Activator.getImageDecriptor( "icons2/lightning.png" );
                }
                else{
                    desc=Activator.getImageDecriptor( "icons2/check.png" );
                }
            }
        }
        else if ( element instanceof IDSTest ) {
            IDSTest test = (IDSTest)element;
            try{
                desc=Activator.imageDescriptorFromPlugin( test.getPluginID(), test.getIcon() );
            }catch (Exception e){
                desc=null;
            }
        }
        else if ( element instanceof TestRun ) {

            TestRun run = (TestRun) element;
            if (run.getStatus()==TestRun.FINISHED){
                //If we have matches, test has failed
                if (run.hasMatches()){
                    desc=Activator.getImageDecriptor( "icons2/warning16.gif" );
                }

                //If not, all is well, no matches
                else{
                    desc=Activator.getImageDecriptor( "icons2/check.png" );
                }
            }
            
            else if (run.getStatus()==TestRun.FINISHED_WITH_ERRORS){
                desc=Activator.getImageDecriptor( "icons2/delete.gif" );
            }
            else if (run.getStatus()==TestRun.RUNNING){
                desc=Activator.getImageDecriptor( "icons2/refresh2.png" );
            }
            else{
                desc=Activator.getImageDecriptor( "icons2/box-q.gif" );
            }

        }

        if (desc==null)
            return null;

        return desc.createImage();
    }

    public String getText( Object element ) {

        if ( element instanceof ITestResult ) {
            ITestResult match = (ITestResult) element;
            return match.getName();
        }
        else if ( element instanceof TestRun ) {
            TestRun run = (TestRun) element;
            return run.getTest().getName();
        }

        return element.toString();
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


    public Color getBackground( Object element ) {
        return null;
    }

    public Color getForeground( Object element ) {
        if ( element instanceof IDSTest ) {
//            IDSTest dst = (IDSTest) element;
//            if (dst.isExcluded());
                return Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
        }
        return Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
    }

}
