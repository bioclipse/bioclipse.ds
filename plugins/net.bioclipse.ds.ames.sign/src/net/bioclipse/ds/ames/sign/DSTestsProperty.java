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
package net.bioclipse.ds.ames.sign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.ds.model.ITestResult;

/**
 * 
 * @author ola
 *
 */
public class DSTestsProperty {

    Map<String, List<ITestResult>> result;

    
    public DSTestsProperty() {
        result=new HashMap<String, List<ITestResult>>();
    }


    public Map<String, List<ITestResult>> getResult() {
    
        return result;
    }

    
    public void setResult( Map<String, List<ITestResult>> result ) {
    
        this.result = result;
    }

    
}
