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
package org.ut.biolab.medsavant.client.app.page;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.ut.biolab.medsavant.client.app.AppInfo;
import org.ut.biolab.medsavant.client.app.jAppStore;
import org.ut.biolab.medsavant.client.view.component.ProgressWheel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class AppInstallProgressView extends JPanel {

    public AppInstallProgressView(AppInfo i) {
        this.setBackground(Color.white);
        int padding = 10;
        this.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));

        this.add(new JLabel("<html><b>" + i.getName() + "</b> " + i.getVersion() + " <small> by " + i.getAuthor() + "</small></html>"));
        this.add(Box.createHorizontalGlue());
        this.add(getIndeterminantProgressBar());
        jAppStore.wrapComponentWithLineBorder(this);
    }

    private ProgressWheel getIndeterminantProgressBar() {

        return ViewUtil.getIndeterminateProgressBar();

    }

}
