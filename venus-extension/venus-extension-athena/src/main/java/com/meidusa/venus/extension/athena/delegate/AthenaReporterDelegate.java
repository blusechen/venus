package com.meidusa.venus.extension.athena.delegate;

import com.meidusa.venus.extension.athena.AthenaMetricReporter;
import com.meidusa.venus.extension.athena.AthenaProblemReporter;

/**
 * Created by GodzillaHua on 7/3/16.
 */
public final class AthenaReporterDelegate {

    private static AthenaReporterDelegate delegate = new AthenaReporterDelegate();

    private AthenaMetricReporter metricReporter;

    private AthenaProblemReporter problemReporter;

    public static AthenaReporterDelegate getDelegate() {
        return delegate;
    }

    public void setMetricReporter(AthenaMetricReporter metricReporter) {
        this.metricReporter = metricReporter;
    }

    public void setProblemReporter(AthenaProblemReporter problemReporter) {
        this.problemReporter = problemReporter;
    }

    public void metric(String key) {
        metric(key, 1);
    }

    public void metric(String key, int count) {
        if (metricReporter != null) {
            metricReporter.metric(key, count);
        }
    }

    public void problem(String message, Throwable cause) {
        if (problemReporter != null) {
            problemReporter.problem(message, cause);
        }
    }

}
