package net.bioclipse.ds.birt.handlers;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow;
import org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;
import org.eclipse.birt.report.engine.api.script.instance.IDataSourceInstance;


public class QsarScriptedDataset extends ScriptedDataSetEventAdapter{

	public int recordCount;

	@Override
	public boolean fetch(IDataSetInstance dataSet, IUpdatableDataSetRow row) {

//		IDataSourceInstance ds=dataSet.getDataSource();

		try {
			File myFile=new File("/Users/ola/tmp/img/dog20.gif");
			FileInputStream is= new FileInputStream(myFile);
			long lengthi=myFile.length();
			byte[] imagedata=new byte[(int)lengthi];
			is.read(imagedata);
			is.close();

			if (recordCount<20){
				recordCount++;
				row.setColumnValue("ix", recordCount);
				row.setColumnValue("name", "myname_" + recordCount);
				row.setColumnValue("file", "my/file.mol");
				row.setColumnValue("image", imagedata);
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}

		// TODO Auto-generated method stub
		return super.fetch(dataSet, row);
	}

	@Override
	public void open(IDataSetInstance dataSet) {
		recordCount=0;
	}

}
