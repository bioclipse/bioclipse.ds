package net.bioclipse.ds.libsvm;

public class Tuple {

	private int x;
	private double y;

	public Tuple(int x, double y) {
		super();
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "Tuple [x=" + x + ", y=" + y + "]";
	}

	
	
}
