package net.bioclipse.ds.model.result;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.openscience.cdk.interfaces.IAtom;



public class RGBMatch extends SubStructureMatch{
    
    private Map<IAtom, Color> colorMap; 

    public RGBMatch(String name, int resultStatus) {
        super( name, resultStatus );
    }
    
    public void setColorMap( Map<IAtom, Color> colorMap ) {

        this.colorMap = colorMap;
    }

    public Map<IAtom, Color> getColorMap() {
        return colorMap;
    }

    public void putAtomColor( IAtom atomToAdd, Color color ) {
        if (colorMap==null) colorMap=new HashMap<IAtom, Color>();
        colorMap.put( atomToAdd, color );
        
    }

}
