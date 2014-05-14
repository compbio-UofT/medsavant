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

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import mfiume.component.transition.accessor.FloatAccessor.FloatStruct;

/**
 *
 * @author mfiume
 */
public class AlphaBasedPainterPanel implements PanelPainter {

    private BufferedImage newImage;
    private FloatStruct newImageAlpha;
    private BufferedImage previousImage;

    public AlphaBasedPainterPanel() {
    }

    @Override
    public void paintComponent(Graphics g) {
        
        if (previousImage != null) {
            g.drawImage(previousImage, 0 , 0, null);
        }
        if (newImage != null) {
            Graphics2D g2d = (Graphics2D) g;
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, newImageAlpha.getFloat());
            g2d.setComposite(ac);
            g2d.drawImage(newImage, 0, 0, null);
        }

        g.dispose();
    }

    public void setNewImageSource(JPanel source, Dimension dim) {
        this.newImage = PaintUtils.getImageFromSource(source, dim);
    }

    public void setNewImageAlpha(FloatStruct newImageAlpha) {
        this.newImageAlpha = newImageAlpha;
    }

    public void setPreviousImageSource(JPanel source, Dimension dim) {
        this.previousImage = PaintUtils.getImageFromSource(source, dim);
    }

    
}
