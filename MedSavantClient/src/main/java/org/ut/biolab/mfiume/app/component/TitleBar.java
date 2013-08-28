package org.ut.biolab.mfiume.app.component;

import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class TitleBar extends JPanel {

    public TitleBar(String title) {

        this.setOpaque(false);
        this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        JLabel titleLabel = new JLabel(title);

        Font font = titleLabel.getFont();
        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize()+5);
        titleLabel.setFont(boldFont);

        //this.add(Box.createHorizontalStrut(5));
        this.add(titleLabel);
        this.add(Box.createHorizontalGlue());
    }
}
