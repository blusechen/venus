package com.meidusa.venus.extension.monitor.entity;


public class PerformanceMonitorEntity extends AbstractMonitorEntity {
	private static final long serialVersionUID = 1L;
	/**
	 * average request completed time 该值作为一个统计周期的平均值，跟参与前一个统计周期数据的无关
	 */
	private double average;

	/**
	 * request times
	 */
	private long times;

	public double getAverage() {
		return average;
	}

	public void setAverage(double average) {
		this.average = average;
	}

	public long getTimes() {
		return times;
	}

	public void setTimes(long times) {
		this.times = times;
	}

}
