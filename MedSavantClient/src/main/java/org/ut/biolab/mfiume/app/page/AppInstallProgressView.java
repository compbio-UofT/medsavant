package org.ut.biolab.mfiume.app.page;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.ut.biolab.mfiume.app.AppInfo;
import org.ut.biolab.mfiume.app.jAppStore;

/**
 *
 * @author mfiume
 */
public class AppInstallProgressView extends JPanel {

    public AppInstallProgressView(AppInfo i) {
        this.setBackground(Color.white);
        int padding = 10;
        this.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));

        this.add(new JLabel("<html><b>" + i.getName() + "</b> " + i.getVersion() + " <small> by " + i.getAuthor() + "</small></html>"));
        this.add(Box.createHorizontalGlue());
        this.add(getIndeterminantProgressBar());
        jAppStore.wrapComponentWithLineBorder(this);
    }

    private JProgressBar getIndeterminantProgressBar() {

        JProgressBar b = new JProgressBar();
        b.setIndeterminate(true);

        b.putClientProperty("JProgressBar.style", "circular");

        return b;

    }

}
