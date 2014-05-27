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
package org.ut.biolab.medsavant.client.view.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 * A helper class to assist in creating App containers with 
 * standard scroll behaviour and padding. An instance of this class
 * should  be returned by all getView methods of apps.
 * @author mfiume
 */
public class StandardAppContainer extends JPanel {

    public StandardAppContainer(JPanel view) {
        this(view,false);
    }
    
    int insets = 30;
    
    public StandardAppContainer(final JPanel view, boolean doesScroll) {
        
        final JPanel paddedContainer = ViewUtil.getClearPanel();
        paddedContainer.setLayout(new MigLayout(String.format("fillx, filly, insets %d",insets)));

        this.setBackground(Color.white);
        this.setLayout(new BorderLayout());

        if (doesScroll) {
            final JScrollPane p = ViewUtil.getClearBorderlessScrollPane(paddedContainer);
            p.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            paddedContainer.add(view,"growy 1.0");
            this.add(p, BorderLayout.CENTER);
            
            
            p.addComponentListener(new ComponentListener() {

                @Override
                public void componentResized(ComponentEvent e) {
                   repositionView();
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                }

                @Override
                public void componentShown(ComponentEvent e) {
                    repositionView();
                }

                @Override
                public void componentHidden(ComponentEvent e) {
                }

                private void repositionView() {
                    int size = ((int)p.getSize().getWidth()-2*insets);
                    paddedContainer.remove(view);
                    paddedContainer.add(view,String.format("growy 1.0, width %d",size));
                }
                
            });
        } else {
            paddedContainer.add(view,"growy 1.0, width 100%");
            this.add(paddedContainer,BorderLayout.CENTER);
        }
    }
}
