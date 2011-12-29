package net.bioclipse.ds.model.result;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author ola
 */
public class AtomResultMatch extends SubStructureMatch implements IDoubleResult{
	
    //AtomNumber > Value
    private Map<Integer, Number> resultMap;
    private double value;

    public AtomResultMatch(String name, int resultStatus) {
        super( name, resultStatus );
        resultMap=new HashMap<Integer, Number>();
    }

    public AtomResultMatch(String name, double resultvalue, int resultStatus) {
        this( name, resultStatus );
        this.value=resultvalue;
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

	@Override
	public double getValue() {
		return value;
	}

	@Override
	public void setValue(double value) {
		this.value=value;
	}

}
