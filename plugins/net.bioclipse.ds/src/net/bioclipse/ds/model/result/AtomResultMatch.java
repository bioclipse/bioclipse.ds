package net.bioclipse.ds.model.result;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author ola
 */
public class AtomResultMatch extends SubStructureMatch{
	
    //AtomNumber > Value
    private Map<Integer, Integer> resultMap; 

    public AtomResultMatch(String name, int resultStatus) {
        super( name, resultStatus );
        resultMap=new HashMap<Integer, Integer>();
    }
    
    public void setResultMap( Map<Integer, Integer> resulMap ) {
        this.resultMap = resulMap;
    }

    public Map<Integer, Integer> getResultMap() {
        return resultMap;
    }

    public void putAtomResult( Integer atomToAdd, Integer result ) {
        if (resultMap==null) resultMap=new HashMap<Integer, Integer>();
        resultMap.put( atomToAdd, result );
        if (!getAtomNumbers().contains( atomToAdd )){
            getAtomNumbers().add( atomToAdd );
        }
        
    }

}
