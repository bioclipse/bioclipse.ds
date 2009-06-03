package net.bioclipse.ds.ui.views;

import net.bioclipse.ds.Activator;
import net.bioclipse.ds.model.ErrorResult;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.TestRun;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;


public class TestsViewDecorator implements ILabelDecorator {

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
                    getImageDecriptor( "/icons2/cancel2.png" ).createImage();
                }
                toReturn = cachedInactiveDSTest;
            }

            //Inactive test
            else {
                if (cachedInactiveDSTest==null){
                    cachedInactiveDSTest=Activator.
                    getImageDecriptor( "/icons2/box-q_dis.gif" ).createImage();
                }
                toReturn = cachedInactiveDSTest;
            }
        }
        
        //TestRun has error -> decorate
        else if ( element instanceof TestRun ) {
            TestRun tr = (TestRun) element;
            if (tr.getTest().getTestErrorMessage().length()>1){
                if (cachedErrorInDSTest==null){
                    cachedErrorInDSTest=Activator.
                    getImageDecriptor( "/icons2/cancel2.png" ).createImage();
                }
                toReturn=cachedErrorInDSTest;
            }
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
        }

        //Decorate with no results and no errors
        else if ( element instanceof TestRun ) {
            TestRun tr = (TestRun) element;
            if (tr.getTest().getTestErrorMessage().length()>1){
                return text + " [" + tr.getTest().getTestErrorMessage() + "]";
            }
            if (tr.hasMatches()){
                int hits=0;
                int errors=0;
                for (ITestResult hit : tr.getMatches()){
                    if (!( hit instanceof ErrorResult )) {
                        hits++;
                    }else{
                        errors++;
                    }
                }
                if (hits>0 && errors<=0)
                    return tr.getTest().getName() + " [" + hits+" hits]";
                else if (hits>0 && errors>0)
                    return tr.getTest().getName() + " [" + hits+" hits, " + errors + " errors]";
                else if (hits<=0 && errors>0)
                    return tr.getTest().getName() + " [" + errors + " errors]";

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
