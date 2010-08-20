package net.bioclipse.ds.ui.views;

import net.bioclipse.ds.model.result.ExternalMoleculeMatch;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class DSViewerComparator extends ViewerComparator {

	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {

		//Handle comparison of matches with similarities (e.g. tanimoto)
		if (e2 instanceof ExternalMoleculeMatch && e1 instanceof ExternalMoleculeMatch ) {
			ExternalMoleculeMatch em1 = (ExternalMoleculeMatch ) e1;
			ExternalMoleculeMatch em2 = (ExternalMoleculeMatch ) e2;
			if (em1.getSimilarity()>=0 && em2.getSimilarity()>=0){
				if (em1.getSimilarity() > em2.getSimilarity()) return -1;
				if (em1.getSimilarity() == em2.getSimilarity()) return 0;
				if (em1.getSimilarity() < em2.getSimilarity()) return 1;
			}
		}

		//Else use default comparator (Strings)
		return super.compare(viewer, e1, e2);
	}

}
