package net.bioclipse.ds.cpdb.signatures;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.openscience.cdk.interfaces.IAtom;

import net.bioclipse.ds.impl.result.SubStructureMatch;


public class RGBMatch extends SubStructureMatch{
    
    private Map<IAtom, Color> colorMap; 

    public RGBMatch(String name, int resultStatus) {
        super( name, resultStatus );
    }
    
    @Override
    public Color getHighlightingColor( IAtom atom ) {
        
        if (getColorMap().containsKey( atom ))
            return getColorMap().get( atom );

        return null;
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
