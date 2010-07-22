package net.bioclipse.ds.report.model;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

/**
 * Report model for an Endpoint
 * 
 * @author ola
 *
 */
public class Endpoint {

	String name;
	String consensus;
	Image endpointImage;
	Image consensusImage;
	List<Test> tests;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getConsensus() {
		return consensus;
	}
	public void setConsensus(String consensus) {
		this.consensus = consensus;
	}
	public List<Test> getTests() {
		return tests;
	}
	public void setTests(List<Test> tests) {
		this.tests = tests;
	}
	public Image getEndpointImage() {
		return endpointImage;
	}
	public void setEndpointImage(Image endpointImage) {
		this.endpointImage = endpointImage;
	}
	public Image getConsensusImage() {
		return consensusImage;
	}
	public void setConsensusImage(Image consensusImage) {
		this.consensusImage = consensusImage;
	}
	
	public Endpoint(String name, String consensus, Image endpointImage) {
		super();
		this.name = name;
		this.consensus = consensus;
		this.endpointImage = endpointImage;
	}

	public Endpoint(String name, String consensus, Image endpointImage,
			Image consensusImage) {
		super();
		this.name = name;
		this.consensus = consensus;
		this.endpointImage = endpointImage;
		this.consensusImage = consensusImage;
	}
	public Endpoint(String name, String consensus) {
		super();
		this.name = name;
		this.consensus = consensus;
	}

	public void addTest(Test test){
		if (tests==null)
			tests=new ArrayList<Test>();
		tests.add(test);
	}

}
