package net.bioclipse.ds.impl.result;

import java.util.HashMap;
import java.util.Map;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

import net.bioclipse.ds.DSConstants;

/**
 * 
 * @author ola
 */
public class ScaledResultMatch extends SubStructureMatch{
    
    //AtomNumber > Value
    private Map<Integer, Integer> resultMap; 

    public ScaledResultMatch(String name, int resultStatus) {
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

    /**
     * Serialize resultmap to property on AC
     */
    public void writeResultsAsProperties(IAtomContainer ac, String propertyKey){
        
        if (ac==null) return;
        if (resultMap==null || resultMap.isEmpty()) return;
        
        String prop="";
        for (Integer atomNo : resultMap.keySet()){
            int res=resultMap.get( atomNo );
            if (prop.length()==0){  //first
                prop=prop+ atomNo + "," + res;
            }else{
                prop=prop+ ";" + atomNo + "," + res;
            }
        }
        
        ac.getProperties().put( propertyKey, prop );
        setResultProperty( propertyKey );
        
    }

}
