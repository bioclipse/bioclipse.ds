package net.bioclipse.ds.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.help.IContext2;
import org.eclipse.help.IHelpResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import net.bioclipse.ds.Activator;
import net.bioclipse.ds.report.StatusHelper;

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
    private TopLevel toplevel;

	private String helppage;

    private String                sortingWeight;


	private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}
    
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

    
    public String getHelppage() {
		return helppage;
	}

	public void setHelppage(String helppage) {
		this.helppage = helppage;
	}

	public String getDescription() {
    
        return description;
    }

    
    public void setDescription( String description ) {
    
        this.description = description;
    }

    
    public List<IDSTest> getTests() {

        if ( tests == null )
            return Collections.emptyList();
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

        ImageDescriptor imageDesc;
        //Create the icon if not already done so
        if ( icon == null && plugin != null && iconpath != null ) {
            imageDesc = Activator.imageDescriptorFromPlugin( plugin, iconpath );
            if ( imageDesc == null )
                return null;
            icon= imageDesc.createImage();
        }
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
    
    public TopLevel getToplevel() {
		return toplevel;
	}

	public void setToplevel(TopLevel toplevel) {
		this.toplevel = toplevel;
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

    /**
     * Use consensuscalculator to compute consensus from all testruns
     * @return
     */
    public String getConsensusString(){
    	return StatusHelper.statusToString(getConsensus());
    }

	public int getConsensus() {
		if (testruns==null) return ITestResult.ERROR;
		
		List<Integer> res=new ArrayList<Integer>();
    	for (TestRun tr : testruns){
    		if (!(tr.getTest().isExcluded()) && tr.getTest().isVisible())
    			res.add(tr.getConsensusStatus());
    		
            //If any test with a hit has isOverride, then just assign
            //result based on this test
    		if (tr.getTest().isOverride() && tr.hasMatches())
    			if (tr.getConsensusStatus()==ITestResult.POSITIVE 
    					|| 
    				tr.getConsensusStatus()==ITestResult.NEGATIVE)
    			return tr.getConsensusStatus();
    	}
    	
    	int consensus = getConsensusCalculator().calculate(res);
		return consensus;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

    public String getSortingWeight() {

        return sortingWeight;
    }

    public void setSortingWeight( String order ) {

        this.sortingWeight = order;
    }

	@Override
	public String toString() {
		return "Endpoint [id=" + id + ", name=" + name + ", description="
				+ description + "]";
	}
    
}
