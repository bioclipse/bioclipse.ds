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
package net.bioclipse.ds.smarts.model.impl;

import net.bioclipse.ds.model.SubStructureMatch;

public class SmartsMatchingTestMatch extends SubStructureMatch{

    private String smartsString;
    private String smartsName;
    
    public String getSmartsString() {
    
        return smartsString;
    }
    public void setSmartsString( String smarts ) {
    
        this.smartsString = smarts;
        
    }

    public String getName() {
        if (smartsName==null) return "NO SMARTS SET";
        return smartsName;
    }

    public String getSmartsName() {
    
        return smartsName;
    }

    public void setSmartsName( String smartsName ) {
    
        this.smartsName = smartsName;
    }

//    @Override
//    public String toString() {
//
//        String ret="SmartsMatchingTestMatch: Name=" + getName() + ", Smarts=" + getSmartsString() + 
//        ", Matching atoms: ";
//        
//        for (int i : getMatchingAtoms()){
//            ret=ret+ i + ",";
//        }
//
//        return ret;
//    }


}
