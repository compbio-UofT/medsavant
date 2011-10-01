/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view;

import org.ut.biolab.medsavant.model.event.SectionChangedEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.model.event.SectionChangedEventListener;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class SplitView extends JPanel {
    private final JPanel pageCard;
    private final JPanel left;
    private final CardLayout pageCardLayout;
    private final ButtonGroup bg;
    private final ArrayList<SectionChangedEventListener> listeners;
    private final Map<String,JRadioButton> sectionCheckBoxMap;
    private String currentSection;
    private CardLayout bannerCardLayout;
    private JPanel bannerCard;
    private final JPanel bannerContainer;
    private final JLabel sectionTitleLabel;

    public SplitView() {
        left = new JPanel();
        left.setBorder(new EmptyBorder(10,10,10,10));
        left.setLayout(new BoxLayout(left,BoxLayout.Y_AXIS));

        bannerContainer = ViewUtil.getSecondaryBannerPanel();
        bannerContainer.setLayout(new BoxLayout(bannerContainer,BoxLayout.X_AXIS));
        bannerContainer.setBorder(new EmptyBorder(5,3,5,3));
        sectionTitleLabel = (JLabel) ViewUtil.clear(new JLabel("Title"));
        sectionTitleLabel.setFont(ViewUtil.getBigTitleFont());//new Font("Arial", Font.BOLD, 18));
        sectionTitleLabel.setForeground(Color.darkGray);
        bannerContainer.add(sectionTitleLabel);
        bannerCardLayout = new CardLayout();
        bannerCard = new JPanel(bannerCardLayout);
        bannerCard.setOpaque(false);
        bannerContainer.add(bannerCard);

        left.setBackground(new Color(217,222,229));
        pageCardLayout = new CardLayout();
        pageCard = new JPanel(pageCardLayout);
        pageCard.setBackground(Color.gray);

        JPanel cardContainer = new JPanel();
        cardContainer.setLayout(new BorderLayout());
        cardContainer.add(pageCard, BorderLayout.CENTER);
        cardContainer.add(bannerContainer, BorderLayout.NORTH);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                           left, cardContainer);

        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(200);
        this.setLayout(new BorderLayout());
        this.add(splitPane,BorderLayout.CENTER);
        bg = new ButtonGroup();
        listeners = new ArrayList<SectionChangedEventListener>();
        sectionCheckBoxMap = new TreeMap<String,JRadioButton>();
    }

    public void addSection(String sectionName) {
        if (left.getComponentCount() != 0) { left.add(Box.createVerticalStrut(10)); }
        JLabel l = new JLabel(sectionName.toUpperCase());
        l.setFont(new Font("Arial", Font.BOLD, 14));
        left.add(l);
    }

    public void setSubsection(String subsectionName) {
        if (this.sectionCheckBoxMap.containsKey(subsectionName)) {
           // if (currentSection == null || !currentSection.equals(subsectionName)) {
                sectionTitleLabel.setText(subsectionName);
                pageCardLayout.show(pageCard, subsectionName);
                bannerCardLayout.show(bannerCard, subsectionName);
                this.sectionCheckBoxMap.get(subsectionName).setSelected(true);
                fireSectionChangedEvent(new SectionChangedEvent(subsectionName));
                currentSection = subsectionName;
          //  }
        }
    }

    public void addSubsection(final String subsectionName, SubSectionView view) {
        JRadioButton b = (JRadioButton) ViewUtil.clear(new JRadioButton(subsectionName));
        b.setFont(new Font("Arial", Font.PLAIN, 14));
        b.setForeground(Color.darkGray);
        b.setAlignmentX(0F);
        bg.add(b);
        left.add(b);
        //bannerCard.add(view.getBanner(), subsectionName);
        pageCard.add(view.getView(false),subsectionName);
        b.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JRadioButton b2 = (JRadioButton) e.getSource();
                if (b2.isSelected() && (currentSection == null || !currentSection.equals(subsectionName))) {
                    setSubsection(subsectionName);
                }
            }

        });
        sectionCheckBoxMap.put(subsectionName, b);
    }

    public void addSectionChangedListener(SectionChangedEventListener l) {
        listeners.add(l);
    }

    public void fireSectionChangedEvent(SectionChangedEvent e) {
        for (SectionChangedEventListener l : listeners) {
            l.sectionChangedEventReceived(e);
        }
    }

}
