/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.patient;

import java.awt.Color;
import java.util.HashSet;
import pedviz.view.NodeView;
import pedviz.view.rules.Rule;

import org.ut.biolab.medsavant.patient.IndividualDetailedView.HospitalSymbol;
import org.ut.biolab.medsavant.view.util.ViewUtil;


/**
 *
 * @author mfiume
 */
public class PedigreeBasicRule extends Rule {

        private final HashSet<Integer> selectedIds;

        public PedigreeBasicRule() {
            selectedIds = null;
        }

        public PedigreeBasicRule(int[] patientIds) {
            selectedIds = new HashSet<Integer>();
            for (int id : patientIds) {
                selectedIds.add(id);
            }
        }

        @Override
        public void applyRule(NodeView nv) {
            nv.setColor(Color.white);
            String hospitalIdOfNode = nv.getNode().getId().toString();
            nv.setHintText(hospitalIdOfNode);

            if (selectedIds !=null) {


                Object pIdPrecursor = nv.getNode().getUserData(Pedigree.FIELD_PATIENTID);
                if (pIdPrecursor != null) {
                    int patientId = Integer.parseInt(pIdPrecursor.toString());
                    if (selectedIds.contains(patientId)) {
                        nv.setBorderColor(ViewUtil.detailSelectedBackground);
                    }
                }

                Object affectedPrecursor = nv.getNode().getUserData(Pedigree.FIELD_AFFECTED);
                if (affectedPrecursor != null) {
                    boolean affected = 1 == Integer.parseInt(affectedPrecursor.toString());
                    if (affected) {
                        nv.setColor(Color.BLACK);
                    }
                }

            }

            Object hIdPrecursor = nv.getNode().getUserData(Pedigree.FIELD_HOSPITALID);
            if (hIdPrecursor != null) {
                String hospitalId = hIdPrecursor.toString();
                nv.addSymbol(new HospitalSymbol(hospitalId));
            }

            nv.setHintText(null);

        }
    }
