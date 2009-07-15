/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.birt.handlers.model;

import net.bioclipse.ds.model.report.AbstractTestReportModel;
import net.bioclipse.ds.model.report.DSRow;
import net.bioclipse.ds.ui.views.DSView;

import org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow;
import org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

public class SmartsMatchHandler extends ScriptedDataSetEventAdapter{

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
        row.setColumnValue("smarts", thisrow.getParameter("smarts"));
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
	                                  .getTestModel( "smarts.bursi" );

	    if (testmodel==null){
          System.out.println("REPORT MODEL IS NULL");
          return;
	    }

		record=0;
	}


    

}
