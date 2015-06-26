package net.bioclipse.ds.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
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
public class TopLevel implements IContext2{

    private String id;
    private String name;
    private String description;
    private String plugin;
    private Image icon;
    private String iconpath;
	private String helppage;
	private List<Endpoint> endpoints;

    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}
    
    public TopLevel(String pid, String pname) {
        id=pid;
        name=pname;
    }

    public TopLevel(String pid, String pname, String pdesc) {
        this(pid, pname);
        setDescription( pdesc);
    }

    public TopLevel(String pid, String pname, String pdesc, String picon, 
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

    
    public String getId() {
        return id;
    }

    
    public String getName() {
        return name;
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


    public List<Endpoint> getEndpoints() {

        if ( endpoints == null )
            return Collections.emptyList();
		return endpoints;
	}

	public void setEndpoints(List<Endpoint> endpoints) {
		this.endpoints = endpoints;
	}
	
	public void addEndpoint(Endpoint ep) {
		if (endpoints==null) endpoints=new ArrayList<Endpoint>();
		endpoints.add(ep);
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


	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "TopLevel [id=" + id + ", name=" + name + ", description="
				+ description + "]";
	}
    
}
