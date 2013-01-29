package net.bioclipse.ds.matcher.model;

import java.util.ArrayList;
import java.util.List;

public class SmartsMatchEntry {

	String name;
	List<String> matchingSmarts;
	List<String> nonMatchingSmarts;

	//CONSTRUCTORS
	public SmartsMatchEntry(String name) {
		super();
		this.name = name;
	}

	//GETTERS
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getMatchingSmarts() {
		return matchingSmarts;
	}
	public void setMatchingSmarts(List<String> matchingSmarts) {
		this.matchingSmarts = matchingSmarts;
	}
	public List<String> getNonMatchingSmarts() {
		return nonMatchingSmarts;
	}
	public void setNonMatchingSmarts(List<String> nonMatchingSmarts) {
		this.nonMatchingSmarts = nonMatchingSmarts;
	}

	public void addSmarts(String smarts) {
		if (matchingSmarts==null) matchingSmarts=new ArrayList<String>();
		matchingSmarts.add(smarts);
	}
	public void addNonMatchingSmarts(String smarts) {
		if (nonMatchingSmarts==null) nonMatchingSmarts=new ArrayList<String>();
		nonMatchingSmarts.add(smarts);
	}

	@Override
	public String toString() {
		return "SmartsMatchEntry [name=" + name + ", matchingSmarts="
				+ matchingSmarts + ", nonMatchingSmarts=" + nonMatchingSmarts
				+ "]";
	}

}
