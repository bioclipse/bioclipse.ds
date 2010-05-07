package net.bioclipse.ds.signatures.prop.calc;

import java.util.List;

/**
 * A class to hold a list of Signatures in String representation
 * @author ola
 *
 */
public class Signatures {

    List<String> signatures;

    public Signatures(List<String> signatures) {
        this.signatures = signatures;
    }

    public Signatures() {
    }

    public List<String> getSignatures() {
        return signatures;
    }

    
    public void setSignatures( List<String> signatures ) {
        this.signatures = signatures;
    }

    /**
     * Return a comma-separated list of signatures
     */
    @Override
    public String toString() {
        StringBuffer buf=new StringBuffer();
        for (String s : signatures){
            buf.append( s +",");
        }
        //Omit last comma
        return buf.substring( 0, buf.length()-1 );
    }
    
    @Override
    public boolean equals( Object obj ) {
        
        if (!( obj instanceof Signatures )) return false;

        Signatures sp = (Signatures) obj;

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
