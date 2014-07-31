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
package org.ut.biolab.medsavant.client.view.genetics.inspector.stat;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.api.Listener;

import org.ut.biolab.medsavant.shared.vcf.VariantRecord;
import org.ut.biolab.medsavant.client.view.genetics.TablePanel;
import org.ut.biolab.medsavant.client.view.genetics.inspector.CollapsibleInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.SocialVariantSubInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.BasicVariantSubInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.BeaconSubInspector;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class StaticVariantInspector extends CollapsibleInspector implements Listener<VariantRecord> {

    private static StaticVariantInspector instance;
    private static List<Listener<VariantRecord>> listeners = new ArrayList<Listener<VariantRecord>>();
    private static VariantRecord record;

    public static StaticVariantInspector getInstance() {
        if (instance == null) {
            instance = new StaticVariantInspector();
        }
        return instance;
    }

    private StaticVariantInspector() {

        JPanel messagePanel = new JPanel();
        //messagePanel.setBackground(Color.white);
        messagePanel.setBorder(ViewUtil.getHugeBorder());
        ViewUtil.applyVerticalBoxLayout(messagePanel);

        JLabel h1 = new JLabel("No Variant Selected");
        h1.setFont(ViewUtil.getMediumTitleFont());

        String m = "<html><div style=\"text-align: center;\">Choose one from the list on the left</div></html>";
        JLabel h2 = new JLabel(m);
        h2.setPreferredSize(new Dimension(190,300));
        h2.setMinimumSize(new Dimension(190,300));
        h2.setBackground(Color.red);

        messagePanel.add(ViewUtil.centerHorizontally(h1));
        messagePanel.add(Box.createVerticalStrut(10));
        messagePanel.add(ViewUtil.centerHorizontally(h2));

        this.setMessage(messagePanel);

        TablePanel.addVariantSelectionChangedListener(this);
        this.addSubInspector(new BasicVariantSubInspector());
        this.addSubInspector(new SocialVariantSubInspector());
        //this.addSubInfoPanel(new BasicGeneInfoSubPanel());
    }

    @Override
    public String getName() {
        return "Variant Inspector";
    }

    public static void addVariantSelectionChangedListener(Listener<VariantRecord> l) {
        listeners.add(l);
    }

    @Override
    public void handleEvent(VariantRecord r) {
        if (r == null) {
            this.switchToMessage();
        } else {
            this.switchToPanes();
        }
        StaticInspectorPanel.getInstance().switchToVariantInspector();
        for (Listener<VariantRecord> l : listeners) {
            l.handleEvent(r);
        }
        record = r;
    }
}
