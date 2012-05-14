package org.ut.biolab.medsavant.view.component;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JComponent;

public class HoverButton extends JButton {

    private boolean over;
    private Font f;
    String face = "Arial";
    int size = 15;
    int style = Font.PLAIN;

    public void setForeColor(Color c) {
        this.textColor = c;
    }

    public void setFontSize(int size) {
        this.size = size;
        resetFont();

    }

    public void setFontStyle(int style) {
        this.style = style;
        resetFont();
    }

    public HoverButton(String s) {
        super(s);

        resetFont();

        this.setOpaque(false);
        final JComponent instance = this;
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {

                over = true;
                instance.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                over = false;
                instance.repaint();
            }
        });
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    Color textColorUnselected = Color.gray;
    Color textColor = Color.white;
    Color bgColor = new Color(255, 255, 255, 255);

    @Override
    public void paintComponent(Graphics g) {

        String title = this.getText();

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setFont(f);

        int width = g2d.getFontMetrics().stringWidth(title);
        int height = g2d.getFontMetrics().getAscent();

        if (over || this.isSelected()) {
            g2d.setColor(textColor);
        } else {
            g2d.setColor(textColorUnselected);
        }


        g2d.drawString(title, 8, (getHeight() + height) / 2 - 2);

    }

    private void resetFont() {
        f = new Font(face, style, size);
    }

}
