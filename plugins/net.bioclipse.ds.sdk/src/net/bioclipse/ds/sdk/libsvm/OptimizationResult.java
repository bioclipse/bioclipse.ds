package net.bioclipse.ds.sdk.libsvm;

public class OptimizationResult {
	
	private double OptimumValue;
	private double optimumGamma;
	private double optimumC;
	
	public OptimizationResult(double optimumValue, double optimumGamma,
			double optimumC) {
		super();
		OptimumValue = optimumValue;
		this.optimumGamma = optimumGamma;
		this.optimumC = optimumC;
	}

	public double getOptimumValue() {
		return OptimumValue;
	}

	public void setOptimumValue(double optimumValue) {
		OptimumValue = optimumValue;
	}

	public double getOptimumGamma() {
		return optimumGamma;
	}

	public void setOptimumGamma(double optimumGamma) {
		this.optimumGamma = optimumGamma;
	}

	public double getOptimumC() {
		return optimumC;
	}

	public void setOptimumC(double optimumC) {
		this.optimumC = optimumC;
	}

	@Override
	public String toString() {
		return "OptimizationResult [OptimumValue=" + OptimumValue
				+ ", optimumGamma=" + optimumGamma + ", optimumC=" + optimumC
				+ "]";
	}

	
	

}
