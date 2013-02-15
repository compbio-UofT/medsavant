/*
 *    Copyright 2012 University of Toronto
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
package org.ut.biolab.medsavant.client.filter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.ut.biolab.medsavant.client.api.FilterStateAdapter;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public abstract class FilterView extends JPanel {

    private final String title;
    protected final int queryID;
    protected final JPanel viewCard;
    protected final JPanel waitCard;
    private boolean initialized = false;
    private boolean filterValuesReady;
    public final Object filterValuesReadySemaphore = new Object();

    public FilterView(String title, int queryID) {

        this.viewCard = new JPanel();

        this.waitCard = new JPanel();//new WaitPanel("Getting categories");
        waitCard.setLayout(new BorderLayout());
        JProgressBar idpb = new JProgressBar();
        idpb.setIndeterminate(true);
        waitCard.add(idpb, BorderLayout.CENTER);

        super.setLayout(new BorderLayout());
        super.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));//ViewUtil.getSmallBorder());
        super.add(this.viewCard, BorderLayout.CENTER);

        initialized = true;

        this.title = title;
        this.queryID = queryID;

        showWaitCard();
    }

    public String getTitle() {
        return title;
    }

    public abstract FilterStateAdapter saveState();

    /**
     * Give derived classes a chance to clean up when the filter instance is
     * being removed.
     */
    public void cleanup() {
    }

    /**
     * Many filters have a single parameter called "value", which can have
     * multiple values. This method packs them all into a string array, suitable
     * for passing to the <c>FilterState</c> constructor.
     *
     * @param applied the values which are applied (e.g. ["chr1", "chr2"])
     */
    public static List<String> wrapValues(Collection applied) {
        List<String> result = new ArrayList<String>();
        if (applied != null && !applied.isEmpty()) {
            for (Object val : applied) {
                result.add(val.toString());
            }
        }
        return result;
    }

    public synchronized final void showWaitCard() {
        super.removeAll();
        super.setLayout(new BorderLayout());
        super.add(waitCard, BorderLayout.CENTER);
        super.updateUI();
    }

    public synchronized final void showViewCard() {
        this.setFilterValuesReady(true);
        super.removeAll();
        super.setLayout(new BorderLayout());
        super.add(viewCard, BorderLayout.CENTER);
        super.updateUI();
    }

    @Override
    public Component add(Component c) {
        if (initialized) {
            return viewCard.add(c);
        } else {
            return super.add(c);
        }
    }

    @Override
    public void add(Component c, Object o) {

        if (initialized) {
            viewCard.add(c, o);
        } else {
            super.add(c, o);
        }
    }

    @Override
    public Component add(Component c, int i) {
        if (initialized) {
            return viewCard.add(c, i);
        } else {
            return super.add(c, i);
        }
    }

    @Override
    public void setLayout(LayoutManager lm) {
        if (initialized) {

            // create a new one, otherwise a sharing conflict occurs
            if (lm instanceof BoxLayout) {
                BoxLayout bl = (BoxLayout) lm;
                lm = new BoxLayout(viewCard, bl.getAxis());
            }

            viewCard.setLayout(lm);
        } else {
            super.setLayout(lm);
        }

    }

    @Override
    public void setBorder(Border b) {
        if (initialized) {
            viewCard.setBorder(b);
        } else {
            super.setBorder(b);
        }
    }

    public void setFilterValuesReady(boolean b) {
        filterValuesReady = true;
        synchronized (filterValuesReadySemaphore) {
            filterValuesReadySemaphore.notify();
        }
    }

    public boolean areFilterValuesReady() {
        return filterValuesReady;
    }

    public void waitForFilterValuesToBeReady() {
        if (!areFilterValuesReady()) {
            synchronized (filterValuesReadySemaphore) {
                try {
                    filterValuesReadySemaphore.wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
