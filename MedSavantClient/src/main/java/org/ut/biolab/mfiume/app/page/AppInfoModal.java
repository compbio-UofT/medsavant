package org.ut.biolab.mfiume.app.page;

import com.explodingpixels.macwidgets.MacUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.mfiume.app.AppInfo;
import org.ut.biolab.mfiume.app.AppStoreViewManager;

/**
 *
 * @author mfiume
 */
public class AppInfoModal extends JDialog {

    private final AppStoreInstalledPage installedPage;
    private final AppStoreViewManager avm;

    public AppInfoModal(final AppInfo i, final AppStoreInstalledPage installedPage, final AppStoreViewManager avm) {
        super((JFrame) null, i.getName(), true);

        //MacUtils.makeWindowLeopardStyle(this.getRootPane());
        this.setModal(true);

        this.setResizable(false);
        this.setBackground(Color.white);

        this.installedPage = installedPage;
        this.avm = avm;

        Dimension d = new Dimension(400, 400);
        this.setPreferredSize(d);
        this.setMinimumSize(d);

        this.setLocationRelativeTo(null);
        //this.setLayout(new BorderLayout());

        JPanel mig = new JPanel();
        mig.setBackground(Color.white);
        //mig.setOpaque(false);

        mig.setLayout(new MigLayout("wrap 1"));

        // bold
        JLabel nameLabel = new JLabel(i.getName() + " " + i.getVersion());
        Font font = nameLabel.getFont();

        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        Font smallFont = new Font(font.getFontName(), Font.PLAIN, font.getSize() - 3);
        Font mediumFont = new Font(font.getFontName(), Font.PLAIN, font.getSize() - 2);

        nameLabel.setFont(boldFont);

        // small, gray
        JLabel categoryLabel = new JLabel(i.getCategory());
        categoryLabel.setForeground(Color.darkGray);
        categoryLabel.setFont(smallFont);

        // gray
        JLabel authorLabel = new JLabel("Developed by " + i.getAuthor());
        authorLabel.setFont(mediumFont);
        authorLabel.setForeground(Color.darkGray);

        JTextArea description = new JTextArea();
        description.setEditable(false);
        description.setFocusable(false);
        description.setOpaque(false);
        description.setLineWrap(true);
        description.setText(i.getDescription());


        JButton downloadButton = getSoftButton("Install App");
        JButton moreInfo = getSoftButton("More Info");

        final JDialog thisInstance = this;

        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                installedPage.queueAppForInstallation(i);
                thisInstance.setVisible(false);
                avm.switchToPage(installedPage);
            }
        });

        JPanel actionBar = new JPanel();
        actionBar.setOpaque(false);
        actionBar.setLayout(new BoxLayout(actionBar, BoxLayout.X_AXIS));

        actionBar.add(downloadButton);

        mig.add(nameLabel);
        mig.add(categoryLabel);
        mig.add(Box.createVerticalStrut(3));
        mig.add(authorLabel);
        mig.add(Box.createVerticalStrut(3));
        mig.add(description,"width 100%");
        mig.add(actionBar);

        this.add(mig);
    }

    public static JButton getSoftButton(String string) {
        JButton b = new JButton(string);
        b.putClientProperty("JButton.buttonType", "segmentedRoundRect");
        b.putClientProperty("JButton.segmentPosition", "only");
        b.setFocusable(false);
        b.putClientProperty("JComponent.sizeVariant", "small");
        return b;
    }
}
