package net.bioclipse.ds.matcher.model;

/**
 * A class to hold information about a significant signature
 * 
 * @author ola
 */
public class SignificantSignature {

	String signature;
	int nrPos;
	int nrTot;
	double pValue;
	double accuracy;
	String activeCall;
	int height;

	/**
	 * Constructor for all fields.
	 * 
	 * @param signature
	 * @param nrPos
	 * @param nrTot
	 * @param pValue
	 * @param accuracy
	 * @param activeCall
	 */
	public SignificantSignature(String signature, int nrPos, int nrTot,
			double pValue, double accuracy, String activeCall, int height) {

		super();
		this.signature = signature;
		this.nrPos = nrPos;
		this.nrTot = nrTot;
		this.pValue = pValue;
		this.accuracy = accuracy;
		this.activeCall = activeCall;
		this.height=height;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public int getNrTot() {
		return nrTot;
	}
	public void setNrTot(int nrTot) {
		this.nrTot = nrTot;
	}
	public String getActiveCall() {
		return activeCall;
	}
	public void setActiveCall(String activeCall) {
		this.activeCall = activeCall;
	}
	public int getNrPos() {
		return nrPos;
	}
	public void setNrPos(int nrPos) {
		this.nrPos = nrPos;
	}
	public double getpValue() {
		return pValue;
	}
	public void setpValue(double pValue) {
		this.pValue = pValue;
	}
	public double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public String toString() {
		return signature + " [nrpos=" + nrPos + ", nrTot=" + nrTot + 
		", pValue=" + pValue + ", accuracy=" + accuracy + 
		", activeCall=" + activeCall + "]";
	}

}
