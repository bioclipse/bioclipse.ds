/* *****************************************************************************
 * Copyright (c) 2011 Ola Spjuth
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.signatures.handlers;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.domain.IDataset;
import net.bioclipse.ds.signatures.business.ISignaturesManager;

/**
 * 
 * @author ola
 */
public class SparseSignaturesDatasetFromSDF extends AbstractSignaturesDatasetFromSDF{

	@Override
	protected IDataset generateDataset(List<ICDKMolecule> mols, int height,
			String nameProperty, String responseProperty,
			IProgressMonitor monitor) {
		
		ISignaturesManager signatures=net.bioclipse.ds.signatures.Activator.getDefault().getJavaSignaturesManager();
		return signatures.generateSparseDataset(mols, height, nameProperty, responseProperty, monitor);

	}
	
	@Override
	protected String getFileExtension() {
		return "csr";
	}

}
