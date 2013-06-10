package org.ut.biolab.medsavant.client.view.component;

import java.awt.CardLayout;
import java.awt.Color;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public final class BlockingPanel extends JPanel {

    private CardLayout cardLayout = new CardLayout();
    private static String BLOCK_CARD_NAME = "block";
    private static String CONTENT_CARD_NAME = "content";


    public BlockingPanel(String blockMessage, JPanel content) {
        setLayout(cardLayout);
        WaitPanel blockCard = new WaitPanel(blockMessage);
        blockCard.setTextColor(Color.gray);
        blockCard.setProgressBarVisible(false);
        add(blockCard,BLOCK_CARD_NAME);
        add(content,CONTENT_CARD_NAME);
        block();
    }

    public void block() {
        showCard(BLOCK_CARD_NAME);
    }

    public void unblock() {
        showCard(CONTENT_CARD_NAME);
    }

    private void showCard(String card) {
        cardLayout.show(this, card);
    }
}
