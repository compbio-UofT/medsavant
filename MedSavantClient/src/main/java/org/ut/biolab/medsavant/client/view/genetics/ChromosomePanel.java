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
package org.ut.biolab.medsavant.client.view.genetics;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.shared.model.Chromosome;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ChromosomePanel extends JPanel {

    private final Chromosome chr;
    private final ChromosomeDiagramPanel cdp;

    public ChromosomePanel(Chromosome c) {
        this.setOpaque(false);
        this.chr = c;
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        JLabel shortLabel = new JLabel(chr.getShortname());
        ViewUtil.makeSmall(shortLabel);

        shortLabel.setForeground(Color.gray);

        this.add(shortLabel);
        this.add(ViewUtil.getSmallVerticalSeparator());
        cdp = new ChromosomeDiagramPanel(c);
        this.add(cdp);

        this.setPreferredSize(new Dimension(20,999));
        this.setMaximumSize(new Dimension(20,999));
    }

    void setScaleWithRespectToLength(long len) {
        cdp.setScaleWithRespectToLength(len);
    }

    public void updateFrequencyCounts(Map<Range,Integer> binCounts,int max) {
        cdp.updateFrequencyCounts(binCounts,max);
    }

    public String getChrName() {
        return chr.getName();
    }

    public String getShortChrName() {
        return chr.getShortname();
    }
}
