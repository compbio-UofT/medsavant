/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
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
