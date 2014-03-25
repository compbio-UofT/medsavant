/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ut.biolab.medsavant.client.patient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.dialog.IndividualSelector;
import org.ut.biolab.medsavant.component.field.editable.EnumEditableField;
import org.ut.biolab.medsavant.component.field.editable.OnClickEditableField;
import org.ut.biolab.medsavant.component.field.editable.StringEditableField;

/**
 *
 * @author mfiume
 */
class EditablePatientField extends OnClickEditableField<String> {

    private final JButton button;
    private String editorValue;
    private final ActionListener actionListener;
    private final JPanel view;
    private final StringEditableField textOver;

    public EditablePatientField(boolean stringOverride) {

        view = new JPanel();
        view.setOpaque(false);
        view.setLayout(new MigLayout("insets 0, hidemode 3"));

        button = new JButton();
        button.setFocusable(false);

        actionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                IndividualSelector s = new IndividualSelector(true);
                s.setVisible(true);

                Set<String> selected = s.getHospitalIDsOfSelectedIndividuals();
                if (!selected.isEmpty()) {
                    editorValue = (String) selected.toArray()[0];
                    textOver.setValue(editorValue);
                }
            }

        };

        button.addActionListener(actionListener);

        this.setAcceptButtonVisible(true);

        textOver = new StringEditableField() {

            @Override
            public void didToggleEditMode(boolean editMode) {
                if (!editMode) {
                    try {
                        editorValue = this.getValueFromEditor();
                    } catch (NullPointerException npe) {
                        // thrown on initialization, it's ok, we don't need 
                        // it then
                    }
                }
            }
        };
        textOver.setAutonomousEditingEnabled(stringOverride);

        view.add(textOver);
        view.add(button);
    }

    @Override
    public void updateEditorRepresentationForValue(String value) {
        button.setText("Choose...");
        if (value == null) {
            textOver.setValue("");
        } else {
            textOver.setValue(value);
        }
    }

    @Override
    public JComponent getEditor() {
        return view;
    }

    @Override
    public String getValueFromEditor() {
        return editorValue;
    }

    @Override
    public void didToggleEditMode(boolean editMode) {
    }

}
