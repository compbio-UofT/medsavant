/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.importfile;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import javax.swing.JDialog;

/**
 *
 * @author Andrew
 */
public class ImportFileView extends JDialog {
    
    private ImportFilePanel panel;
    
    public ImportFileView(Window parent, String title) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        this.setTitle(title);
        initGUI();
        this.setLocationRelativeTo(null);
    }
 
    private void initGUI(){
        
        setMinimumSize(new Dimension(600, 600));
        setPreferredSize(new Dimension(600, 600));
        setLayout(new BorderLayout());
        
        panel = new ImportFilePanel();
        add(panel, BorderLayout.CENTER);
    }
    
    public ImportFilePanel getImportFilePanel() {
        return panel;
    }
    
}
