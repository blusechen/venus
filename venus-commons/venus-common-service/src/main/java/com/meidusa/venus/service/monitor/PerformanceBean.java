package com.meidusa.venus.service.monitor;

import com.meidusa.toolkit.util.TimeUtil;

public class PerformanceBean {

    private String name;
    /**
     * average request completed time 该值作为一个统计周期的平均值，跟参与前一个统计周期数据的无关
     */
    private double average;

    /**
     * 平均时间的 "经验值"，每次统计平均值以后，这个值将作为下一次统计的初始值（基数），不参与 reset
     */
    private double basicAverage ;
    /**
     * request times
     */
    private long times;
    
    private long errors;

    /**
     * start time or rest time
     */
    private long startTime = TimeUtil.currentTimeMillis();

    public synchronized void calculateAverage(long current ,boolean isError) {
        double j = (double) times / (double) (times + 1);
        double x = (double) average * j + (double) current / (double) (times + 1);
        double y = (double) basicAverage * j + (double) current / (double) (times + 1);
        basicAverage = y;
        average = x;
        times++;
        if(isError){
        	errors++;
        }
    }

    public synchronized void rest() {
        average = 0;
        times = 0;
        startTime = TimeUtil.currentTimeMillis();
        errors = 0;
    }

    public double getAverage() {
        return average;
    }

    public double getBasicAverage() {
        return basicAverage;
    }

    public long getTimes() {
        return times;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public long getErrors() {
		return errors;
	}

}
