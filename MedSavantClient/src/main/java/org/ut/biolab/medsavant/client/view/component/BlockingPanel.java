/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
