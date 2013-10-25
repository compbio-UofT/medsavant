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

package makeposterbg;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;

public class RingChart extends JPanel {

    List<Ring> rings;

    @SuppressWarnings("LeakingThisInConstructor")
    public RingChart() {
        setOpaque(false);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent arg0) {
                repaint();
            }
        });

        rings = new ArrayList<Ring>();

        ToolTipManager.sharedInstance().registerComponent(this);

        setOpaque(false);
    }


    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));

        Stroke s = new BasicStroke(1.25f);

        //g2.setColor(Color.WHITE);

        //g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(s);



        Insets insets = this.getInsets();
        int h = this.getHeight() - insets.top - insets.bottom;
        int w = this.getWidth() - insets.left - insets.right;
        int m = Math.min(h, w);


        for (int r = 0; r < rings.size(); r++) {
            Ring ring = rings.get(r);
            double rw = ((double) m / ((double) rings.size() + 1)) / 2;
            ring.setRingWidth(rw);
            ring.setRadius(2 * rw + rw * (double) r);
            ring.setCenter((double) this.getWidth() / 2.0, (double) this.getHeight() / 2.0);
            ring.createSegments();

            for (int index = 0; index < ring.count(); index++) {
                g2.setColor(ring.getColor(index));
                g2.fill(ring.getSegment(index));
            }
        }
    }

    @Override
    public JToolTip createToolTip() {

        JToolTip tooltip = super.createToolTip();

        tooltip.setBackground(new Color(255, 255, 140, 128));

        this.repaint(); // TODO: will this bog us down?

        return tooltip;

    }

    @Override
    public String getToolTipText(java.awt.event.MouseEvent e) {

        for (int r = 0; r < rings.size(); r++) {
            Ring ring = rings.get(r);

            for (int index = 0; index < ring.count(); index++) {
                if (ring.getSegment(index).contains(e.getPoint())) {
                    return ring.getLabel(index) + ": " + ring.getValue(index);
                }
            }
        }

        return super.getToolTipText(e);
    }

    public void setRings(List<Ring> r) {
        rings = r;
        repaint();
    }
}
