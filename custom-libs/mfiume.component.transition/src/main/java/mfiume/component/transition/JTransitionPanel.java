/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mfiume.component.transition;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author mfiume
 */
public class JTransitionPanel extends JPanel {

    private static final float DEFAULT_SLIDE_DURATION = 100.0f;

    private JPanel currentPanel;

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

    public JTransitionPanel() {
        currentPanel = null;
        this.setDoubleBuffered(true);
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

        final JPanel previousPanel = currentPanel;

        // TODO: layout the new panel before it is rendered
        final JTransitionPanel instance = this;
        ActionListener uberListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (previousPanel != null) {
                    instance.remove(previousPanel);
                }
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        instance.setLayout(new BorderLayout());
                        instance.add(newPanel, BorderLayout.CENTER);
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
                        this,
                        currentPanel, newPanel,
                        DEFAULT_SLIDE_DURATION,
                        uberListener,
                        new Point(this.getWidth(), 0),
                        origin);
                break;
            case SLIDE_RIGHT:
                Animator.animateSlide(
                        this,
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
                        this,
                        currentPanel, newPanel,
                        DEFAULT_SLIDE_DURATION,
                        uberListener,
                        new Point(0, -this.getHeight()),
                        origin);
                break;
            case PUSH_LEFT:
                Animator.animatePush(
                        this,
                        currentPanel, newPanel,
                        DEFAULT_SLIDE_DURATION,
                        uberListener,
                        new Point(this.getWidth(), 0),
                        origin);
                break;
            case PUSH_RIGHT:
                Animator.animatePush(
                        this,
                        currentPanel, newPanel,
                        DEFAULT_SLIDE_DURATION,
                        uberListener,
                        new Point(-this.getWidth(), 0),
                        origin);
                break;
            case PUSH_UP:
                Animator.animatePush(
                        this,
                        currentPanel, newPanel,
                        DEFAULT_SLIDE_DURATION,
                        uberListener,
                        new Point(0, this.getHeight()),
                        origin);
                break;
            case PUSH_DOWN:
                Animator.animatePush(
                        this,
                        currentPanel, newPanel,
                        DEFAULT_SLIDE_DURATION,
                        uberListener,
                        new Point(0, -this.getHeight()),
                        origin);
                break;
            case FADE_IN:
                Animator.animateAlpha(
                        this,
                        currentPanel, newPanel,
                        DEFAULT_SLIDE_DURATION,
                        uberListener,
                        0f,
                        1.0f);
                break;
            case FADE_OUT:
                Animator.animateAlpha(
                        this,
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
                p.push(yellow, TransitionType.FADE_IN, null);
            }
        });

        for (int i = 0; i < 30; i++) {
            JButton doIt2 = new JButton("Do it " + i);
            yellow.add(doIt2);
            doIt2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    p.push(red, TransitionType.FADE_OUT, null);
                }
            });
        }

        JFrame f = new JFrame();

        f.add(p);
        f.pack();
        f.setVisible(true);
    }

}
