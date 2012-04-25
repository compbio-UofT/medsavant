/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

/**
 *
 * @author mfiume
 */
public class DrillDownItemRenderer extends JPanel implements ListCellRenderer {
  protected Color m_c = Color.black;

  public DrillDownItemRenderer() {
    super();
    setBorder(new CompoundBorder(
        new MatteBorder(2, 10, 2, 10, Color.white), new LineBorder(
            Color.black)));
  }

  public Component getListCellRendererComponent(JList list, Object obj,
      int row, boolean sel, boolean hasFocus) {
    if (obj instanceof Color)
      m_c = (Color) obj;
    return this;
  }

  public void paint(Graphics g) {
    setBackground(m_c);
    super.paint(g);
  }

  public static void main(String[] a) {
    JComboBox cbColor = new JComboBox();
    int[] values = new int[] { 0, 128, 192, 255 };
    for (int r = 0; r < values.length; r++)
      for (int g = 0; g < values.length; g++)
        for (int b = 0; b < values.length; b++) {
          Color c = new Color(values[r], values[g], values[b]);
          cbColor.addItem(c);
        }
    cbColor.setRenderer(new DrillDownItemRenderer());
    
    JFrame f = new JFrame();
    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    f.getContentPane().add(cbColor);
    f.pack();
    f.setSize(new Dimension(300, 80));
    f.show();    
    
  }
}
