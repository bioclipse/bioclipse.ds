package net.bioclipse.ds.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.help.IContext2;
import org.eclipse.help.IHelpResource;
import org.eclipse.swt.graphics.Image;

import net.bioclipse.ds.Activator;

/**
 * An endpoint in Decision Support.
 * @author ola
 *
 */
public class Endpoint implements IContext2{

    private String id;
    private String name;
    private String description;
    private String plugin;
    private List<IDSTest> tests;
    private List<TestRun> testruns;
    private Image icon;
    private String iconpath;
    private IConsensusCalculator consensusCalculator;

    public Endpoint(String pid, String pname) {
        id=pid;
        name=pname;
    }

    public Endpoint(String pid, String pname, String pdesc) {
        this(pid, pname);
        setDescription( pdesc);
    }

    public Endpoint(String pid, String pname, String pdesc, String picon, 
                    String plugin) {
        this(pid, pname, pdesc);
        setPlugin( plugin );
        setIcon( picon );
    }

    
    public String getDescription() {
    
        return description;
    }

    
    public void setDescription( String description ) {
    
        this.description = description;
    }

    
    public List<IDSTest> getTests() {
    
        return tests;
    }

    
    public void setTests( List<IDSTest> tests ) {
        this.tests = tests;
    }

    
    public String getId() {
        return id;
    }

    
    public String getName() {
        return name;
    }

    public void addTest( IDSTest test ) {
        if (tests==null) tests=new ArrayList<IDSTest>();
        tests.add( test );
        
    }

    public void setIcon( String iconpath ) {
        this.iconpath=iconpath;
    }

    public Image getIcon() {
        //Create the icon if not already done so
        if (icon==null && plugin!=null && iconpath!=null)
            icon=Activator.imageDescriptorFromPlugin( 
                      plugin, iconpath ).createImage();
        return icon;
    }

    public void setPlugin( String plugin ) {
        this.plugin = plugin;
    }
    public String getPlugin() {
        return plugin;
    }

    public void setTestruns( List<TestRun> testruns ) {

        this.testruns = testruns;
    }

    public List<TestRun> getTestruns() {

        return testruns;
    }

    public void addTestRun( TestRun newTestRun ) {
        if (testruns==null)
            testruns=new ArrayList<TestRun>();

        testruns.add( newTestRun );
        
    }

    public void setConsensusCalculator( IConsensusCalculator consensusCalculator ) {

        this.consensusCalculator = consensusCalculator;
    }

    public IConsensusCalculator getConsensusCalculator() {

        return consensusCalculator;
    }

    
    /*
     * BELOW is for CONTEXT
     */
    
    public String getText() {
        return null;
    }
    
    public String getStyledText() {
        return getDescription();
    }
    
    
    public IHelpResource[] getRelatedTopics() {
        return null;
    }
    

    public String getCategory( IHelpResource topic ) {
        return null;
    }
    
    public String getTitle() {
        return getName();
    }

    
}
