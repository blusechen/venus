package com.meidusa.venus.extension.monitor.entity;

public class JVMMonitorEntity extends AbstractMonitorEntity {
	private static final long serialVersionUID = 1L;
	private double oldGen;
	private double permGen;
	private int threadSize;

	public double getOldGen() {
		return oldGen;
	}

	public void setOldGen(double oldGen) {
		this.oldGen = oldGen;
	}

	public double getPermGen() {
		return permGen;
	}

	public void setPermGen(double permGen) {
		this.permGen = permGen;
	}

	public int getThreadSize() {
		return threadSize;
	}

	public void setThreadSize(int threadSize) {
		this.threadSize = threadSize;
	}

}
