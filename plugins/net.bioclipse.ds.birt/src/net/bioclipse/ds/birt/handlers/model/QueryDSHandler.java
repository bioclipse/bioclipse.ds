package net.bioclipse.ds.birt.handlers.model;

import net.bioclipse.ds.model.report.DSSingleReportModel;
import net.bioclipse.ds.ui.views.DSView;

import org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

public class QueryDSHandler extends ScriptedDataSetEventAdapter{

	DSSingleReportModel testmodel;
	boolean isParsed;

	@Override
	public boolean fetch(IDataSetInstance dataSet, IUpdatableDataSetRow row) {

	    if (!isParsed){
	        
	        try {
	            row.setColumnValue("compoundName", testmodel.getCompoundName());
	            row.setColumnValue("mw", ""+ testmodel.getMw());
	            row.setColumnValue("SMILES", ""+ testmodel.getSMILES());
	        } catch ( ScriptException e ) {
	            e.printStackTrace();
	        }
	        isParsed=true;
	        return true;
	    }
	    return false;
	}
	
	@Override
	public void open(IDataSetInstance dataSet) {

	    testmodel=DSView.getInstance().waitAndReturnReportModel();

	    if (testmodel==null){
          System.out.println("REPORT MODEL IS NULL");
          return;
	    }
	   isParsed=false; 
	}


    

}
