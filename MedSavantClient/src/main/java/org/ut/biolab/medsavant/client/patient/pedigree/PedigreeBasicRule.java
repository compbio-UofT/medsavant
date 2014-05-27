/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.patient.pedigree;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import org.ut.biolab.medsavant.client.view.font.FontFactory;

import pedviz.view.NodeView;
import pedviz.view.rules.Rule;

import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import pedviz.view.symbols.Symbol2D;
import pedviz.view.symbols.SymbolGenotypes;

/**
 *
 * @author mfiume
 */
public class PedigreeBasicRule extends Rule implements PedigreeFields {

    private int selectedPatientID;
    private float fontSize = 1.0f;
    private boolean patientLabelsShown = true;

    public PedigreeBasicRule() {
        this(-1);
    }

    public PedigreeBasicRule(int patientID) {
        this.selectedPatientID = patientID;
    }

    @Override
    public void applyRule(NodeView nv) {
        nv.setColor(Color.white);
        String hospitalIdOfNode = nv.getNode().getId().toString();
        nv.setHintText(hospitalIdOfNode);

        /*Object pIdPrecursor = nv.getNode().getUserData(PATIENT_ID);
        if (pIdPrecursor != null) {
            int patientId = Integer.parseInt(pIdPrecursor.toString());
            if (selectedPatientID == patientId) {
                nv.setBorderColor(ViewUtil.detailSelectedBackground);
            }
        }*/
        
        Object affectedPrecursor = nv.getNode().getUserData(AFFECTED);
        if (affectedPrecursor != null) {
            boolean affected = 1 == Integer.parseInt(affectedPrecursor.toString());
            if (affected) {
                nv.setColor(Color.BLACK);
            }
        }

        Object hIdPrecursor = nv.getNode().getUserData(HOSPITAL_ID);
        if (hIdPrecursor
                != null) {
            
            nv.addSymbol(new NameSymbol(patientLabelsShown));
            
            //String hospitalId = hIdPrecursor.toString();
            //nv.addSymbol(new HospitalSymbol(hospitalId, fontSize, patientLabelsShown));
        }

        nv.setHintText(null);

    }

    void setPatientLabelsShown(boolean b) {
        this.patientLabelsShown = b;
    }

    float getFontSize() {
        return fontSize;
    }

    void setFontSize(float f) {
        fontSize = f;
    }

    public static class HospitalSymbol extends Symbol2D {

        private final String hid;
        private final float fontSize;
        private final boolean patientLabelsShown;

        public HospitalSymbol(String hid, float fontSize, boolean patientLabelsShown) {
            this.hid = hid;
            this.fontSize = fontSize;
            this.patientLabelsShown = patientLabelsShown;
        }

        @Override
        public void drawSymbol(Graphics2D g2, Point2D.Float position, float size, Color color, Color color1, NodeView nv) {

            if (patientLabelsShown) {
                
                Font font = new Font(FontFactory.getGeneralFont().getFamily(),Font.PLAIN, 1);
                g2.setFont(font);
                FontMetrics fm = g2.getFontMetrics();

                FontRenderContext frc = g2.getFontRenderContext();

                int gap = 2;
                
                float height = font.getLineMetrics(hid, frc).getHeight();
                float width = fm.stringWidth(hid);
                float startX = (float) position.x - width/2;
                float startY = (float) (position.y + size/2 + gap);

                g2.setColor(new Color(255, 255, 255, 200));

                float padding = 0.3f;

                float rx = startX - padding;
                float ry = startY - height - padding;
                float rwid = width + 2 * padding;
                float rheight = height + 2 * padding;
                float radius = rheight;
                RoundRectangle2D.Float rr = new RoundRectangle2D.Float(rx, ry, rwid, rheight, radius, radius);
                
                float alpha = 0.8f;
                Color topColor = Color.white;
                Color bottomColor = new Color(245,245,245);
                
                /* GradientPaint gp = new GradientPaint(startX - padding, startY - height - padding, new Color(topColor.getRed(), topColor.getGreen(), topColor.getBlue(),(int)(alpha*255)),
                        rx, ry+rheight, new Color(bottomColor.getRed(), bottomColor.getGreen(), bottomColor.getBlue(), (int)(alpha*255)), true);
                // Fill with a gradient.
                g2.setPaint(gp);
                
                g2.fill(rr);
                g2.setColor(bottomColor);
                g2.setStroke(new BasicStroke(0.05f));
                g2.draw(rr);*/

                g2.setColor(Color.black);
                
                g2.drawString(hid, startX, startY);
            }
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }
}
