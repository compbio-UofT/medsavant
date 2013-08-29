package medsavant.listenrichment.app;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import medsavant.listenrichment.app.AggregatesStatsPanel;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.filter.FilterEvent;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.reference.ReferenceEvent;
import org.ut.biolab.medsavant.client.util.ThreadController;

/**
 *
 * @author mfiume
 */
class EnrichmentPanel extends JPanel {

    private AggregatesStatsPanel statsPanel;
    private static String pageName = "Enrichment";

    public EnrichmentPanel() {
        FilterController.getInstance().addListener(new Listener<FilterEvent>() {
            @Override
            public void handleEvent(FilterEvent event) {
                ThreadController.getInstance().cancelWorkers(pageName);
                if (statsPanel != null) {
                    statsPanel.update();
                }
            }
        });
        ReferenceController.getInstance().addListener(new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent evt) {
                if (evt.getType() == ReferenceEvent.Type.CHANGED && statsPanel != null) {
                    statsPanel.update();
                }
            }
        });

        initView();
    }

    private void initView() {
        this.setLayout(new BorderLayout());
        statsPanel = new AggregatesStatsPanel(pageName);
        this.add(statsPanel, BorderLayout.CENTER);
        if (statsPanel != null) {
            statsPanel.update();
        }
    }

    void stop() {
        ThreadController.getInstance().cancelWorkers(pageName);
    }

    void restart() {
        if (statsPanel != null) {
            statsPanel.update();
        }
    }
}
