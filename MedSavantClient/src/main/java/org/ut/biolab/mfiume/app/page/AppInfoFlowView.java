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
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.mfiume.app.AppInfo;
import org.ut.biolab.mfiume.app.AppStoreViewManager;
import org.ut.biolab.mfiume.app.component.RoundedJPanel;
import org.ut.biolab.mfiume.app.jAppStore;

/**
 *
 * @author mfiume
 */
class AppInfoFlowView extends RoundedJPanel {

    private final AppStoreInstalledPage installedPage;
    private final AppStoreViewManager avm;
    private final JButton downloadButton;
    private final AppInfoModal aim;
    private final InstallActionListener ial;

    public AppInfoFlowView(final AppInfo i, final AppStoreViewManager avm, final AppStoreInstalledPage installedPage, boolean installedAlready, boolean canUpdate) {
        super();

        this.avm = avm;
        this.installedPage = installedPage;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.setBackground(Color.white);
        int border = 20;
        this.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));

        // bold
        String shortName = ViewUtil.ellipsize(i.getName(), 24);
        JLabel nameLabel = new JLabel("<html><b>" + shortName + "</b> " + i.getVersion() + "</html>");
        if (!shortName.equals(i.getName())) {
            nameLabel.setToolTipText(i.getName());
        }
        Font font = nameLabel.getFont();

        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        Font smallFont = new Font(font.getFontName(), Font.PLAIN, font.getSize() - 3);
        Font mediumFont = new Font(font.getFontName(), Font.PLAIN, font.getSize() - 2);


        // small, gray
        JLabel categoryLabel = new JLabel(i.getCategory());
        categoryLabel.setForeground(Color.darkGray);
        categoryLabel.setFont(smallFont);

        // gray
        JLabel authorLabel = new JLabel(i.getAuthor());
        ViewUtil.shortenLabelToLength(authorLabel,30);
        authorLabel.setFont(mediumFont);
        authorLabel.setForeground(Color.darkGray);

        downloadButton = getSoftButton("Install App");
        JButton moreInfo = getSoftButton("More Info");

        ial = new InstallActionListener(installedPage, i, avm);
        downloadButton.addActionListener(ial);

        aim = new AppInfoModal(i, installedPage, avm);

        moreInfo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                aim.setVisible(true);
            }
        });

        if (installedAlready) {
            if (canUpdate) {
                setUpdateAllowed(true);
            } else {
                setInstalled(true);
            }
        }

        JPanel actionBar = new JPanel();
        actionBar.setOpaque(false);
        actionBar.setLayout(new BoxLayout(actionBar, BoxLayout.X_AXIS));
        actionBar.add(moreInfo);

        actionBar.add(Box.createHorizontalGlue());
        actionBar.add(downloadButton);

        this.add(Box.createRigidArea(new Dimension(220,1)));
        this.add(getLeftAlignedComponent(nameLabel));
        this.add(getLeftAlignedComponent(categoryLabel));
        this.add(Box.createVerticalStrut(3));
        this.add(getLeftAlignedComponent(authorLabel));
        this.add(actionBar);

        //jAppStore.wrapComponentWithLineBorder(this);
        this.setBackground(Color.yellow);

        this.updateUI();
    }

    public static JButton getSoftButton(String string) {
        JButton b = new JButton(string);
        //b.putClientProperty("JButton.buttonType", "segmentedRoundRect");
        //b.putClientProperty("JButton.segmentPosition", "only");
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

    final void setInstalled(boolean installedAlready) {
        if (installedAlready) {
            downloadButton.setEnabled(false);
            downloadButton.setText("Installed");
            downloadButton.updateUI();
            downloadButton.invalidate();
        }
        aim.setInstalled(installedAlready);
    }

    private void setUpdateAllowed(boolean canUpdate) {
        if (canUpdate) {
            downloadButton.setText("Update");
            ial.setModeToUpdate(true);
        }
        aim.setUpdateAllowed(canUpdate);
    }
}
