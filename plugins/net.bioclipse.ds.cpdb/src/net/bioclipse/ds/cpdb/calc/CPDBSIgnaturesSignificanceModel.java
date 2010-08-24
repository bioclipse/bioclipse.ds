/*******************************************************************************
 * Copyright (c) 2010 Ola Spjuth <ola@bioclipse.net>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth
 ******************************************************************************/
package net.bioclipse.ds.cpdb.calc;

import net.bioclipse.ds.libsvm.SignaturesRegressionTest;

/**
 * The values are for scaling the gradient results above/below a percentile.
 * 
 * @author ola
 *
 */
public class CPDBSIgnaturesSignificanceModel extends SignaturesRegressionTest{

				
	@Override
	public Double getHighPercentileDeriv() {
		return 1.812;
	}

	@Override
	public Double getLowPercentileDeriv() {
		return -1.471;
	}


}
