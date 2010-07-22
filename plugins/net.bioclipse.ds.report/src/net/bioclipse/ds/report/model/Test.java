package net.bioclipse.ds.report.model;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

/**
 * A report model for a Test
 * 
 * @author ola
 *
 */
public class Test {
	
	String name;
	String outcome;
	Image consensusImage;
	List<Result> results;
	
	public Image getConsensusImage() {
		return consensusImage;
	}
	public void setConsensusImage(Image consensusImage) {
		this.consensusImage = consensusImage;
	}
	public List<Result> getResults() {
		return results;
	}
	public void setResults(List<Result> results) {
		this.results = results;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOutcome() {
		return outcome;
	}
	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}

	public Test(String name, String outcome, Image consensusImage) {
		super();
		this.name = name;
		this.outcome = outcome;
		this.consensusImage = consensusImage;
	}
	public Test(String name, String outcome) {
		super();
		this.name = name;
		this.outcome = outcome;
	}
	
	public void addResult(Result result){
		if (results==null)
			results=new ArrayList<Result>();
		results.add(result);
	}


}
