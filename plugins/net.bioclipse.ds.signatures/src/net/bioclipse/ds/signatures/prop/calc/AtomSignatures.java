package net.bioclipse.ds.signatures.prop.calc;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to hold a list of Signatures in String representation
 * @author ola
 *
 */
public class AtomSignatures {

    List<String> signatures;

    public AtomSignatures(List<String> signatures) {
        this.signatures = signatures;
    }

    public AtomSignatures() {
    }

    public List<String> getSignatures() {
        return signatures;
    }

    
    public void setSignatures( List<String> signatures ) {
        this.signatures = signatures;
    }
    
    public void addSignatures( List<String> signatures_in ) {
    	if (signatures==null) signatures=new ArrayList<String>();
    	for (String sign : signatures_in){
//    		if (!signatures.contains(sign))
    			signatures.add(sign);
    	}
    }

    /**
     * Return a semi-colon-separated list of signatures
     */
    @Override
    public String toString() {
        StringBuffer buf=new StringBuffer();
        for (String s : signatures){
            buf.append( s +";");
        }
        //Omit last comma
        return buf.substring( 0, buf.length()-1 );
    }
    
    @Override
    public boolean equals( Object obj ) {
        
        if (!( obj instanceof AtomSignatures )) return false;

        AtomSignatures sp = (AtomSignatures) obj;

        //Ensure equal size
        if (getSignatures().size()!=sp.getSignatures().size()) return false;

        if ((getSignatures().containsAll( sp.getSignatures() ))
                &&
                (sp.getSignatures().containsAll( getSignatures() )))
            return true;

        //Ensure all signatures in query mol exists in this
//        for (String sigstring : sp.getSignatures()){
//            if (!getSignatures().contains( sigstring ))
//                return false;
//        }
    
        return false;
    }
    
}
