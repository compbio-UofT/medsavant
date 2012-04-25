package org.ut.biolab.medsavant.view.patients.individual;

import java.awt.Color;
import java.util.HashSet;
import org.ut.biolab.medsavant.view.patients.individual.IndividualDetailedView.HospitalSymbol;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import pedviz.view.NodeView;
import pedviz.view.rules.Rule;

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
