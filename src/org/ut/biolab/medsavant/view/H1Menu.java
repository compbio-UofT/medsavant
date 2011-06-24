/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;
import org.ut.biolab.medsavant.view.util.PaintUtil;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
class H1Menu extends JPanel {

    public H1Menu() {
        this.setPreferredSize(new Dimension(40,40));
        this.setBorder(ViewUtil.getMediumBorder());
        this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        
        String[] colors = { "Patients", "Variants", "Annotations" };
        final JComboBox comboBox = new JComboBox(colors);
        comboBox.setFont(new Font("Tahoma",Font.BOLD,16));
        //comboBox.setBackground(Color.darkGray);
        //comboBox.setForeground(Color.white);
        comboBox.setPreferredSize(new Dimension(200,25));
        comboBox.setMaximumSize(new Dimension(200,25));
        comboBox.setRenderer(new FancyCellRenderer());
        this.add(comboBox);
        this.add(Box.createHorizontalGlue());
    }
    
    @Override
    public void paintComponent(Graphics g) {
        PaintUtil.paintDarkMenu(g,this);
    } 
    
}

class FancyCellRenderer implements ListCellRenderer {
  protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

  // Width doesn't matter as the combo box will size
  private final static Dimension preferredSize = new Dimension(0, 20);

  public Component getListCellRendererComponent(JList list, Object value, int index,
      boolean isSelected, boolean cellHasFocus) {

   Component renderer = ViewUtil.getDropDownPanel((String) value,isSelected,cellHasFocus);
    renderer.setPreferredSize(preferredSize);
    return renderer;
  }
}
