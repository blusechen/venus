package com.meidusa.venus.backend.services.xml.bean;

public class PerformanceLogger {
    private int error = 5 * 1000;
    private int warn = 3 * 1000;
    private int info = 1 * 1000;
    private boolean printParams = true;
    private boolean printResult = false;

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public int getWarn() {
        return warn;
    }

    public void setWarn(int warn) {
        this.warn = warn;
    }

    public int getInfo() {
        return info;
    }

    public void setInfo(int info) {
        this.info = info;
    }

    public boolean isPrintParams() {
        return printParams;
    }

    public void setPrintParams(boolean printParams) {
        this.printParams = printParams;
    }

    public boolean isPrintResult() {
        return printResult;
    }

    public void setPrintResult(boolean printResult) {
        this.printResult = printResult;
    }
}
