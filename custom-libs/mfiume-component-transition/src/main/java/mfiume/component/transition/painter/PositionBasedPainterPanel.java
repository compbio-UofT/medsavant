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
package mfiume.component.transition.painter;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class PositionBasedPainterPanel implements PanelPainter {

    private BufferedImage newImage;
    private Point newOrigin;
    private BufferedImage previousImage;
    private Point previousOrigin;

    public PositionBasedPainterPanel() {
    }

    @Override
    public void paintComponent(Graphics g) {
        if (newImage != null) {
            g.drawImage(newImage, (int) newOrigin.getX(), (int) newOrigin.getY(), null);
        }
        if (previousImage != null) {
            g.drawImage(previousImage, (int) previousOrigin.getX(), (int) previousOrigin.getY(), null);
        }
        g.dispose();
    }

    public void setNewImageSource(JPanel source, Dimension dim) {
        this.newImage = PaintUtils.getImageFromSource(source, dim);
    }

    public void setNewOrigin(Point origin) {
        this.newOrigin = origin;
    }
    
    public void setPreviousImageSource(JPanel source, Dimension dim) {
        this.previousImage = PaintUtils.getImageFromSource(source, dim);
    }

    public void setPreviousOrigin(Point origin) {
        this.previousOrigin = origin;
    }

}
