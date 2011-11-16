package org.ut.biolab.medsavant.view.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class CollapsablePanel extends JPanel {
    private final JPanel titlePanel;
    private final JPanel titleButtonsPanel;
    private final JPanel contentPanel;
    
    private final boolean isCollapsable;
    private final JButton expandButton;
    private final JButton collapseButton;
    
    private final MouseListener toggler = new MouseListener() {

            public void mouseClicked(MouseEvent me) {
                setContentPaneVisible(!contentPanel.isVisible());
            }

            public void mousePressed(MouseEvent me) {
            }

            public void mouseReleased(MouseEvent me) {
            }

            public void mouseEntered(MouseEvent me) {
            }

            public void mouseExited(MouseEvent me) {
            }
            
        };
    private final JLabel descriptionLabel;
    
    public CollapsablePanel(String title) {
        this(title,true);
    }
    
    public CollapsablePanel(String title, boolean isCollapsable) {
        ViewUtil.applyVerticalBoxLayout(this);
        ViewUtil.clear(this);
        
        titlePanel = ViewUtil.getSecondaryBannerPanel();//new JPanel();
        ViewUtil.applyHorizontalBoxLayout(titlePanel);
        titlePanel.setBorder(BorderFactory.createCompoundBorder(
                ViewUtil.getTinyLineBorder(),
                ViewUtil.getMediumBorder()));
        
        this.isCollapsable = isCollapsable;
        
        if (isCollapsable) {
            expandButton = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.EXPAND));
            collapseButton = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.COLLAPSE));
            
            titlePanel.add(expandButton);
            titlePanel.add(collapseButton);
            
            expandButton.addMouseListener(toggler);
            collapseButton.addMouseListener(toggler);
            
            
        } else {
            expandButton = null;
            collapseButton = null;
        }
        
        // title label
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ViewUtil.getMediumTitleFont());
        titlePanel.add(titleLabel);
        
        descriptionLabel = new JLabel("");
        //titleLabel.setForeground(Color.white);
        
        // place for buttons on the right
        titleButtonsPanel = (JPanel) ViewUtil.clear(new JPanel());
        
        ViewUtil.applyHorizontalBoxLayout(titleButtonsPanel);
        titlePanel.add(titleLabel);
        titlePanel.add(ViewUtil.getMediumSeparator());
        titlePanel.add(descriptionLabel);
        titlePanel.add(Box.createHorizontalGlue());
        titlePanel.add(titleButtonsPanel);
        
        contentPanel = new JPanel();
        contentPanel.setBorder(ViewUtil.getMediumBorder());
        ViewUtil.applyVerticalBoxLayout(contentPanel);
        
        contentPanel.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0,1,1,1,Color.lightGray), 
                    ViewUtil.getMediumBorder()));
        contentPanel.setBackground(Color.white);
        
        setContentPaneVisible(true);
        
        this.add(titlePanel);
        this.add(contentPanel);
    }
    
    public JPanel getContentPane() {
        return contentPanel;
    }
    
    public final void setContentPaneVisible(boolean b) {
        if (isCollapsable) {
            contentPanel.setVisible(b);
            expandButton.setVisible(!b);
            collapseButton.setVisible(b);
        }
    }
    
    public final void setDescription(String s) {
        if (s.isEmpty()) {
            this.descriptionLabel.setText("");
        } else {
            this.descriptionLabel.setText("(" + s + ")");
        }
    }
}
