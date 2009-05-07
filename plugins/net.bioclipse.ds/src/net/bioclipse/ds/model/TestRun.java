package net.bioclipse.ds.model;

import java.util.List;

import net.bioclipse.core.domain.IMolecule;


public class TestRun {

    private IDSTest test;
    private IMolecule mol;
    private List<ITestResult> matches;
    private boolean run;
    
    
    public TestRun() {
        setRun( false );
    }
    
    public TestRun(IMolecule mol, IDSTest test) {
        this.mol=mol;
        this.test=test;
        setRun( false );
    }

    public IDSTest getTest() {
        return test;
    }
    
    public void setTest( IDSTest test ) {
        this.test = test;
    }
    
    public IMolecule getMol() {
        return mol;
    }
    
    public void setMol( IMolecule mol ) {
        this.mol = mol;
    }
    
    public List<ITestResult> getMatches() {
        return matches;
    }
    
    public void setMatches( List<ITestResult> matches ) {
        this.matches = matches;
    }

    public void setRun( boolean run ) {

        this.run = run;
    }

    public boolean isRun() {

        return run;
    }
    
    @Override
    public String toString() {
        String ret="TestRun: mol=" + mol +", Test=" + test + ",isRun=" + isRun();
        if (matches!=null)
            ret=ret +", matches="+ matches.size();
        else
            ret=ret +", no matches";
        
        return ret;
    }

    public boolean hasMatches() {
        if (matches!=null && matches.size()>0) return true;
        return false;
    }
    
}
