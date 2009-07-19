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
package net.bioclipse.ds.impl.cons;

import java.util.List;

import net.bioclipse.ds.model.ITestResult;


/**
 * This implementation follows the following rules:
 * <ol>
 *         <li>if #err > #pos && #err > #neg >> ERROR
 *         <li>if #incon >=pos && #incon >= #neg >> INCONCLUSIVE
 *         <li>if #pos == #neg >> INCONCLUSIVE
 *         <li>if #pos > #neg >> POSITIVE
 *         <li>else >> NEGATIVE
 * </ol>
 * 
 * @author ola
 *
 */
public class MajorityVote extends AbstractConsensusCalculator{
    
    public int calculate(List<Integer> classifications){
        
        if (classifications==null)
            return ITestResult.ERROR;

        int numpos=0;
        int numneg=0;
        int numinc=0;
        int numerr=0;
        int numinf=0;
        
        for (Integer res : classifications){
            if (res==ITestResult.POSITIVE)
                numpos++;
            else if (res==ITestResult.NEGATIVE)
                numneg++;
            else if (res==ITestResult.INCONCLUSIVE)
                numinc++;
            else if (res==ITestResult.ERROR)
                numerr++;
            else if (res==ITestResult.INFORMATIVE)
                numinf++;
        }

        //If at least one but more pos than neg:
        if (numerr>numneg && numerr>numpos)
            return ITestResult.ERROR;

        else if (numinc >= numpos && numinc >= numneg)
            return ITestResult.INCONCLUSIVE;

        else if (numpos==numneg)
            return ITestResult.INCONCLUSIVE;

        //If at least one but more pos than neg:
        else if (numpos>numneg)
            return ITestResult.POSITIVE;

        //In all other cases:
        else
            return ITestResult.NEGATIVE;
        
    }    
    
}
