package net.bioclipse.ds;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.PropertySheetPage;

public class PropertyViewHelper {

	public static void collapseAll(){
		IViewPart[] views = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViews();
		for (IViewPart vp : views){
			if (vp instanceof PropertySheet) {
				PropertySheet propsheet = (PropertySheet) vp;
				PropertySheetPage page = (PropertySheetPage) propsheet.getCurrentPage();
				if (page.getControl() instanceof Tree) {
					Tree tree = (Tree) page.getControl();
					TreeItem[] items = tree.getItems();
					for (TreeItem item : items)
						item.setExpanded(false);
				}
			}
		}
	}

}
