/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.mfiume.query.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.ut.biolab.medsavant.client.filter.SearchBar;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
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
    private final static int MAXIMUM_HEIGHT = 100; //*maximum* height, in pixels.
    private final static int LEFT_BORDER_OFFSET = 10;
    public static final int BACKOFF = 1;
    public static final int VPAD = 2;
    public static final int HPAD = 7;
    private static final int DIALOG_HEIGHT = 384;
    private static final int DIALOG_WIDTH = 512;
    private boolean isDisclosureVisible;
    private final JPanel leftPanel;
    private final JPanel middlePanel;
    private final JPanel rightPanel;
    private ConditionEditorDialogGenerator dialogGenerator;
    private ConditionPopupGenerator popupGenerator;
    private boolean isActivated;
    private boolean isSelected;
    private JLabel textLabel;
    private JLabel expandButton;
    private JButton editButton;
    private JLabel infoButton;
    private String info;
    private final Component infoButtonPadding;

    public static int getAllowedWidth(int depth) {
        int w = SearchBar.getInstance().getWidth() - 2 * HPAD;
        for (int i = 0; i < depth; ++i) {
            w -= (HPAD + BACKOFF);
        }
        return w;
    }

    public final void indent(int depth) {
        this.setMaximumSize(new Dimension(getAllowedWidth(depth), MAXIMUM_HEIGHT));
    }

    public PillView() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(VPAD, HPAD, VPAD, HPAD));
        this.setOpaque(false);

        this.leftPanel = new JPanel();
        this.middlePanel = new JPanel();
        this.rightPanel = new JPanel();

        leftPanel.setOpaque(false);
        middlePanel.setOpaque(false);
        middlePanel.setBorder(new EmptyBorder(0, LEFT_BORDER_OFFSET, 0, 0));
        rightPanel.setOpaque(false);

        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.X_AXIS));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));

        this.add(leftPanel);
        this.add(middlePanel);

        this.add(rightPanel);

        setIsDisclosureVisible(true);

        indent(0);
        //setMaximumWidth(SearchBar.getInstance().getWidth() - 2 * HPAD);


        final PillView instance = this;

        /*
         this.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent me) {
         if (me.getButton() == me.BUTTON1) {
         //do nothing?
         } else if (me.getButton() == me.BUTTON2) {
         showDialog(instance.getLocationOnScreen());
         } else if (me.getButton() == me.BUTTON3) {
         showDialog(instance.getLocationonScreen());
         }
         }
         });
         */

        ImageIcon icinfo = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.INFO);
        infoButton = ViewUtil.createIconButton(icinfo);
        infoButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showInfoPopup();
            }
        });
        infoButton.setVisible(false);

        infoButtonPadding = Box.createHorizontalStrut(2);
        infoButtonPadding.setVisible(infoButton.isVisible());

        editButton = ViewUtil.getConfigureButton();
        editButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (dialogGenerator != null) {
                    showDialog(instance.getLocationOnScreen());
                } else if (popupGenerator != null) {
                    showPopup();
                }
            }
        });

        this.setRightPanel(ViewUtil.horizontallyAlignComponents(new Component[]{infoButton, infoButtonPadding, editButton}));
    }

    public PillView(boolean expandable) {
        this();
        ImageIcon ic = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.COLLAPSE);
        expandButton = ViewUtil.createIconButton(ic);
        expandButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                toggleExpandButton(!expandState);
                updateUI();
            }
        });
    }

    public void setActivated(boolean b) {
        this.isActivated = b;
    }

    public void setInfo(String info) {
        this.infoButton.setVisible(true);
        this.infoButtonPadding.setVisible(infoButton.isVisible());
        this.info = info;
    }

    public void showInfoPopup() {
        JDialog d = ViewUtil.getHUD(this.infoButton, this.textLabel.getText(), info);
        d.setVisible(true);
    }

    public void showPopup() {

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

            m.show(this, ViewUtil.getPositionRelativeTo(instance, editButton).x, this.getHeight());

            final JComponent pillInstance = this;
            this.addComponentListener(new ComponentListener() {
                @Override
                public void componentResized(ComponentEvent ce) {
                }

                @Override
                public void componentMoved(ComponentEvent ce) {
                    if (m.isVisible()) {
                        m.show(pillInstance, editButton.getX(), pillInstance.getHeight());
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
      
    public void showDialog(Point p){
        showDialog(p, "Editing Condition: ");
    }
    
    public void showDialog(Point p, String title) {
        if (dialogGenerator != null) {
            if (this.isDisclosureVisible) {
                
                final JDialog jd = dialogGenerator.generateDialog();     
                jd.setTitle(title);
                jd.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                jd.pack();
                int y = p.y + this.getHeight() + 2;
                int frameHeight = MedSavantFrame.getInstance().getHeight();
                if ((y + DIALOG_HEIGHT) > (frameHeight)) {
                    y = frameHeight - DIALOG_HEIGHT - 10;
                }
                jd.setLocation(new Point(p.x, y));                                
                setSelected(true);
                repaint();
                jd.addWindowListener(new WindowAdapter() {
                    
                    @Override
                    public void windowClosing(WindowEvent e) {                                
                        setSelected(false);                        
                        jd.dispose();
                        repaint();
                    }
                });
                jd.setVisible(true);
                jd.requestFocus();
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
        RoundRectangle2D rec2 = new RoundRectangle2D.Double(0, 0, this.getWidth() - BACKOFF, this.getHeight() - BACKOFF, bend, bend);
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
        if (expandButton != null) {
            expandState = expand;
            ImageIcon ic = IconFactory.getInstance().getIcon(expand ? IconFactory.StandardIcon.COLLAPSE : IconFactory.StandardIcon.EXPAND);
            expandButton.setIcon(ic);
        }
    }

    public void setExpandListener(final ActionListener l) {
        if (expandButton != null) {
            expandButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    l.actionPerformed(null);
                }
            });
        }
    }

    public void toggleExpandButton() {
        toggleExpandButton(!expandState);
    }

    public void expand() {
        toggleExpandButton(true);
        updateUI();
    }

    public void collapse() {
        toggleExpandButton(false);
        updateUI();
    }

    public void setText(String text) {
        this.middlePanel.removeAll();
        if (expandButton != null) {
            this.middlePanel.add(expandButton);
        }

        //String shortenedText = ViewUtil.ellipsize(text, 50);

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

    public void setDialogGenerator(ConditionEditorDialogGenerator pg) {
        this.dialogGenerator = pg;
    }

    public void setPopupGenerator(ConditionPopupGenerator cpg) {
        this.popupGenerator = cpg;
    }
}
