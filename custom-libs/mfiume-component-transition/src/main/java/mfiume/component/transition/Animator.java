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

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import mfiume.component.transition.accessor.FloatAccessor;
import mfiume.component.transition.accessor.PointAccessor;
import mfiume.component.transition.painter.AlphaBasedPainterPanel;
import mfiume.component.transition.painter.PositionBasedPainterPanel;

/**
 *
 * @author mfiume
 */
public class Animator {

    private static final int TIMER_UPDATE_FREQUENCY = 2;

    static void animateSlide(final JPanel canvas, final JPanel previous, final JPanel current, final float duration, final ActionListener completionCallback, final Point startPoint, final Point endPoint) {

        final PositionBasedPainterPanel painter = new PositionBasedPainterPanel();

        Tween.registerAccessor(Point.class, new PointAccessor());
        final TweenManager manager = new TweenManager();

        painter.setNewOrigin(startPoint);
        painter.setNewImageSource(current, canvas.getSize());

        final Runnable animationPanelRepainter = new Runnable() {

            @Override
            public void run() {
                Graphics g = canvas.getGraphics();
                painter.paintComponent(g);
                g.dispose();
            }

        };

        Tween.to(startPoint, PointAccessor.TWEET_TYPE_BASIC, duration)
                .target(endPoint.x, endPoint.y)
                .start(manager);

        final long startTime = System.currentTimeMillis();

        final Timer animationTimer = new Timer(TIMER_UPDATE_FREQUENCY, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (startPoint.x == endPoint.x && startPoint.y == endPoint.y) {
                    ((Timer) e.getSource()).stop();
                    ((Timer) e.getSource()).stop();
                    if (completionCallback != null) {
                        completionCallback.actionPerformed(e);
                    }
                    return;
                }
                manager.update((System.currentTimeMillis() - startTime) / 1000f); // elapsed seconds
                try {
                    SwingUtilities.invokeLater(animationPanelRepainter);
                } catch (Exception ex) {
                }
            }

        });
        animationTimer.setRepeats(true);
        animationTimer.start();
    }

    static void animatePush(final JPanel canvas, final JPanel previous, final JPanel current, final float duration, final ActionListener completionCallback, final Point startPoint, final Point endPoint) {

        final PositionBasedPainterPanel painter = new PositionBasedPainterPanel();

        Tween.registerAccessor(Point.class, new PointAccessor());
        final TweenManager manager = new TweenManager();

        painter.setNewOrigin(startPoint);
        painter.setNewImageSource(current, canvas.getSize());
        painter.setPreviousImageSource(previous, canvas.getSize());

        final int xDifference = endPoint.x - startPoint.x;
        final int yDifference = endPoint.y - startPoint.y;

        painter.setPreviousOrigin(new Point() {
            public double getX() {
                return startPoint.x + xDifference;
            }

            public double getY() {
                return startPoint.y + yDifference;
            }
        });

        final Runnable animationPanelRepainter = new Runnable() {

            @Override
            public void run() {
                Graphics g = canvas.getGraphics();
                painter.paintComponent(g);
                g.dispose();
            }

        };

        Tween.to(startPoint, PointAccessor.TWEET_TYPE_BASIC, duration)
                .target(endPoint.x, endPoint.y)/*.ease(Quad.INOUT)*/
                .start(manager);

        final long startTime = System.currentTimeMillis();

        final Timer animationTimer = new Timer(TIMER_UPDATE_FREQUENCY, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (startPoint.x == endPoint.x && startPoint.y == endPoint.y) {
                    ((Timer) e.getSource()).stop();
                    if (completionCallback != null) {
                        completionCallback.actionPerformed(e);
                    }
                    return;
                }
                manager.update((System.currentTimeMillis() - startTime) / 1000f); // elapsed seconds
                try {
                    SwingUtilities.invokeLater(animationPanelRepainter);
                } catch (Exception ex) {
                }
            }

        });
        animationTimer.setRepeats(true);
        animationTimer.start();
    }
    
    static void animateAlpha(final JPanel canvas, final JPanel previous, final JPanel current, final float duration, final ActionListener completionCallback, final Float startAlpha, final Float endAlpha) {

        final AlphaBasedPainterPanel painter = new AlphaBasedPainterPanel();

        Tween.registerAccessor(FloatAccessor.FloatStruct.class, new FloatAccessor());
        final TweenManager manager = new TweenManager();

        final FloatAccessor.FloatStruct struct = new FloatAccessor.FloatStruct(startAlpha);
        
        painter.setNewImageAlpha(struct);
        painter.setNewImageSource(current, canvas.getSize());
        painter.setPreviousImageSource(previous, canvas.getSize());

        final Runnable animationPanelRepainter = new Runnable() {

            @Override
            public void run() {
                Graphics g = canvas.getGraphics();
                painter.paintComponent(g);
                g.dispose();
            }

        };

        Tween.to(struct, PointAccessor.TWEET_TYPE_BASIC, duration)
                .target(endAlpha)/*.ease(Quad.INOUT)*/
                .start(manager);

        final long startTime = System.currentTimeMillis();

        final Timer animationTimer = new Timer(TIMER_UPDATE_FREQUENCY, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (struct.getFloat() == endAlpha) {
                    ((Timer) e.getSource()).stop();
                    if (completionCallback != null) {
                        completionCallback.actionPerformed(e);
                    }
                    return;
                }
                manager.update((System.currentTimeMillis() - startTime) / 1000f); // elapsed seconds
                try {
                    SwingUtilities.invokeLater(animationPanelRepainter);
                } catch (Exception ex) {
                }
            }

        });
        animationTimer.setRepeats(true);
        animationTimer.start();
    }
    

}
