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
import net.bioclipse.ds.model.Endpoint;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.TestRun;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

/**
 * 
 * @author ola
 *
 */
public class DSViewLabelProvider extends ColumnLabelProvider{

    public Image getImage( Object element ) {

        if ( element instanceof Endpoint ) {
            Endpoint ep = (Endpoint)element;
            return ep.getIcon();
        }
        else if ( element instanceof ITestResult ) {
            ITestResult match = (ITestResult) element;
            return match.getIcon();
        }
        else if ( element instanceof IDSTest ) {
            IDSTest test = (IDSTest)element;
            return test.getIcon();
        }
        else if ( element instanceof TestRun ) {
            TestRun run = (TestRun) element;
            return run.getIcon();
        }

        return null;
    }

    public String getText( Object element ) {

        if ( element instanceof Endpoint ) {
            Endpoint ep = (Endpoint)element;
            return ep.getName();
        }

        else if ( element instanceof ITestResult ) {
            ITestResult match = (ITestResult) element;
            return match.getName() + match.getSuffix();
        }
        else if ( element instanceof TestRun ) {
            TestRun run = (TestRun) element;
            return run.getTest().getName()+ run.getSuffix();
        }
        else if ( element instanceof IDSTest ) {
            IDSTest dstest = (IDSTest) element;
            if (dstest.getTestErrorMessage()!=null 
                    && dstest.getTestErrorMessage().length()>1){
                return dstest.getName() + " [" 
                                           + dstest.getTestErrorMessage() + "]";
            }
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

    /**
     * The color of the text in the treeviewer
     */
    public Color getForeground( Object element ) {
        if ( element instanceof IDSTest ) {
            IDSTest dst = (IDSTest) element;
            if (dst.isExcluded())
                return Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
            else 
                return Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
        }
        //Decorate with no results and no errors
        else if ( element instanceof TestRun ) {
            TestRun tr = (TestRun) element;
            if (tr.getTest().getTestErrorMessage().length()>1){
                return Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED);
            }
            else if (tr.getStatus()==TestRun.EXCLUDED){
                return Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
                
            }
        }

        return Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
    }

    

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipText(java.lang.Object)
     */
    public String getToolTipText(Object element) {
      return element.toString();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipShift(java.lang.Object)
     */
    public Point getToolTipShift(Object object) {
      return new Point(5,5);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipDisplayDelayTime(java.lang.Object)
     */
    public int getToolTipDisplayDelayTime(Object object) {
      return 500;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipTimeDisplayed(java.lang.Object)
     */
    public int getToolTipTimeDisplayed(Object object) {
      return 10000;
    }

}
