package org.ut.biolab.mfiume.query;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import org.ut.biolab.mfiume.query.view.ScrollableJPopupMenu;

/**
 *
 * @author mfiume
 */
public class TestScroll {

    public static void main(String[] args) {
        final ScrollableJPopupMenu m = new ScrollableJPopupMenu();

        /*m.setPreferredSize(new Dimension(300,500));
        m.setMinimumSize(new Dimension(300,500));
        m.setMaximumSize(new Dimension(300,500));
        */
        JFrame f = new JFrame();

        final JButton b = new JButton("b");
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                m.removeAll();
                for (int i = 0; i < 240; i++) {
            m.addComponent(new JMenuItem(i + " "));
        }
                m.show(b, 0, 23);
                System.out.println("M has " + m.getComponentCount() + " cs");
            }
        });
        f.add(b);
        f.pack();
        f.setVisible(true);


    }
}
