package net.bioclipse.ds.report.model;

import java.awt.Image;

/**
 * A report model for a Result
 * 
 * @author ola
 *
 */
public class Result {
	
//	Name of Result
	String name;

//	Outcome of the test, e.g. POSITIVE
	String outcome;

//	Details to put in report
	String details;

//	Image to put in report, e.g. highlighted atoms
	Image testImage;

//	Image to put in report, e.g. highlighted atoms
	Image consensusImage;

	public Image getConsensusImage() {
		return consensusImage;
	}
	public void setConsensusImage(Image consensusImage) {
		this.consensusImage = consensusImage;
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
	public Image getTestImage() {
		return testImage;
	}
	public void setTestImage(Image testImage) {
		this.testImage = testImage;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}

	public Result(String name, String outcome, String details, Image testImage,
			Image consensusImage) {
		super();
		this.name = name;
		this.outcome = outcome;
		this.details = details;
		this.testImage = testImage;
		this.consensusImage = consensusImage;
	}
	public Result(String name, String outcome, String details, Image testImage) {
		super();
		this.name = name;
		this.outcome = outcome;
		this.details = details;
		this.testImage = testImage;
	}
	
	public Result(String name, String outcome, String details) {
		super();
		this.name = name;
		this.outcome = outcome;
		this.details = details;
	}
	
	public Result(String name, String outcome, Image testImage) {
		this.name = name;
		this.outcome = outcome;
		this.testImage = testImage;
	}
	
	public Result(String name, String outcome) {
		this.name = name;
		this.outcome = outcome;
	}

}
