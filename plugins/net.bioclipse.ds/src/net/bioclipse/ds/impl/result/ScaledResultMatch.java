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
    
    private Map<IAtom, Integer> resultMap; 

    public ScaledResultMatch(String name, int resultStatus) {
        super( name, resultStatus );
    }
    
    public void setResultMap( Map<IAtom, Integer> resulMap ) {
        this.resultMap = resulMap;
    }

    public Map<IAtom, Integer> getResultMap() {
        return resultMap;
    }

    public void putAtomResult( IAtom atomToAdd, Integer result ) {
        if (resultMap==null) resultMap=new HashMap<IAtom, Integer>();
        resultMap.put( atomToAdd, result );
        
    }

    /**
     * Serialize resultmap to property on AC
     */
    public void writeResultsAsProperties(IAtomContainer ac, String propertyKey){
        
        if (ac==null) return;
        if (resultMap==null || resultMap.isEmpty()) return;
        
        String prop="";
        for (IAtom atom : resultMap.keySet()){
            int no=ac.getAtomNumber( atom );
            int res=resultMap.get( atom );
            if (prop.length()==0){  //first
                prop=prop+ no + "," + res;
            }else{
                prop=prop+ ";" + no + "," + res;
            }
        }
        
        ac.getProperties().put( propertyKey, prop );
        setResultProperty( propertyKey );
        
    }

}
