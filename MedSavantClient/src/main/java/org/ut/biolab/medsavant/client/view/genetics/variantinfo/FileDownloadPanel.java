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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * Deprecated 3/7/2013 in favor of Notifications (i.e. jobs)
 * @author jim
 */
@Deprecated
public class FileDownloadPanel extends JPanel implements PropertyChangeListener {

    private JLabel heading;
    private JProgressBar progressBar;
    private DownloadTask fileDownloadTask;

    private void init() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        add(heading);
        add(progressBar);
        updateLabel(0);
    }

    private String getMegs(long bytes) {
        String m = Integer.toString((int) Math.round((double) bytes / 1e5));
        return m.substring(0, m.length() - 1) + "." + m.charAt(m.length() - 1) + "M";
    }

    private void updateLabel(long bytesFetched) {
        String s = getMegs(bytesFetched) + " / " + getMegs(fileDownloadTask.getFileSize());
        progressBar.setString(s);
        repaint();
    }

    protected void setLabel(String s) {
        progressBar.setString(s);
        repaint();
    }

    public FileDownloadPanel(String heading, DownloadTask dt) throws IOException {
        super();
        this.fileDownloadTask = dt;
        this.heading = new JLabel(heading);
        init();
        fileDownloadTask.addPropertyChangeListener(this);
    }

    public void download() {
        fileDownloadTask.execute();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();            
            progressBar.setValue(progress);
            updateLabel(fileDownloadTask.getTotalBytesSoFar());
        }
    }
}
