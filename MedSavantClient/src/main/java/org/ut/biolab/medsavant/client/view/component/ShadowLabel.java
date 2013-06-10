package org.ut.biolab.medsavant.client.view.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;

public class ShadowLabel extends JLabel {

    private String text;

    private Font f;

    private boolean invertColors = false;

    public ShadowLabel() {
      super();
    }

    public ShadowLabel(String text, int size) {
        super();
        this.text = text;
        f = new Font("Dialog", 1, size);
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2D = (Graphics2D) g;
        // ////////////////////////////////////////////////////////////////
        // antialiasing
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);
        // ////////////////////////////////////////////////////////////////

        /**
         * draw text
         */
        if (!invertColors) {
          g2D.setFont(f);
          g2D.setColor(new Color(0, 0, 0));
          g2D.drawString(this.text, 1, 11);
          g2D.setColor(new Color(255, 255, 255, 230));
          g2D.drawString(this.text, 0, 10);
        } else {
          g2D.setFont(f);
          g2D.setColor(new Color(255, 255, 255, 230));
          g2D.drawString(this.text, 1, 11);
          g2D.setColor(new Color(0, 0, 0));
          g2D.drawString(this.text, 0, 10);
        }
        g2D.dispose();

    }

    public void setInvertColors(boolean invertColors) {
    this.invertColors = invertColors;
  }

  public void setText(String text) {
        this.text = text;
        repaint();
    }

    /**
     * Default UID
     */
    private static final long serialVersionUID = 1L;

}
