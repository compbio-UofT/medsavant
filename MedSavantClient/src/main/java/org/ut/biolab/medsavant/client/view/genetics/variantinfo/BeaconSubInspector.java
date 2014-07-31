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
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import com.dnastack.ga4gh.beacon.BeaconDatasource;
import com.dnastack.ga4gh.beacon.BeaconSearcher;
import java.awt.BorderLayout;
import org.ut.biolab.medsavant.client.view.genetics.inspector.SubInspector;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.component.BlockingPanel;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.font.FontFactory;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.images.ImagePanel;

import org.ut.biolab.medsavant.shared.vcf.VariantRecord;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;


/**
 *
 * @author mfiume
 */
public class BeaconSubInspector extends SubInspector {
    
    private final String name;
    private final JPanel p;
    private final BeaconSearcher bs;

    public BeaconSubInspector() {
        this.name = "GA4GH Beacons";
        p = new JPanel();
        bs = new BeaconSearcher();
    }

    @Override
    public String getName() {
        return this.name;
    }

    public boolean showHeader() {
        return false;
    }

    @Override
    public JPanel getInfoPanel() {

        return p;
    }

    public void setVariantRecord(final VariantRecord r) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                searchBeaconsForVariant(r);
            }
            
        });
        t.start();
    }

    private void searchBeaconsForVariant(VariantRecord r) {
        p.removeAll();
        
        JPanel results = ViewUtil.getClearPanel();
        BlockingPanel bp = new BlockingPanel("Querying Beacons...",results);
        bp.block();
        p.setLayout(new BorderLayout());
        p.add(bp,BorderLayout.CENTER);
        p.updateUI();
        
        List<BeaconDatasource> datasources = bs.getDatasources();
        
        results.setLayout(new MigLayout("wrap 2"));
        
        for (BeaconDatasource ds : datasources) {
            
            boolean hasVariant = bs.searchBeacons(ds, null, r.getChrom(), r.getStartPosition().intValue(), r.getAlt());
            
            JLabel nameLabel = new JLabel(ds.getName());
            Font font = FontFactory.getGeneralFont().deriveFont(11f).deriveFont(Font.BOLD);
            nameLabel.setFont(font);
            
            results.add(nameLabel);
            
            String url = hasVariant ?
                    "org/ut/biolab/medsavant/client/view/images/icon/beacon-yes.png"
                    : "org/ut/biolab/medsavant/client/view/images/icon/beacon-no.png";
            
            ImagePanel imagePanel = new ImagePanel(url);
            
            results.add(imagePanel);
        }
        bp.unblock();
        p.updateUI();
    }
}
