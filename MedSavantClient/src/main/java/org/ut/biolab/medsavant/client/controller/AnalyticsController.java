package org.ut.biolab.medsavant.client.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.view.genetics.family.FamilyMattersOptionView;
import org.ut.biolab.medsavant.client.view.subview.SectionView;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

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

        Analysis cohortAnalysis = new Analysis() {
            private JPanel view;
            private FamilyMattersOptionView fo;

            @Override
            public String getName() {
                return "Family Matters";
            }

            @Override
            public JPanel getContent() {

                if (view == null) {
                    view = new JPanel();
                    view.setLayout(new BorderLayout());
                    view.setBackground(Color.white);

                    JPanel titlePanel = new JPanel();
                    ViewUtil.applyHorizontalBoxLayout(titlePanel);

                    JLabel title = new JLabel(getName());
                    title.setOpaque(true);
                    title.setBorder(ViewUtil.getBigBorder());
                    title.setOpaque(false);
                    title.setFont(ViewUtil.getMediumTitleFont());
                    titlePanel.add(title);

                    view.add(titlePanel, BorderLayout.NORTH);

                    JPanel p = ViewUtil.getClearPanel();
                    p.setBorder(ViewUtil.getBigBorder());
                    p.setLayout(new BorderLayout());

                    fo = new FamilyMattersOptionView();
                    p.add(ViewUtil.centerHorizontally(fo.getView()), BorderLayout.NORTH);

                    view.add(p, BorderLayout.CENTER);
                }
                return view;
            }

            @Override
            public void stop() {
            }

            @Override
            public void restart() {
            }
        };

        this.installVariantAnalytic(enrichment);

        this.installVariantAnalytic(cohortAnalysis);
    }
}
