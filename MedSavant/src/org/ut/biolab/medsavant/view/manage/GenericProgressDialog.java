/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.view.manage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
class GenericProgressDialog extends JDialog {

    private final JPanel buttonPane;
    private final JButton okButton;
    private final JButton cancelButton;
    private CancelRequestListener cancelRequestListener;
    private boolean cancelRequested;
    private final WaitPanel wp;
    private boolean isComplete;

    void setCancelListener(CancelRequestListener cancelRequestListener) {
         this.cancelRequestListener = cancelRequestListener;
    }

    boolean wasCancelRequested() {
        return this.cancelRequested;
    }

    void setStatus(String string) {
        if (!this.wasCancelRequested() && !isComplete) {
            wp.setStatus(string);
        }
    }
    
    public interface CancelRequestListener {
        public void cancelRequested();
    }
    
    
    public GenericProgressDialog(String title, String message) {
        
        this.setTitle(title);
        this.setLayout(new BorderLayout());
        
        this.setMinimumSize(new Dimension(300,200));
        this.setPreferredSize(new Dimension(300,200));
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        cancelRequested = false;
        
        wp = new WaitPanel(message);
                
        this.add(wp,BorderLayout.CENTER);
        
        buttonPane = ViewUtil.getSecondaryBannerPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane,BoxLayout.X_AXIS));
        buttonPane.add(Box.createHorizontalGlue());
        
        okButton = new JButton("OK");
        okButton.setVisible(false);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               closeDialog();
            }
            
        });
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               cancelRequestListener.cancelRequested();
               cancelRequested = true;
               wp.setStatus("Cancelled");
               wp.setComplete();
               cancelButton.setVisible(false);
               okButton.setVisible(true);
            }
        });
        
        buttonPane.add(okButton);
        buttonPane.add(cancelButton);
        
        this.add(buttonPane,BorderLayout.SOUTH);
        
        this.setLocationRelativeTo(null);
        
    }
    
    private void closeDialog() {
        this.dispose();
    }
    
    public void setComplete() {
        okButton.setVisible(true);
        cancelButton.setVisible(false);
        wp.setStatus("Complete");
        wp.setComplete();
        this.isComplete = true;
    }
}
