package org.ut.biolab.medsavant.view.subview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class SubSectionViewCollection extends SubSectionView {

    private final String name;
    Map<String, SubSectionView> subsectionMap;
    private JPanel panel;
    private JPanel menuPanel;
    private Component glue;
    private final ButtonGroup buttonGroup;
    private JPanel contentPanel;

    public SubSectionViewCollection(SectionView parent, String name) {
        super(parent);
        this.name = name;
        subsectionMap = new HashMap<String, SubSectionView>();

        buttonGroup = new ButtonGroup();

        initView();

    }

    public void addSubSectionView(SubSectionView v) {

        this.subsectionMap.put(v.getName(), v);

        menuPanel.remove(glue);

        // init the view
        v.getView(true);

        final String title = v.getName();

        JRadioButton button = new JRadioButton(v.getName()) {

            @Override
            public void paintComponent(Graphics g) {

                String title = this.getText();

                Color textColor = Color.BLACK;
                Color bgColor = new Color(255,255,255,255);
                if (this.isSelected()) {
                    textColor = Color.white;
                    bgColor = Color.gray;
                }

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = g2d.getFontMetrics().stringWidth(title);
                int height = g2d.getFontMetrics().getAscent();

                int hpad = 8;
                int vpad = 4;

                if (this.isSelected()) {
                    g2d.setColor(bgColor);
                    g2d.fillRoundRect(0, 0, width+2*hpad, height+2*vpad, 20, 20);
                }

                g2d.setColor(textColor);
                g2d.drawString(title, hpad, height+vpad/2);

            }
        };

        buttonGroup.add(button);
        menuPanel.add(button);

        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                setPage(title);
            }

        });

        menuPanel.add(Box.createHorizontalStrut(5));
        menuPanel.add(glue);
    }

    int blah = 0;

    void setPage(String pageName) {
        SubSectionView view = this.subsectionMap.get(pageName);
        contentPanel.removeAll();

        contentPanel.add(view.getView(true),BorderLayout.CENTER);
        contentPanel.updateUI();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getView(boolean update) {
        return panel;
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
    }

    private void initView() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        menuPanel = new JPanel();//ViewUtil.getQuaternaryBannerPanel();
        menuPanel.setBorder(ViewUtil.getSmallBorder());
        ViewUtil.applyHorizontalBoxLayout(menuPanel);
        menuPanel.add(Box.createHorizontalGlue());

        glue = Box.createHorizontalGlue();
        menuPanel.add(glue);

        panel.add(menuPanel, BorderLayout.NORTH);

        contentPanel = new JPanel();
        //contentPanel.setBackground(Color.red);
        contentPanel.setLayout(new BorderLayout());

        panel.add(contentPanel, BorderLayout.CENTER);
    }
}
