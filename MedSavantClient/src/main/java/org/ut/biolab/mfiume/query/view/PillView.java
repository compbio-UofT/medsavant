package org.ut.biolab.mfiume.query.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.mfiume.query.img.ImagePanel;

/**
 *
 * @author mfiume
 */
public class PillView extends JPanel {

    public final static Color[] COLOR_SCHEME_INACTIVE = new Color[]{new Color(241, 215, 215), new Color(233, 139, 139), new Color(233, 49, 49), new Color(239, 97, 97)};
    public final static Color[] COLOR_SCHEME_ACTIVE = new Color[]{new Color(222, 231, 241), new Color(166, 190, 236), new Color(49, 121, 233), new Color(97, 155, 239)}; // 0 is background, 1 is border
    int backoff = 1;
    int vpad = 2;
    int hpad = 7;
    private boolean isDisclosureVisible;
    private final JPanel leftPanel;
    private final JPanel middlePanel;
    private final JPanel rightPanel;
    private PopupGenerator popupGenerator;
    private boolean isActivated;
    private boolean isSelected;
    private JLabel textLabel;
    private boolean expandable = false;
    private JButton expandButton;

    public PillView() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(vpad, hpad, vpad, hpad));
        this.setOpaque(false);

        this.leftPanel = new JPanel();
        this.middlePanel = new JPanel();
        this.rightPanel = new JPanel();

        leftPanel.setOpaque(false);
        middlePanel.setOpaque(false);
        rightPanel.setOpaque(false);

        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.X_AXIS));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));

        this.add(leftPanel);
        this.add(middlePanel);

        this.add(rightPanel);

        setIsDisclosureVisible(true);

        final PillView instance = this;

        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.getButton() == me.BUTTON1) {       
                    //do nothing?
                } else if (me.getButton() == me.BUTTON2) {                    
                    showPopup();
                }else if(me.getButton() == me.BUTTON3){
                    showPopup();
                }
            }

            @Override
            public void mousePressed(MouseEvent me) {
            }

            @Override
            public void mouseReleased(MouseEvent me) {
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            @Override
            public void mouseExited(MouseEvent me) {
            }
        });

        setSelected(false);
    }

    public PillView(boolean expandable) {
        this();
        ImageIcon ic = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.COLLAPSE);
        expandButton = ViewUtil.getIconButton(ic);
        
        expandButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {                                    
                toggleExpandButton(!expandState);
                updateUI();
            }
        });                            
    }

    public void setActivated(boolean b) {
        this.isActivated = b;
    }

    public void showPopup() {
        
        if (popupGenerator != null) {
            if (this.isDisclosureVisible) {                
                final JPopupMenu m = popupGenerator.generatePopup();
                final PillView instance = this;
                m.addPopupMenuListener(new PopupMenuListener() {
                    @Override
                    public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
                        setSelected(true);

                    }

                    @Override
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {
                        setSelected(false);
                    }

                    @Override
                    public void popupMenuCanceled(PopupMenuEvent pme) {
                        setSelected(false);
                    }
                });
                m.show(this, 0, this.getHeight());

                final JComponent pillInstance = this;
                this.addComponentListener(new ComponentListener() {
                    @Override
                    public void componentResized(ComponentEvent ce) {
                    }

                    @Override
                    public void componentMoved(ComponentEvent ce) {
                        if (m.isVisible()) {
                            m.show(pillInstance, 0, pillInstance.getHeight());
                        }
                    }

                    @Override
                    public void componentShown(ComponentEvent ce) {
                    }

                    @Override
                    public void componentHidden(ComponentEvent ce) {
                    }
                });

            }
        }
    }

    public void setSelected(boolean b) {
        this.isSelected = b;

        if (textLabel != null) {
            if (b) {
                textLabel.setForeground(Color.white);
            } else {
                textLabel.setForeground(Color.darkGray);
            }
        }
        updateDisclosure();
        this.updateUI();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Color[] currentColorSheme = this.isActivated ? COLOR_SCHEME_ACTIVE : COLOR_SCHEME_INACTIVE;

        int bend = this.getHeight();

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isSelected) {

            Color bottom = currentColorSheme[2];
            Color top = currentColorSheme[3];
            GradientPaint redtowhite = new GradientPaint(
                    0, 0, top,
                    0, (int) this.getHeight(), bottom);
            g2.setPaint(redtowhite);

        } else {
            g2.setColor(currentColorSheme[0]);
        }
        g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), bend, bend);
        g2.setColor(currentColorSheme[1]);
        RoundRectangle2D rec2 = new RoundRectangle2D.Double(0, 0, this.getWidth() - backoff, this.getHeight() - backoff, bend, bend);
        g2.draw(rec2);
        g2.clip(rec2);

        // hack to make sure text is the right color
        // even though setSelected should take care of this
        if (textLabel != null) {
            if (isSelected) {
                textLabel.setForeground(Color.white);
            } else {
                textLabel.setForeground(Color.darkGray);
            }
        }
    }

    private void setIsDisclosureVisible(boolean b) {
        this.isDisclosureVisible = b;
        updateDisclosure();
    }

    private boolean expandState = false;
    
    private void toggleExpandButton(boolean expand) {
        if(expandButton != null){
           // System.out.println("\ttoggleExpand: "+expandState+" => "+expand);
            expandState = expand;        
            ImageIcon ic = IconFactory.getInstance().getIcon(expand ? IconFactory.StandardIcon.COLLAPSE : IconFactory.StandardIcon.EXPAND);       
            expandButton.setIcon(ic);
        }
    }
    
    
    public void setExpandListener(ActionListener l){       
        if(expandButton != null){
            expandButton.addActionListener(l);
        }
    }

    public void toggleExpandButton(){
        toggleExpandButton(!expandState);
        /*System.out.println("toggle expand doClick()");
        if(expandButton != null){
            expandButton.doClick();
        }*/
    }
    
    public void expand() {        
       // System.out.println("expand()");
        toggleExpandButton(true);
        updateUI();        
    }

    public void collapse() {
        //System.out.println("collapse()");
        toggleExpandButton(false);
        updateUI();
    }

   
    
    public void setText(String text) {
        this.middlePanel.removeAll();
        if(expandButton != null){
            this.middlePanel.add(expandButton);
        }
               
        textLabel = new JLabel(text);        
        textLabel.setFont(new Font(textLabel.getFont().getFamily(), Font.PLAIN, 13));
        textLabel.setOpaque(false);

        this.middlePanel.add(textLabel);
        updateUI();
    }

    private void setLeftPanel(JComponent c) {
        this.leftPanel.removeAll();
        if (c != null) {
            this.leftPanel.add(c);
            this.leftPanel.add(Box.createHorizontalStrut(4));
        }
        updateUI();
    }

    private void setRightPanel(JComponent c) {
        this.rightPanel.removeAll();
        if (c != null) {
            this.rightPanel.add(Box.createHorizontalStrut(4));
            this.rightPanel.add(c);
        }
        updateUI();
    }

    private void updateDisclosure() {
        this.leftPanel.removeAll();
        if (true) {
            return;
        }
        if (this.isDisclosureVisible) {
            if (this.isSelected) {
                setLeftPanel(new ImagePanel("disclosure.png"));
            } else {
                setLeftPanel(new ImagePanel("disclosure-gray.png"));
            }
        } else {
            setLeftPanel(null);
        }
    }

    public void setPopupGenerator(PopupGenerator pg) {
        this.popupGenerator = pg;
    }
}
