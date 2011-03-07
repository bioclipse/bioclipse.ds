package net.bioclipse.ds.sdk.qsar;

import org.eclipse.core.runtime.IProgressMonitor;

public class ConsoleProgressMonitor implements IProgressMonitor {

	private int totalWork;
	private int worked;

	@Override
	public void beginTask(String name, int totalWork) {
		System.out.println("MONITOR BEGIN: " + name + " Work=" + totalWork);
		this.totalWork=totalWork;
		worked=0;
	}

	@Override
	public void done() {
		System.out.println("MONITOR: DONE!");
	}

	@Override
	public void internalWorked(double work) {
	}

	@Override
	public boolean isCanceled() {
		return false;
	}

	@Override
	public void setCanceled(boolean value) {
	}

	@Override
	public void setTaskName(String name) {
	}

	@Override
	public void subTask(String name) {
	}

	@Override
	public void worked(int work) {
		System.out.println("MONITOR AT: " + work + "/" + totalWork);
		worked=worked+work;

	}

}
