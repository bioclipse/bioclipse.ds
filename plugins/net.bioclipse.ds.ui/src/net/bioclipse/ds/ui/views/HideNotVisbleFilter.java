package net.bioclipse.ds.ui.views;

import net.bioclipse.ds.model.Endpoint;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.TestRun;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;


public class HideNotVisbleFilter extends ViewerFilter {

    @Override
    public boolean select( Viewer viewer, Object parentElement, Object element ) {

        if ( element instanceof IDSTest ) {
            IDSTest test = (IDSTest) element;
            if (!test.isVisible())
                return false;
        }
        else if ( element instanceof TestRun ) {
            TestRun tr = (TestRun)element;
            if (!tr.getTest().isVisible())
                return false;
        }       
        else if ( element instanceof Endpoint ) {
        	Endpoint ep = (Endpoint)element;
            if (ep.getTests()==null || ep.getTests().size()<=0)
                return false;
        }       
        return true;
    }

}
