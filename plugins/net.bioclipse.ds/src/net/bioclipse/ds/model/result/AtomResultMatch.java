package net.bioclipse.ds.model.result;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author ola
 */
public class AtomResultMatch extends SubStructureMatch{
	
    //AtomNumber > Value
    private Map<Integer, Number> resultMap; 

    public AtomResultMatch(String name, int resultStatus) {
        super( name, resultStatus );
        resultMap=new HashMap<Integer, Number>();
    }
    
    public void setResultMap( Map<Integer, Number> resulMap ) {
        this.resultMap = resulMap;
    }

    public Map<Integer, Number> getResultMap() {
        return resultMap;
    }

    public void putAtomResult( Integer atomToAdd, Number result ) {
        if (resultMap==null) resultMap=new HashMap<Integer, Number>();
        resultMap.put( atomToAdd, result );
        if (!getAtomNumbers().contains( atomToAdd )){
            getAtomNumbers().add( atomToAdd );
        }
        
    }

}
