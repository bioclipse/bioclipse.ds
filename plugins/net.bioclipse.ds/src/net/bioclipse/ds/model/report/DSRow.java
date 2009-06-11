package net.bioclipse.ds.model.report;

import java.util.Map;

public class DSRow {

	private byte[] structureImage;
	private Map<String, String> columns;
	
	
	public DSRow(byte[] structureImage, Map<String, String> columns) {
		super();
		this.structureImage = structureImage;
		this.columns = columns;
	}
	
	public byte[] getStructureData() {
		return structureImage;
	}

	public String getParameter(String param) {
		if (columns.containsKey(param)){
			return columns.get(param);
		}
		return "N/A";
	}


}
