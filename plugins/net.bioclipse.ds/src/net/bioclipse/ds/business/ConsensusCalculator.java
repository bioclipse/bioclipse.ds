package net.bioclipse.ds.business;

import java.util.List;

import net.bioclipse.ds.model.ITestResult;


public class ConsensusCalculator {
    
    public static int calculate(List<Integer> classifications){
        
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

        //If we have informative, should only be one
        else if (numinf>0)
            return ITestResult.INFORMATIVE;

        //If no positive results:
        else if (numpos==0)
            return ITestResult.NEGATIVE;

        //If at least one but equal:
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
