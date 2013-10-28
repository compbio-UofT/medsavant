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
package org.ut.biolab.medsavant.client.view.component;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.Document;

/**
 *
 * @author mfiume
 */
public class PlaceHolderTextField extends JTextField {

    public static void main(final String[] args) {
        final PlaceHolderTextField tf = new PlaceHolderTextField("");
        tf.setColumns(20);
        final Font f = tf.getFont();
        tf.setFont(new Font(f.getName(), f.getStyle(), 30));
        //JOptionPane.showMessageDialog(null, tf);
    }

    private String placeholder;

    public PlaceHolderTextField() {
    }

    public PlaceHolderTextField(
        final Document pDoc,
        final String pText,
        final int pColumns)
    {
        super(pDoc, pText, pColumns);
    }

    public PlaceHolderTextField(final int pColumns) {
        super(pColumns);
    }

    public PlaceHolderTextField(final String pText) {
        super(pText);
    }

    public PlaceHolderTextField(final String pText, final int pColumns) {
        super(pText, pColumns);
    }

    public String getPlaceholder() {
        return placeholder;
    }

    @Override
    protected void paintComponent(final Graphics pG) {
        super.paintComponent(pG);

        if (placeholder.length() == 0 || getText().length() > 0) {
            return;
        }

        final Graphics2D g = (Graphics2D) pG;
        g.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(getDisabledTextColor());
        g.drawString(placeholder, getInsets().left, pG.getFontMetrics()
            .getMaxAscent() + getInsets().top);
    }

    public void setPlaceholder(final String s) {
        placeholder = s;
    }

}