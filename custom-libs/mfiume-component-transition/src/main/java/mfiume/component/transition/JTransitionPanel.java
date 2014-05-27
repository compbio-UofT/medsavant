/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package mfiume.component.transition;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author mfiume
 */
public class JTransitionPanel extends JPanel {

    private static final float DEFAULT_SLIDE_DURATION = 50.0f;

    // holds all the layers, including the animation panel
    private final JLayeredPane layers;
      
    // the panel currently on display
    private JPanel currentPanel;
    
    private final JPanel placeHolderOne; // holds the incoming panel, for layout purposes
    private final JPanel placeHolderTwo; // holds the current panel
    //private final JPanel blankOutCanvas; // blank out the previous panels, while animating
    private final JPanel animationCanvas; // where animation happens

    private void addPanelAndFill(JComponent child, JComponent parent) {
        parent.removeAll();
        parent.setLayout(new BorderLayout());
        parent.add(child,BorderLayout.CENTER);
        parent.updateUI();
    }

    public enum TransitionType {

        PUSH_LEFT,
        PUSH_RIGHT,
        PUSH_UP,
        PUSH_DOWN,
        ZOOM_IN,
        ZOOM_OUT,
        SLIDE_LEFT,
        SLIDE_RIGHT,
        SLIDE_UP,
        SLIDE_DOWN,
        FADE_IN,
        FADE_OUT,
        NONE
    }
    
    @Override
    public void setBackground(Color c) {
        //if (blankOutCanvas != null) { blankOutCanvas.setBackground(c); }
        super.setBackground(c);
    }

    public JTransitionPanel() {
        currentPanel = null;
        this.setDoubleBuffered(true);
        layers = new JLayeredPane();
        addPanelAndFill(layers,this);
        
        placeHolderOne = new JPanel();
        placeHolderTwo = new JPanel();
        //blankOutCanvas = new JPanel();
        animationCanvas = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                // do nothing
                System.out.println("Flash");
            }
        };
        animationCanvas.setOpaque(false);
        
        //blankOutCanvas.setVisible(true);
        
        layers.add(placeHolderOne,0,0);
        layers.add(placeHolderTwo,1,0);
        //layers.add(blankOutCanvas,2,0);
        layers.add(animationCanvas,2,0);
        
        this.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
                rebound();
            }
            public void componentMoved(ComponentEvent e) {
            }
            public void componentShown(ComponentEvent e) {
            }
            public void componentHidden(ComponentEvent e) {
            }
        });
    }
    
    private void rebound() {
        for (Component c : layers.getComponents()) {
            c.setBounds(layers.getBounds());
        }
        this.updateUI();
    }

    /**
     * Replace the existing panel with a new one, animating the transition as
     * specified.
     *
     * @param newPanel The new panel to show
     * @param type The type of transition to use to show it
     * @param doneListener The action to take when the transition is complete
     */
    public void push(final JPanel newPanel, TransitionType type, final ActionListener doneListener) {

        // TODO: layout the new panel before it is rendered
        //blankOutCanvas.setVisible(true);
        //addPanelAndFill(newPanel,placeHolderOne);

        ActionListener uberListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                final JTransitionPanel instance = JTransitionPanel.this;
                
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        placeHolderOne.removeAll();
                        addPanelAndFill(newPanel,placeHolderTwo);
                        //blankOutCanvas.setVisible(false);
                        animationCanvas.setVisible(false);
                        instance.setBackground(placeHolderTwo.getBackground());
                        instance.updateUI();
                    }

                });

                if (doneListener != null) {
                    doneListener.actionPerformed(e);
                }
            }
        };

        Point origin = new Point(0, 0);
        switch (type) {
            case SLIDE_LEFT:
                Animator.animateSlide(
                        animationCanvas,
                        currentPanel, newPanel,
                        DEFAULT_SLIDE_DURATION,
                        uberListener,
                        new Point(this.getWidth(), 0),
                        origin);
                break;
            case SLIDE_RIGHT:
                Animator.animateSlide(
                        animationCanvas,
                        currentPanel, newPanel,
                        DEFAULT_SLIDE_DURATION,
                        uberListener,
                        new Point(-this.getWidth(), 0),
                        origin);
                break;
            case SLIDE_UP:
                Animator.animateSlide(
                        this,
                        currentPanel, newPanel,
                        DEFAULT_SLIDE_DURATION,
                        uberListener,
                        new Point(0, this.getHeight()),
                        origin);
                break;
            case SLIDE_DOWN:
                Animator.animateSlide(
                        animationCanvas,
                        currentPanel, newPanel,
                        DEFAULT_SLIDE_DURATION,
                        uberListener,
                        new Point(0, -this.getHeight()),
                        origin);
                break;
            case PUSH_LEFT:
                Animator.animatePush(
                        animationCanvas,
                        currentPanel, newPanel,
                        DEFAULT_SLIDE_DURATION,
                        uberListener,
                        new Point(this.getWidth(), 0),
                        origin);
                break;
            case PUSH_RIGHT:
                Animator.animatePush(
                        animationCanvas,
                        currentPanel, newPanel,
                        DEFAULT_SLIDE_DURATION,
                        uberListener,
                        new Point(-this.getWidth(), 0),
                        origin);
                break;
            case PUSH_UP:
                Animator.animatePush(
                        animationCanvas,
                        currentPanel, newPanel,
                        DEFAULT_SLIDE_DURATION,
                        uberListener,
                        new Point(0, this.getHeight()),
                        origin);
                break;
            case PUSH_DOWN:
                Animator.animatePush(
                        animationCanvas,
                        currentPanel, newPanel,
                        DEFAULT_SLIDE_DURATION,
                        uberListener,
                        new Point(0, -this.getHeight()),
                        origin);
                break;
            case FADE_IN:
                Animator.animateAlpha(
                        animationCanvas,
                        currentPanel, newPanel,
                        DEFAULT_SLIDE_DURATION,
                        uberListener,
                        0f,
                        1.0f);
                break;
            case FADE_OUT:
                Animator.animateAlpha(
                        animationCanvas,
                        newPanel,currentPanel,
                        DEFAULT_SLIDE_DURATION,
                        uberListener,
                        1.0f,
                        0f);
                break;
            case NONE:
                uberListener.actionPerformed(null);
                break;
            default:
                throw new UnsupportedOperationException("Transition " + type + " not supported yet");
        }

        currentPanel = newPanel;
    }

    public static void main(String[] v) {
        final JTransitionPanel p = new JTransitionPanel();

        final JPanel red = new JPanel();
        red.setBackground(new Color(60, 60, 60));

        final JPanel yellow = new JPanel();
        yellow.setBackground(new Color(100, 100, 100));

        p.push(red, TransitionType.NONE, null);

        JButton doIt = new JButton("Do it");
        red.add(doIt);

        doIt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                p.push(yellow, TransitionType.PUSH_RIGHT, null);
            }
        });

        for (int i = 0; i < 30; i++) {
            JButton doIt2 = new JButton("Do it " + i);
            yellow.add(doIt2);
            doIt2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    p.push(red, TransitionType.PUSH_LEFT, null);
                }
            });
        }

        JFrame f = new JFrame();

        f.add(p);
        f.pack();
        f.setVisible(true);
    }

}
