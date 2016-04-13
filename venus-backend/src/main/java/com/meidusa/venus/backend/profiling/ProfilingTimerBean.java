package com.meidusa.venus.backend.profiling;

import java.util.ArrayList;
import java.util.List;

import com.meidusa.toolkit.util.TimeUtil;

public class ProfilingTimerBean implements java.io.Serializable {

    private static final long serialVersionUID = -6180672043920208784L;

    List<ProfilingTimerBean> children = new ArrayList<ProfilingTimerBean>();
    ProfilingTimerBean parent = null;

    String resource;

    long startTime;
    long totalTime;

    public ProfilingTimerBean(String resource) {
        this.resource = resource;
    }

    protected void addParent(ProfilingTimerBean parent) {
        this.parent = parent;
    }

    public ProfilingTimerBean getParent() {
        return parent;
    }

    public void addChild(ProfilingTimerBean child) {
        children.add(child);
        child.addParent(this);
    }

    public void setStartTime() {
        this.startTime = TimeUtil.currentTimeMillis();
    }

    public void setEndTime() {
        this.totalTime = TimeUtil.currentTimeMillis() - startTime;
    }

    public String getResource() {
        return resource;
    }

    /**
     * Get a formatted string representing all the methods that took longer than a specified time.
     */

    public String getPrintable(long minTime) {
        return getPrintable("", minTime);
    }

    protected String getPrintable(String indent, long minTime) {
        // only print the value if we are larger or equal to the min time.
        if (totalTime >= minTime) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(indent);
            buffer.append("[" + totalTime + "ms] - " + resource);
            buffer.append("\n");

            for (ProfilingTimerBean aChildren : children) {
                buffer.append((aChildren).getPrintable(indent + "  ", minTime));
            }

            return buffer.toString();
        } else
            return "";
    }
}
