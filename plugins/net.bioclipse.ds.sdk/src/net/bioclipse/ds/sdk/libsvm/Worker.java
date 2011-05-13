package net.bioclipse.ds.sdk.libsvm;

import java.awt.Point;
import java.util.List;

import javax.vecmath.Vector2d;

public class Worker implements Runnable {  

	private OptimizationResult optres;
	private List<Point> params;

	public OptimizationResult getOptres() {
		return optres;
	}

	public void setOptres(OptimizationResult optres) {
		this.optres = optres;
	}

	public Worker(List<Point> params) {  
		this.params=params;  
	}  

	public void run() {  
	}


	public List<Point> getParams() {
		return params;
	}

	public void setParams(List<Point> params) {
		this.params = params;
	}

	@Override
	public String toString() {
		return "Worker [optres=" + optres + ", params=" + params + "]";
	}  
	
	

}  