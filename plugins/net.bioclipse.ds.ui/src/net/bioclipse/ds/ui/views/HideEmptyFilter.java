package net.bioclipse.ds.ui.views;

import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.TestRun;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * A filter to hide testresults which has no results
 * @author ola
 *
 */
public class HideEmptyFilter extends ViewerFilter {

	@Override
	public boolean select( Viewer viewer, Object parentElement, Object element ) {

		if ( element instanceof TestRun ) {
			TestRun tr = (TestRun)element;
			if (tr.getStatus()==TestRun.FINISHED && tr.getConsensusStatus()==ITestResult.EMPTY)
				return false;
			if (tr.getStatus()==TestRun.FINISHED && tr.getConsensusStatus()==ITestResult.INCONCLUSIVE)
				return false;
		}
		return true;
	}

}
