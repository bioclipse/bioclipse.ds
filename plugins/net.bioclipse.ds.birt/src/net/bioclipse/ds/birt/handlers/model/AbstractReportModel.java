package net.bioclipse.ds.birt.handlers.model;

import net.bioclipse.ds.report.AbstractTestReportModel;
import net.bioclipse.ds.report.DSRow;
import net.bioclipse.ds.ui.views.DSView;

import org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow;
import org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

/**
 * An abstract base class that reads and populates Birt dataset rows 
 * from a local ReportModel extracted from DSView on open.
 * @author ola
 *
 */
public abstract class AbstractReportModel extends ScriptedDataSetEventAdapter {

    public int record;
    AbstractTestReportModel testmodel;

    public AbstractReportModel() {
        super();
    }

    public int getRecord() {
        return record;
    }

    public void setRecord( int record ) {
        this.record = record;
    }

    public AbstractTestReportModel getTestmodel() {
        return testmodel;
    }

    public void setTestmodel( AbstractTestReportModel testmodel ) {
        this.testmodel = testmodel;
    }

    /**
     * Wait for DSView to complete running and get the reportmodel for 
     * the subclassing test from there.
     */
    @Override
    public void open( IDataSetInstance dataSet ) {
    
    	    testmodel=DSView.getInstance().waitAndReturnReportModel()
    	                                  .getTestModel( getTestID() );
    
    	    if (testmodel==null){
              System.out.println("Report model is null for test: " + getTestID());
              return;
    	    }
    
    		record=0;
    	}
    
    /**
     * Populate the row with data from the DSRow from teh reportmodel
     */
    @Override
    public boolean fetch(IDataSetInstance dataSet, IUpdatableDataSetRow row) {

        if (getTestmodel()==null)
            return false;
        
      try {

        if (getTestmodel().existsRow(record)){
          DSRow thisrow = getTestmodel().getRows().get(record);
          
          //If we have a structure, set it
          if (thisrow.getStructureData()!=null){
              row.setColumnValue("structure", thisrow.getStructureData());
          }
          
          //Set all parameters
          for (String param : thisrow.getParameters().keySet()){
              row.setColumnValue(param, thisrow.getParameter(param));
          }
          setRecord( getRecord() + 1 );
          return true;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      return false;
    }

    /**
     * Subclasses must provide a ID for the test
     * @return
     */
    protected abstract String getTestID();
        

}