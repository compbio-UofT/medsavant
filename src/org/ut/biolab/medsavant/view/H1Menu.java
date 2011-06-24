/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view;

import com.jidesoft.plaf.basic.ThemePainter;
import com.jidesoft.swing.JideSplitButton;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ListCellRenderer;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
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
        
        //this.add(new H1MenuItem("Patients"));
        
        ButtonGroup group = new ButtonGroup();
        H1MenuItem b1 = new H1MenuItem("Patients",-1); group.add(b1);
        H1MenuItem b2 = new H1MenuItem("Genetics",0); group.add(b2);
        H1MenuItem b3 = new H1MenuItem("Annotate",1); group.add(b3);
        
        this.add(Box.createHorizontalGlue());
        
        this.add(b1);
        this.add(b2);
        this.add(b3);
        
        /*
        this.add(new JRadioButton("Patients"));
        
        String[] colors = { "Patients", "Variants", "Annotations" };
        final JComboBox comboBox = new JComboBox(colors);
        comboBox.setFont(new Font("Tahoma",Font.BOLD,16));
        //comboBox.setBackground(Color.darkGray);
        //comboBox.setForeground(Color.white);
        comboBox.setPreferredSize(new Dimension(200,25));
        comboBox.setMaximumSize(new Dimension(200,25));
        comboBox.setRenderer(new FancyCellRenderer());
        //this.add(comboBox);
        
        this.add(createJideSplitButton("Patients",null));
         * 
         */
        
        this.add(Box.createHorizontalGlue());
    }
    
    @Override
    public void paintComponent(Graphics g) {
        PaintUtil.paintDarkMenu(g,this);
    }
    
    static JideSplitButton createJideSplitButton(String name, Icon icon) {
        final JideSplitButton button = new JideSplitButton(name);
        button.setForegroundOfState(ThemePainter.STATE_DEFAULT, Color.WHITE);
        button.setFont(new Font("Tahoma",Font.BOLD,18));
        button.setBackgroundOfState(0, Color.yellow);
        button.setAlwaysDropdown(true);
        button.setIcon(icon);
        button.add(new AbstractAction("Genetic Variants") {
            public void actionPerformed(ActionEvent e) {
            }
        });
        button.add(new AbstractAction("Annotations") {
            public void actionPerformed(ActionEvent e) {
            }
        });
        return button;
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


