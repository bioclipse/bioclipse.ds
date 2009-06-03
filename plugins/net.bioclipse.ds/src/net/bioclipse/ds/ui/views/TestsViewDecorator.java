package net.bioclipse.ds.ui.views;

import net.bioclipse.ds.Activator;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.TestRun;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;


public class TestsViewDecorator implements ILabelDecorator {

    private Image cachedInactiveDSTest;

    public Image decorateImage( Image image, Object element ) {

        if ( element instanceof IDSTest ) {
            if (cachedInactiveDSTest==null)
                cachedInactiveDSTest=Activator.
                         getImageDecriptor( "/icons2/box-q_dis.gif" ).createImage();
            
            return cachedInactiveDSTest;
        }
        
        return null;
    }

    public String decorateText( String text, Object element ) {

        if ( element instanceof IDSTest ) {
            IDSTest dstest = (IDSTest) element;
            if (dstest.getTestErrorMessage()!=null 
                    && dstest.getTestErrorMessage().length()>1){
                return text + " [" + dstest.getTestErrorMessage() + "]";
            }
        }
        else if ( element instanceof TestRun ) {
            TestRun tr = (TestRun) element;
            if (tr.getTest().getTestErrorMessage().length()>1){
                return text + " [" + tr.getTest().getTestErrorMessage() + "]";
            }
        }
        
        return null;
    }

    public void addListener( ILabelProviderListener listener ) {

        // TODO Auto-generated method stub

    }

    public void dispose() {

        // TODO Auto-generated method stub

    }

    public boolean isLabelProperty( Object element, String property ) {

        // TODO Auto-generated method stub
        return false;
    }

    public void removeListener( ILabelProviderListener listener ) {

        // TODO Auto-generated method stub

    }

}
