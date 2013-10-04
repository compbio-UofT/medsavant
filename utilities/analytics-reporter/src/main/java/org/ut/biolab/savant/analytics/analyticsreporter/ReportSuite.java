package org.ut.biolab.savant.analytics.analyticsreporter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mfiume
 */
class ReportSuite {

    private String SAVANT = "Savant";
    private String MEDSAVANT = "MedSavant";
    private ArrayList<Report> reports;

    public ReportSuite() {
        initReports();
    }

    private void initReports() {
        reports = new ArrayList<Report>();

        reports.add(new RunTimeReport(SAVANT));
    }

    public void runReports() {
        System.out.println("Running " + reports.size() + " reports");
        for (Report r : reports) {
            try {
                r.runReport();
            } catch (Exception e) {}
        }
    }

}
