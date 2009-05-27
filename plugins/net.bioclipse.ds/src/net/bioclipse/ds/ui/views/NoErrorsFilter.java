package net.bioclipse.ds.ui.views;

import net.bioclipse.ds.model.ErrorResult;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;


public class NoErrorsFilter extends ViewerFilter {

    @Override
    public boolean select( Viewer viewer, Object parentElement, Object element ) {

        if ( element instanceof ErrorResult ) {
            return false;
        }
        return true;
    }

}
