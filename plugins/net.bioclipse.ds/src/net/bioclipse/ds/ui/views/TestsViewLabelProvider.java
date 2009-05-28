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
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;


public class TestsViewLabelProvider implements ILabelProvider{

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
//            else if(match.getTestRun().getStatus()==TestRun.NOT_STARTED){
//                desc=Activator.getImageDecriptor( "icons2/box-q.gif" );
//            }
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
            if (run.hasMatches()){
                int hits=0;
                int errors=0;
                for (ITestResult hit : run.getMatches()){
                    if (!( hit instanceof ErrorResult )) {
                        hits++;
                    }else{
                        errors++;
                    }
                }
                if (hits>0 && errors<=0)
                    return run.getTest().getName() + " [" + hits+" hits]";
                else if (hits>0 && errors>0)
                    return run.getTest().getName() + " [" + hits+" hits, " + errors + " errors]";
                else if (hits<=0 && errors>0)
                    return run.getTest().getName() + " [" + errors + " errors]";
                
            }
            else
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

}
