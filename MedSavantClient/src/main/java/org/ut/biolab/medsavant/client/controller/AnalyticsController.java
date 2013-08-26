package org.ut.biolab.medsavant.client.controller;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class AnalyticsController {

    private static AnalyticsController instance;
    private final ArrayList<Analysis> variantanalytics;

    public static AnalyticsController getInstance() {
        if (instance == null) {
            instance = new AnalyticsController();
        }
        return instance;
    }

    private AnalyticsController() {
        this.variantanalytics = new ArrayList<Analysis>();
        installStandardAnalytics();
    }

    public List<Analysis> getVariantAnalytics() {
        return variantanalytics;
    }

    public void installVariantAnalytic(Analysis a) {
        this.variantanalytics.add(a);
    }

    public abstract class Analysis {

        public abstract String getName();

        public abstract JPanel getContent();

        @Override
        public String toString() {
            return getName();
        }

        public abstract void stop();

        public abstract void restart();
    }

    private void installStandardAnalytics() {
        Analysis enrichment = new Analysis() {
            private EnrichmentPanel epanel;

            @Override
            public String getName() {
                return "Enrichment";
            }

            @Override
            public JPanel getContent() {
                if (epanel == null) {
                    epanel = new EnrichmentPanel();
                }
                return epanel;
            }

            @Override
            public void stop() {
                epanel.stop();
            }

            @Override
            public void restart() {
                epanel.restart();
            }
        };

        this.installVariantAnalytic(enrichment);
    }
}
