/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 *
 * All rights reserved. No warranty, explicit or implicit, provided. THE
 * SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE FOR
 * ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.app.google;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.controller.SettingsController;
import org.ut.biolab.medsavant.client.view.util.PathField;
import org.ut.biolab.medsavant.client.view.util.StandardFixableWidthAppPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import savant.util.swing.HyperlinkButton;

/**
 *
 * @author mfiume
 */
public class SettingsView extends JPanel {
    private JLabel currentLabel;
    public static String KEY_CLIENT_SECRETS = "KEY_CLIENT_SECRETS";
    
    private ReadsView readsView;

    public SettingsView(ReadsView rv) {
        this.readsView = rv;
        this.setBackground(Color.white);
        init();
    }

    private void init() {
        StandardFixableWidthAppPanel p = new StandardFixableWidthAppPanel("Settings");

        JPanel authPanel = p.addBlock("Authentication");

        JLabel l0 = new JLabel("To authenticate, you must generate and download a client_secrets.json file.");
        JLabel l00 = new JLabel("To do so, follow the instructions");
        JLabel l2 = new JLabel("then specify the location of the file below:");
        JComponent l1 = HyperlinkButton.createHyperlinkButton("here", Color.blue, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    java.awt.Desktop.getDesktop().browse(new URI("https://developers.google.com/genomics/"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
        });

        final PathField f = new PathField(JFileChooser.OPEN_DIALOG);
        
        JButton ok = new JButton("Set");
        
        authPanel.add(f);

        authPanel.add(l0,"wrap");
        authPanel.add(l00,"split, gapx 0");
        authPanel.add(l1,"gapx 0");
        authPanel.add(l2,"wrap");
        authPanel.add(f,"split, width 500");
        authPanel.add(ok,"wrap");
        
        ok.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SettingsController.getInstance().setValue(KEY_CLIENT_SECRETS, f.getPath());
                readsView.updateGenomicsService();
                f.setPath("");
                updateCurrentLabel();
            }
            
        });
        
        currentLabel = new JLabel();
        authPanel.add(currentLabel,"wrap");
        
        this.setLayout(new BorderLayout());
        this.add(p,BorderLayout.CENTER);
        
        updateCurrentLabel();
    }

    private void updateCurrentLabel() {
        String clientSecretsFile = SettingsController.getInstance().getValue(KEY_CLIENT_SECRETS);
        if (clientSecretsFile == null) {
            clientSecretsFile = "<none>";
        }
        currentLabel.setText("<html><b>client_secrets.json:</b> " + clientSecretsFile + "</html>");
    }

}
