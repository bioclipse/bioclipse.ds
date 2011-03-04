package net.bioclipse.ds.sdk.pdewizard;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.pde.ui.IFieldData;
import org.eclipse.pde.ui.templates.ITemplateSection;
import org.eclipse.pde.ui.templates.NewPluginTemplateWizard;
import org.eclipse.pde.ui.templates.TemplateOption;

/**
 * 
 * @author ola
 *
 */
public class NewDSPluginsWizard extends NewPluginTemplateWizard {

	protected IFieldData fData;
	private DSTemplate template;

	public NewDSPluginsWizard() {
	}

	@Override
	public void init(IFieldData data) {
		super.init(data);
		fData = data;
		setWindowTitle("Bioclipse DS from Data wizard");
	}

	@Override
	public ITemplateSection[] createTemplateSections() {
		template=new DSTemplate();
		return new ITemplateSection[] {template};
	}

	@Override
	public String[] getImportPackages() {
		String[] pkgs = {
				"org.apache.log4j"
		};
		return pkgs;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		List<IWizardPage> pages = Arrays.asList(getPages());

		int index = pages.indexOf(page);

		//if first, return second
		if (index==0) return pages.get(index + 1);

		//Get next page depending on model selection
		int nextPage=-1;
		if (index>=1){
			List<Integer> availPages = template.getPagesForSelectedOptions();
			if (availPages==null || availPages.size()==0) return null; //No models selected
			if (index==1) nextPage = availPages.get(0);
			else{
				int currModelIndex = availPages.indexOf(index);
				if (currModelIndex == (availPages.size()-1)){
					return null;	//Last page
				}
				else
					nextPage=availPages.get(currModelIndex+1);
			}
			
			return pages.get(nextPage);
		}


		if (index == pages.size() - 1 || index == -1) {
			// last page or page not found
			return null;
		}
		return (IWizardPage) pages.get(index + 1);
	}

	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		List<IWizardPage> pages = Arrays.asList(getPages());

		int index = pages.indexOf(page);

		//if first, return second
		if (index==0) return null;
		if (index==1) return pages.get(0);

		//Get next page depending on model selection
		int prevPage=-1;
		List<Integer> availPages = template.getPagesForSelectedOptions();
		int currModelIndex = availPages.indexOf(index);
		if (currModelIndex == 0){ //First model page
			prevPage=1; 		  //model selection page is previous
		}
		else
			prevPage=availPages.get(currModelIndex-1);

		return pages.get(prevPage);
		
	}
	
	@Override
	public boolean canFinish() {
		List<IWizardPage> pages = Arrays.asList(getPages());

		// Default implementation is to check if all pages are complete.
        for (int i = 0; i < pages.size(); i++) {
            if (!((IWizardPage) pages.get(i)).isPageComplete()) {
//            	System.out.println("Page " + i + " is not complete!");
				return false;
			}
        }
        return true;
	}

}
