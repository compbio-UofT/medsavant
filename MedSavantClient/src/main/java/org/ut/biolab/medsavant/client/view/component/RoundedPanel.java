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
package org.ut.biolab.medsavant.client.view.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class RoundedPanel extends JPanel {

   private final int radius;


   public RoundedPanel(int cornerRadius) {
      radius=cornerRadius;
   }

   public void paintComponent(Graphics g) {
       /*
        Color bg = getBackground();
        g.setColor(new Color(bg.getRed(),bg.getGreen(),bg.getBlue(),40));
        g.fillRoundRect(0,0, getWidth()-1, getHeight()-1, radius, radius);
        g.setColor(new Color(0,0,0,70));
        g.drawRoundRect(0,0, getWidth()-1, getHeight()-1, radius, radius);
        *
        */

       Graphics2D g2 = (Graphics2D) g;
       g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg = new Color(237,237,237,0);//getBackground();
        g2.setColor(new Color(bg.getRed(),bg.getGreen(),bg.getBlue()));
        g2.fillRoundRect(0,0, getWidth()-1, getHeight()-1, radius, radius);
        g2.setColor(new Color(164,164,164));
        g2.drawRoundRect(0,0, getWidth()-1, getHeight()-1, radius, radius);
   }

   public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(400, 300);
        frame.setLocation(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel content = new JPanel();
        JPanel wl = new JPanel();
        JPanel el = new JPanel();
        JPanel sl = new JPanel();
        JPanel nl = new JPanel();
        RoundedPanel rp = new RoundedPanel(50);
        JComboBox combobox = new JComboBox();

        frame.setContentPane(content);
        content.setBackground(Color.red);
        content.setLayout(new BorderLayout());
        wl.add(new JButton("west"));
        el.add(new JButton("east"));
        sl.add(new JButton("south"));
        nl.add(new JButton("north"));
        content.add(wl,BorderLayout.WEST);
        content.add(el,BorderLayout.EAST);
        content.add(nl,BorderLayout.NORTH);
        content.add(sl,BorderLayout.SOUTH);

        content.add(rp,BorderLayout.CENTER);
        rp.setBackground(Color.BLACK);

        combobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Třída 1.B", "Třída 1.C", "Třída 2.C" }));
        rp.add(combobox);
        frame.setVisible(true);
    }
}