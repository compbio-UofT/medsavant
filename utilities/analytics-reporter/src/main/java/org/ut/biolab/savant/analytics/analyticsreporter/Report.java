package org.ut.biolab.savant.analytics.analyticsreporter;

import java.util.ArrayList;

/**
 *
 * @author mfiume
 */
abstract class Report {
    private final String name;
    private final ArrayList<ReportLog> logs;

    public Report(String name) {
        this.name = name;
        logs = new ArrayList<ReportLog>();
    }

    abstract void runReport();

    void addErrorLog(String msg) {
        logs.add(new ReportLog(ReportLog.Type.ERROR,msg));
    }

    void addInfoLog(String msg) {
        logs.add(new ReportLog(ReportLog.Type.INFO,msg));
    }
}
