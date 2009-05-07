package net.bioclipse.ds.model;

import java.util.List;

import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.model.impl.DSException;

/**
 * A top interface for all tests
 * @author ola
 *
 */
public interface IDSTest {

    public String getId();    
    public void setId( String id );    
    public String getName();    
    public void setName( String name );    
    public String getIcon();
    public void setIcon( String icon );    

    @Deprecated
    public List<ITestResult> runWarningTest(IMolecule molecule, TestRun testrun) throws DSException;

    public List<ITestResult> runWarningTest(IMolecule molecule) throws DSException;

    public void addParameter( String name, String path );
    void setPluginID( String pluginID );
    String getPluginID();
    
}
