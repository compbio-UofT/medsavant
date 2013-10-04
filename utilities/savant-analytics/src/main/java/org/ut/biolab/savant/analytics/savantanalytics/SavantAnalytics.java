/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.savant.analytics.savantanalytics;

/**
 *
 * @author mfiume
 */
public class SavantAnalytics {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        AnalyticsAgent.onStartSession("Savant","test");

        AnalyticsAgent.log("Example");

        AnalyticsAgent.onEndSession();
    }
}
