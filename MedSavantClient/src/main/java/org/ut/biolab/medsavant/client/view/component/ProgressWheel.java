package org.ut.biolab.medsavant.client.view.component;

import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.images.ImagePanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ProgressWheel extends JPanel {

    public ProgressWheel() {
        Dimension d = new Dimension(23,23);
        this.setPreferredSize(d);
        this.setMaximumSize(d);
        this.setMinimumSize(d);
        this.setBorder(null);

        ImageIcon waitGif = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.WAIT);
        ImagePanel p = new ImagePanel(waitGif.getImage(),23,8);
        
        this.add(ViewUtil.centerVertically(p));
    }

    public void setComplete() {
        this.removeAll();
    }

    public void setIndeterminate(boolean b) {

    }

    public void setValue(int d) {

    }

    public int getMaximum() {
        return 100;
    }

    public boolean isIndeterminate() {
        return true;
    }
}
