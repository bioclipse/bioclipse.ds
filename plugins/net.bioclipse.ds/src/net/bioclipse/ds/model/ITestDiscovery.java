package net.bioclipse.ds.model;

import java.util.List;

import net.bioclipse.core.business.BioclipseException;

/**
 * Interface implemented by plugins via manifest.
 * Provides a mehcnisms for plugin to provide tests 
 * in a dynamic way, e.g. by discovery.
 * 
 * @author ola
 *
 */
public interface ITestDiscovery {

	public List<IDSTest> discoverTests() throws BioclipseException;
	
}
