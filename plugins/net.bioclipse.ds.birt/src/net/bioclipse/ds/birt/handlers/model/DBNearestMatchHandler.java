package net.bioclipse.ds.birt.handlers.model;

import net.bioclipse.ds.model.report.AbstractTestReportModel;
import net.bioclipse.ds.model.report.DSRow;
import net.bioclipse.ds.ui.views.DSView;

import org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow;
import org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

public class DBNearestMatchHandler extends ScriptedDataSetEventAdapter{

	public int record;
	AbstractTestReportModel testmodel;


	@Override
	public boolean fetch(IDataSetInstance dataSet, IUpdatableDataSetRow row) {

	     if (testmodel==null)
	          return false;

		try {

			if (testmodel.existsRow(record)){
				DSRow thisrow = testmodel.getRows().get(record);
				row.setColumnValue("structure", thisrow.getStructureData());
        row.setColumnValue("name", thisrow.getParameter("name"));
        row.setColumnValue("tanimoto", thisrow.getParameter("tanimoto"));
        row.setColumnValue("classification", 
                                        thisrow.getParameter("classification"));
				record++;
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	
	
	@Override
	public void open(IDataSetInstance dataSet) {

	    testmodel=DSView.getInstance().waitAndReturnReportModel()
	                                  .getTestModel( "dblookup.nearest.bursi" );

	    if (testmodel==null){
          System.out.println("REPORT MODEL IS NULL");
          return;
	    }

		record=0;
	}


    

}
