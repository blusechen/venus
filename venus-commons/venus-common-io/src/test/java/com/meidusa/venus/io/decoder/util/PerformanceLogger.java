package com.meidusa.venus.io.decoder.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PerformanceLogger {
    private static long before;
    private static List<PerformanceInfo> performanceLogList = new LinkedList<PerformanceLogger.PerformanceInfo>();

    static class PerformanceInfo {

        public PerformanceInfo(String name, long interval) {
            super();
            this.name = name;
            this.interval = interval;
        }

        String name;
        long interval;
    }

    public static void begin() {
        before = System.nanoTime();
    }

    public static void log(String name) {
        long current = System.nanoTime();
        long interval = current - before;
        before = current;
        performanceLogList.add(new PerformanceInfo(name, interval));

    }

    public static void print(String title) {
        System.out.println(title + "\tTotal " + performanceLogList.size() + " records.");
        for (Iterator iterator = performanceLogList.iterator(); iterator.hasNext();) {
            PerformanceInfo type = (PerformanceInfo) iterator.next();
            System.out.println("\t" + type.name + "\t" + type.interval);
        }
    }

    public static void main(String args) {

    }

}
