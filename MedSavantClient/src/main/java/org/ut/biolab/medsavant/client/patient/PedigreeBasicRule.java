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
package org.ut.biolab.medsavant.client.patient;

import java.awt.Color;
import java.util.HashSet;

import pedviz.view.NodeView;
import pedviz.view.rules.Rule;

import org.ut.biolab.medsavant.client.patient.IndividualDetailedView.HospitalSymbol;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;


/**
 *
 * @author mfiume
 */
public class PedigreeBasicRule extends Rule implements PedigreeFields {

        private final HashSet<Integer> selectedIDs;

        public PedigreeBasicRule() {
            selectedIDs = null;
        }

        public PedigreeBasicRule(int[] patientIds) {
            selectedIDs = new HashSet<Integer>();
            for (int id : patientIds) {
                selectedIDs.add(id);
            }
        }

        @Override
        public void applyRule(NodeView nv) {
            nv.setColor(Color.white);
            String hospitalIdOfNode = nv.getNode().getId().toString();
            nv.setHintText(hospitalIdOfNode);

            if (selectedIDs !=null) {


                Object pIdPrecursor = nv.getNode().getUserData(PATIENT_ID);
                if (pIdPrecursor != null) {
                    int patientId = Integer.parseInt(pIdPrecursor.toString());
                    if (selectedIDs.contains(patientId)) {
                        nv.setBorderColor(ViewUtil.detailSelectedBackground);
                    }
                }

                Object affectedPrecursor = nv.getNode().getUserData(AFFECTED);
                if (affectedPrecursor != null) {
                    boolean affected = 1 == Integer.parseInt(affectedPrecursor.toString());
                    if (affected) {
                        nv.setColor(Color.BLACK);
                    }
                }

            }

            Object hIdPrecursor = nv.getNode().getUserData(HOSPITAL_ID);
            if (hIdPrecursor != null) {
                String hospitalId = hIdPrecursor.toString();
                nv.addSymbol(new HospitalSymbol(hospitalId));
            }

            nv.setHintText(null);

        }
    }
