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
package org.ut.biolab.mfiume.app.page;

import com.explodingpixels.macwidgets.MacUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.mfiume.app.AppInfo;
import org.ut.biolab.mfiume.app.AppStoreViewManager;

/**
 *
 * @author mfiume
 */
public class AppInfoModal extends JDialog {

    private final AppStoreInstalledPage installedPage;
    private final JTabbedPane parent;
    private final JButton downloadButton;
    private final InstallActionListener ail;

    public AppInfoModal(final AppInfo i, final AppStoreInstalledPage installedPage, final JTabbedPane avm) {
        super((JFrame) null, i.getName(), true);

        //MacUtils.makeWindowLeopardStyle(this.getRootPane());
        this.setModal(true);

        this.setResizable(false);
        this.setBackground(Color.white);

        this.installedPage = installedPage;
        this.parent = avm;

        Dimension d = new Dimension(400, 400);
        this.setPreferredSize(d);
        this.setMinimumSize(d);

        this.setLocationRelativeTo(null);
        //this.setLayout(new BorderLayout());

        JPanel mig = new JPanel();
        mig.setBackground(Color.white);
        //mig.setOpaque(false);

        mig.setLayout(new MigLayout("wrap 1"));

        // bold
        JLabel nameLabel = new JLabel("<html><b>" + i.getName() + "</b> " + i.getVersion() + "</html>");
        Font font = nameLabel.getFont();

        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        Font smallFont = new Font(font.getFontName(), Font.PLAIN, font.getSize() - 3);
        Font mediumFont = new Font(font.getFontName(), Font.PLAIN, font.getSize() - 2);


        // small, gray
        JLabel categoryLabel = new JLabel(i.getCategory());
        categoryLabel.setForeground(Color.darkGray);
        categoryLabel.setFont(smallFont);

        // gray
        JLabel authorLabel = new JLabel("Developed by " + i.getAuthor());
        authorLabel.setFont(mediumFont);
        authorLabel.setForeground(Color.darkGray);

        JTextArea description = new JTextArea();
        description.setEditable(false);
        description.setFocusable(false);
        description.setOpaque(false);
        description.setLineWrap(true);
        description.setText(i.getDescription());

        downloadButton = AppInfoFlowView.getSoftButton("Install App");
        JButton moreInfo = AppInfoFlowView.getSoftButton("More Info");

        final JDialog thisInstance = this;

        ail = new InstallActionListener(installedPage,i,avm);
        downloadButton.addActionListener(ail);

        JPanel actionBar = new JPanel();
        actionBar.setOpaque(false);
        actionBar.setLayout(new BoxLayout(actionBar, BoxLayout.X_AXIS));

        actionBar.add(downloadButton);

        mig.add(nameLabel);
        mig.add(categoryLabel);
        mig.add(Box.createVerticalStrut(3));
        mig.add(authorLabel);
        mig.add(Box.createVerticalStrut(3));
        mig.add(description, "width 100%");
        mig.add(actionBar);

        this.add(mig);
    }

    final void setInstalled(boolean installedAlready) {
        if (installedAlready) {
            downloadButton.setEnabled(false);
            downloadButton.setText("Installed");
            downloadButton.updateUI();
            downloadButton.invalidate();
        }
    }

    void setUpdateAllowed(boolean canUpdate) {
        if (canUpdate) {
            downloadButton.setText("Update");
            ail.setModeToUpdate(true);
        }
    }
}
