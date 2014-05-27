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
package org.ut.biolab.medsavant.app.mendelclinic.view;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.view.util.StandardAppContainer;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class MendelPanel {
    public static String PAGE_NAME = "Mendel";

    private JPanel view;
    private OptionView fo;

    public MendelPanel() {
    }

    public JPanel getView() {

        if (view == null) {
            view = new JPanel();
            view.setLayout(new BorderLayout());
            view.setBackground(ViewUtil.getLightGrayBackgroundColor());
            
            fo = new OptionView();
            JPanel p = new StandardAppContainer(fo.getView(), true);
            p.setBackground(ViewUtil.getLightGrayBackgroundColor());
            view.add(p, BorderLayout.CENTER);
        }

        return view;
    }

    public void refresh() {
        fo.viewDidLoad();
    }
}
