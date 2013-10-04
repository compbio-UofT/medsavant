package org.ut.biolab.savant.analytics.analyticsreporter;

import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) {

        ReportSuite s = new ReportSuite();
        s.runReports();


        /*
        double[][] data = new double[][]{
            {210, 300, 320, 265, 299, 200},
            {200, 304, 201, 201, 340, 300},};
        CategoryDataset ds = DatasetUtilities.createCategoryDataset(
                "Team", "Match", data);

        JFreeChart chart = ChartFactory.createBarChart("Chart", "Team", "Y", ds, PlotOrientation.VERTICAL, false, false, false);

        JFrame f = new JFrame();
        f.getContentPane().add(new ChartPanel(chart));
        f.show();
        */

    }
}
