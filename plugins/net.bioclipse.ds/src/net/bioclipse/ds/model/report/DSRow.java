/* *****************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
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

  public Map<String, String> getParameters() {
      return columns;
  }



}
