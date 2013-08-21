package org.ut.biolab.mfiume.app.page;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.mfiume.app.AppInfo;
import org.ut.biolab.mfiume.app.AppStoreViewManager;
import org.ut.biolab.mfiume.app.component.RoundedJPanel;
import org.ut.biolab.mfiume.app.jAppStore;

/**
 *
 * @author mfiume
 */
class AppInfoFlowView extends JPanel {
    private final AppStoreInstalledPage installedPage;
    private final AppStoreViewManager avm;

    public AppInfoFlowView(final AppInfo i, final AppStoreViewManager avm, final AppStoreInstalledPage installedPage) {
        super();

        this.avm = avm;
        this.installedPage = installedPage;

        Dimension d = new Dimension(220, 90);
        this.setPreferredSize(d);
        this.setMinimumSize(d);
        this.setMaximumSize(d);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.setBackground(Color.white);
        int border = 10;
        this.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));

        // bold
        JLabel nameLabel = new JLabel(i.getName() + " " + i.getVersion());
        Font font = nameLabel.getFont();



        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        Font smallFont = new Font(font.getFontName(), Font.PLAIN, font.getSize()-3);
        Font mediumFont = new Font(font.getFontName(), Font.PLAIN, font.getSize()-2);

        nameLabel.setFont(boldFont);

        // small, gray
        JLabel categoryLabel = new JLabel(i.getCategory());
        categoryLabel.setForeground(Color.darkGray);
        categoryLabel.setFont(smallFont);

        // gray
        JLabel authorLabel = new JLabel(i.getAuthor());
        authorLabel.setFont(mediumFont);
        authorLabel.setForeground(Color.darkGray);

        JButton downloadButton = getSoftButton("Install App");
        JButton moreInfo = getSoftButton("More Info");

        downloadButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                installedPage.queueAppForInstallation(i);
                avm.switchToPage(installedPage);
            }
        });

        moreInfo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                AppInfoModal aim = new AppInfoModal(i, installedPage, avm);
                aim.setVisible(true);
            }

        });

        JPanel actionBar = new JPanel();
        actionBar.setOpaque(false);
        actionBar.setLayout(new BoxLayout(actionBar,BoxLayout.X_AXIS));
                actionBar.add(moreInfo);

        actionBar.add(Box.createHorizontalGlue());
                actionBar.add(downloadButton);

        this.add(getLeftAlignedComponent(nameLabel));
        this.add(getLeftAlignedComponent(categoryLabel));
        this.add(Box.createVerticalStrut(3));
        this.add(getLeftAlignedComponent(authorLabel));
        this.add(actionBar);
        jAppStore.wrapComponentWithLineBorder(this);
    }

    public static JButton getSoftButton(String string) {
        JButton b = new JButton(string);
        b.putClientProperty("JButton.buttonType", "segmentedRoundRect");
        b.putClientProperty("JButton.segmentPosition", "only");
        b.setFocusable(false);
        b.putClientProperty("JComponent.sizeVariant", "small");
        return b;
    }

    public static JPanel getLeftAlignedComponent(Component c) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(c);
        p.add(Box.createHorizontalGlue());
        return p;
    }
}
