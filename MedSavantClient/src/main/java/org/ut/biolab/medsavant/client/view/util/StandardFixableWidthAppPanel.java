/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.client.view.util;

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author mfiume
 */
public class StandardFixableWidthAppPanel extends JPanel {

    private final JPanel content;
    private final JLabel titleLabel;
    private final boolean initialized;

    private static boolean DEFAULT_DOESSCROLL = true;
    private static String DEFAULT_TITLE = null;
    private static boolean DEFAULT_FIXEDWIDTH = true;

    public StandardFixableWidthAppPanel() {
        this(DEFAULT_DOESSCROLL);
    }

    public StandardFixableWidthAppPanel(String title) {
        this(title, DEFAULT_DOESSCROLL);
    }

    public StandardFixableWidthAppPanel(boolean doesScroll) {
        this(DEFAULT_TITLE, doesScroll, DEFAULT_FIXEDWIDTH);
    }

    public StandardFixableWidthAppPanel(boolean doesScroll, boolean fixedWidth) {
        this(DEFAULT_TITLE, doesScroll, fixedWidth);
    }

    public StandardFixableWidthAppPanel(String title, boolean doesScroll) {
        this(title, doesScroll, DEFAULT_FIXEDWIDTH);
    }

    public StandardFixableWidthAppPanel(String title, boolean doesScroll, boolean fixedWidth) {

        content = ViewUtil.getClearPanel();
        content.setLayout(new MigLayout("insets 0, fillx, hidemode 3"));

        JPanel fixedWidthContainer;

        if (fixedWidth) {
            fixedWidthContainer = ViewUtil.getDefaultFixedWidthPanel(content);
        } else {
            fixedWidthContainer = ViewUtil.getFixedWidthPanel(content, -1);
        }

        StandardAppContainer sac = new StandardAppContainer(fixedWidthContainer, doesScroll);
        sac.setBackground(ViewUtil.getLightGrayBackgroundColor());

        titleLabel =  ViewUtil.getLargeSerifLabel("");
        titleLabel.setVisible(false);
        if (title != null) {
            setTitle(title);
        }

        content.add(titleLabel, "wrap");

        this.setLayout(new BorderLayout());
        this.add(sac, BorderLayout.CENTER);

        initialized = true;
    }

    @Override
    public void setLayout(LayoutManager mgr) {
        if (initialized) {
            throw new UnsupportedOperationException("Not allowed to change layout for this component");
        } else {
            super.setLayout(mgr);
        }
    }

    public JPanel addBlock() {
        return addBlock(null);
    }

    public JPanel addBlock(String blockTitle) {
        JPanel p = ViewUtil.getWhiteLineBorderedPanel();
        JPanel canvas = ViewUtil.getClearPanel();
        canvas.setLayout(new MigLayout("insets 0"));
        p.setLayout(new MigLayout("fillx, wrap"));

        if (blockTitle != null) {
            JLabel l = new JLabel(blockTitle);
            l.setText(blockTitle);
            l.setFont(ViewUtil.getMediumTitleFont());
            p.add(l);
        }

        p.add(canvas, "width 100%");
        content.add(p, "width 100%, wrap");
        return canvas;
    }

    public void setTitle(String string) {
        titleLabel.setText(string);
        ViewUtil.ellipsizeLabel(titleLabel, 800);
        titleLabel.setVisible(true);
    }

}
