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
package net.bioclipse.ds.report;

import java.util.List;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.ds.model.Endpoint;

/**
 * 
 * @author ola
 *
 */
public class DSSingleReportModel {

	ICDKMolecule queryMol;
	List<Endpoint> endpoints;

	public DSSingleReportModel(ICDKMolecule queryMol, List<Endpoint> endpoints) {
		super();
		this.queryMol = queryMol;
		this.endpoints = endpoints;
	}

	public ICDKMolecule getQueryMol() {
		return queryMol;
	}

	public void setQueryMol(ICDKMolecule queryMol) {
		this.queryMol = queryMol;
	}

	public List<Endpoint> getEndpoints() {
		return endpoints;
	}

	public void setEndpoints(List<Endpoint> endpoints) {
		this.endpoints = endpoints;
	}

	
	
}
