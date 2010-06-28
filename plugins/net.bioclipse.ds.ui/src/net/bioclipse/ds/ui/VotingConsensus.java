package net.bioclipse.ds.ui;

import java.util.List;

import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.TestRun;

public class VotingConsensus {


    
    /**
     * A simple consensus voting.
     * TODO: Implement custom solutions for this.
     * @return
     */
    public static int getConsensusFromTestRuns(List<TestRun> activeTestRuns) {

        int numpos=0;
        int numneg=0;
        int numinc=0;

        if (activeTestRuns==null)
            return ITestResult.INCONCLUSIVE;
            
        for (TestRun tr : activeTestRuns){
            //Only count non-informative and included testruns
            if ((!(tr.getTest().isInformative())) 
                    &&  (!(tr.getTest().isExcluded()))){
                
                if (tr.getStatus()==TestRun.FINISHED){
                    if (tr.getConsensusStatus()==ITestResult.POSITIVE)
                        numpos++;
                    else if (tr.getConsensusStatus()==ITestResult.NEGATIVE)
                        numneg++;
                    else if (tr.getConsensusStatus()==ITestResult.INCONCLUSIVE)
                        numinc++;
                }

            }
        }

        //If no positive results:
        if (numpos==0)
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
