package net.bioclipse.ds.impl.cons;

import net.bioclipse.ds.model.IConsensusCalculator;

/**
 * An abstract base class for all consensus calculators.
 * @author ola
 *
 */
public abstract class AbstractConsensusCalculator implements IConsensusCalculator {

    String id;
    String name;
    String description;

    public String getId() {
        return id;
    }
    
    public void setId( String id ) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName( String name ) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription( String description ) {
        this.description = description;
    }
    
}
