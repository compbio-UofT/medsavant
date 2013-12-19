package org.ut.biolab.medsavant.client.view.dashboard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class Dashboard extends JPanel {

    int appIconWidth = 130;
    int numIconsPerRow = 6;
    int gapX = 30;
    int gapY = 30;


    private final ArrayList<DashboardSection> dashboardSections;
    private final JPanel baseLayer;
    private DashboardApp previousApp;
    private final JPanel appLayer;

    private final String BASE_LAYER = "0";
    private final String APP_LAYER = "1";
    private final CardLayout cardLayout;

    public Dashboard() {

        this.setBackground(Color.white);
        cardLayout = new CardLayout();
        this.setLayout(cardLayout);

        dashboardSections = new ArrayList<DashboardSection>();

        baseLayer = new JPanel();
        baseLayer.setBackground(Color.white);
        this.add(ViewUtil.getClearBorderlessScrollPane(baseLayer), BASE_LAYER);

        appLayer = new JPanel();
        appLayer.setBackground(Color.white);
        appLayer.setLayout(new BorderLayout());

        this.add(appLayer, APP_LAYER);

        cardLayout.show(this, BASE_LAYER);
    }

    public void addDashboardSection(DashboardSection s) {
        this.dashboardSections.add(s);
        this.relayout();
    }

    private void relayout() {
        baseLayer.removeAll();

        JPanel middlePane = ViewUtil.getClearPanel();
        middlePane.setLayout(new MigLayout("gapy 30"));

        baseLayer.setOpaque(true);

        baseLayer.setLayout(new MigLayout());

        for (DashboardSection s : this.dashboardSections) {

            if (s.getApps().isEmpty()) { continue; }

            JPanel appPlaceholder = ViewUtil.getClearPanel();

            MigLayout layout = new MigLayout("gapx 30, gapy 30, wrap " + numIconsPerRow);
            appPlaceholder.setLayout(layout);
            for (DashboardApp launcher : s.getApps()) {
                appPlaceholder.add(getRepresentationForLauncher(launcher));
            }
            middlePane.add(appPlaceholder,"wrap");
        }

        JScrollPane p = ViewUtil.getClearBorderlessScrollPane(middlePane);
        p.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        int xoffset = ((this.appIconWidth+gapX)*this.numIconsPerRow-gapX)/2;

        baseLayer.add(p,"gaptop min(200,20%), gapbottom min(200,20%), x ((container.w)/2)-" + xoffset);

        //baseLayer.add(p,"id app, gaptop min(200,20%), gapbottom min(200,20%), x (((container.w)/2)-(app.w/2))");

    }

    public void goHome() {
        //System.out.println("Going home");
        MedSavantFrame.getInstance().setTitle("MedSavant");

        previousApp = null;
        cardLayout.show(this, BASE_LAYER);
    }

    private void launchApp(DashboardApp app) {

        //System.out.println("Launching app");

        if (previousApp != null) { previousApp.viewWillUnload(); }
        app.viewWillLoad();
        appLayer.removeAll();

        MedSavantFrame.getInstance().setTitle(app.getName());

        JPanel p = app.getView();

        JButton home = new JButton("Home");
        home.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                goHome();
            }
        });

        appLayer.add(ViewUtil.alignLeft(home), BorderLayout.NORTH);
        appLayer.add(p, BorderLayout.CENTER);
        cardLayout.show(this, APP_LAYER);

        if (previousApp != null) { previousApp.viewDidUnload(); }
        app.viewDidLoad();
        previousApp = app;
    }

    private JPanel getRepresentationForLauncher(final DashboardApp launcher) {
        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(p);

        JButton button = ViewUtil.getIconButton(resizeIconTo(launcher.getIcon(),appIconWidth));
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                launchApp(launcher);
            }
        });

        p.add(ViewUtil.centerHorizontally(button));
        p.add(Box.createVerticalStrut(3));

        JLabel title = ViewUtil.getGrayLabel(launcher.getName());
        p.add(ViewUtil.centerHorizontally(title));
        return p;
    }

    private ImageIcon resizeIconTo(ImageIcon icon, int itemSize) {
        Image img = icon.getImage();
        Image newimg = img.getScaledInstance(itemSize, itemSize, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(newimg);
    }

}
