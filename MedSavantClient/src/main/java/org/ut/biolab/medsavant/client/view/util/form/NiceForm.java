package org.ut.biolab.medsavant.client.view.util.form;

import eu.hansolo.custom.SteelCheckBox;
import java.awt.Color;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.component.PlaceHolderPasswordField;
import org.ut.biolab.medsavant.client.view.component.PlaceHolderTextField;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import savant.api.util.DialogUtils;

/**
 * Created by mfiume on 2/2/2014.
 */
public class NiceForm extends JPanel implements Listener<NiceFormModel> {

    private NiceFormModel model;
    private LinkedHashMap<NiceFormField, JComponent> map; // keeps elements in order of insertion
    private boolean showEditButton;
    private boolean isInEditMode;
    private final List<Listener<FormEvent>> listeners;

    public void focus() {
        try {
            NiceFormField field = map.keySet().iterator().next();
            JComponent c = map.get(field);
            c.requestFocus();
        } catch (Exception e) {
        }
    }

    private String bulletStringOfLength(int length) {
        String s = "";
        while (length-- > 0) {
            s += "•";
        }
        return s;
    }

    public enum FormEvent {

        DID_UNLOCK_FOR_EDITING,
        DID_LOCK_FOR_EDITING
    }

    public void setEditModeOn(boolean b) {
        isInEditMode = b;
        refresh();
    }

    public boolean isEditModeOn() {
        return isInEditMode;
    }

    public enum FieldType {

        STRING,
        EMAIL,
        NUMBER,
        PASSWORD,
        BOOLEAN
    }

    public NiceForm(NiceFormModel model) {
        this.setDoubleBuffered(true);
        this.setOpaque(false);
        setModel(model);
        listeners = new ArrayList<Listener<FormEvent>>();
    }

    private void setModel(NiceFormModel model) {
        this.model = model;
        this.model.setListener(this);
        refresh();
    }

    @Override
    public void handleEvent(NiceFormModel event) {
        refresh();
    }

    public void addListener(Listener<FormEvent> listener) {
        listeners.add(listener);
    }

    public void refresh() {

        this.removeAll();
        this.setLayout(new MigLayout("insets 0, gapx 5, gapy 5"));

        if (showEditButton) {
            final SteelCheckBox cb = ViewUtil.getSwitchCheckBox();
            cb.setSelected(!isInEditMode);

            final NiceForm instance = this;
            cb.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    boolean isEditing = !cb.isSelected();
                    instance.setEditModeOn(!cb.isSelected());

                    for (Listener<FormEvent> listener : listeners) {
                        listener.handleEvent(isEditing ? FormEvent.DID_LOCK_FOR_EDITING : FormEvent.DID_LOCK_FOR_EDITING);
                    }
                }
            });
            this.add(cb, "wrap");
        }

        map = new LinkedHashMap<NiceFormField, JComponent>();
        int count = 0;
        for (NiceFormFieldGroup g : model.getGroups()) {
            appendGroup(g);
            if (++count != model.getGroups().size()) {
                this.add(Box.createVerticalStrut(10), "wrap");
            }
        }

        this.updateUI();
        this.invalidate();
    }

    private void appendGroup(NiceFormFieldGroup group) {

        if (group.displayHeading()) {
            this.add(ViewUtil.getEmphasizedLabel(group.getName().toUpperCase()), "wrap, span");
            //this.add(new JSeparator(),"wrap, span, growx 1.0");
        }

        for (NiceFormField field : group.getFields()) {

            boolean addLabel = true;
            JComponent c = null;

            int stringFieldWidth = 16;
            int intFieldWidth = 5;

            switch (field.getType()) {
                case NUMBER:
                    if (isInEditMode) {
                        JTextField f0 = new JTextField(field.getName());
                        if (field.getValue() != null) {
                            f0.setText(field.getValue().toString());
                        }
                        f0.setColumns(intFieldWidth);
                        c = f0;
                    } else {
                        if (field.getValue() != null) {
                            c = new JLabel(field.getValue().toString());
                        }
                    }
                    break;
                case STRING:
                case EMAIL:
                    if (isInEditMode) {
                        JTextField f1 = new JTextField(field.getName(), stringFieldWidth);
                        if (field.getValue() != null) {
                            f1.setText(field.getValue().toString());
                        }
                        c = f1;
                    } else {
                        if (field.getValue() != null) {
                            c = new JLabel(field.getValue().toString());
                        }
                    }
                    break;
                case PASSWORD:
                    if (isInEditMode) {
                        PlaceHolderPasswordField f2 = new PlaceHolderPasswordField("", stringFieldWidth);
                        f2.setPlaceholder("");
                        if (field.getValue() != null) {
                            f2.setText(field.getValue().toString());
                        }
                        c = f2;
                    } else {
                        if (field.getValue() != null) {
                            c = new JLabel(bulletStringOfLength(field.getValue().toString().length()));
                        }
                    }
                    break;
                case BOOLEAN:
                    addLabel = false;
                    //SteelCheckBox cb = ViewUtil.getSwitchCheckBox(field.getName());
                    JCheckBox cb = new JCheckBox(field.getName());
                    if (field.getValue() != null) {
                        cb.setSelected((Boolean) field.getValue());
                    }
                    cb.setEnabled(isInEditMode);
                    c = cb;
                    break;
            }

            if (addLabel) {
                this.add(ViewUtil.makeSmall(ViewUtil.getEmphasizedSemiBlackLabel(field.getName().toUpperCase())));
            } else {
                this.add(Box.createHorizontalStrut(1));
            }

            if (c != null) {

                c.setFont(ViewUtil.getBigInputFont());

                // in non-edit mode, add the textual representation
                /*if (!isInEditMode) {
                 if (c instanceof JTextField) {
                 ((JTextField) c).setDisabledTextColor(ViewUtil.getSemiBlackColor());
                 }
                 c.setEnabled(false);
                 }*/
                Color color = ViewUtil.getSemiBlackColor();
                c.setForeground(color);
                this.add(c, "wrap");
                map.put(field, c);
            }
        }
    }

    public boolean isFieldSet(NiceFormField field) {
        switch (field.getType()) {
            case NUMBER:
            case STRING:
            case PASSWORD:
                return !((JTextField) map.get(field)).getText().isEmpty();
            case BOOLEAN:
                return true;
            default:
                return false;
        }
    }

    public String getValueForStringField(NiceFormField field) {
        if (field.getType() == FieldType.STRING || field.getType() == FieldType.EMAIL || field.getType() == FieldType.PASSWORD) {
            return ((JTextField) map.get(field)).getText();
        } else {
            throw new IllegalArgumentException(field.getName() + " does not have a String value");
        }
    }

    public int getValueForIntegerField(NiceFormField field) {
        if (field.getType() == FieldType.NUMBER) {
            try {
                return Integer.parseInt(((JTextField) map.get(field)).getText());
            } catch (Exception e) {
                return -1;
            }
        } else {
            throw new IllegalArgumentException(field.getName() + " does not have a Integer value");
        }
    }

    public boolean getValueForBooleanField(NiceFormField field) {
        if (field.getType() == FieldType.BOOLEAN) {
            return ((JCheckBox) map.get(field)).isSelected();
        } else {
            throw new IllegalArgumentException(field.getName() + " does not have a Boolean value");
        }
    }

    public boolean validateForm() {

        for (NiceFormFieldGroup group : this.model.getGroups()) {
            for (NiceFormField field : group.getFields()) {
                boolean success = validateField(field);
                if (!success) {
                    DialogUtils.displayError("Invalid value for " + field.getName());
                    map.get(field).requestFocus();
                    return false;
                }
            }
        }

        return true;
    }

    public void setShowEditButton(boolean b) {
        showEditButton = b;
        refresh();
    }

    public boolean isEditable() {
        return showEditButton;
    }

    private boolean validateField(NiceFormField field) {

        try {

            // true iff the field isn't required and is blank
            if (!field.isRequired() && !this.isFieldSet(field)) {
                return true;
            }

            switch (field.getType()) {
                case BOOLEAN:
                    this.getValueForBooleanField(field);
                    break;
                case NUMBER: // todo: custom unsigned int validator
                    int i = this.getValueForIntegerField(field);
                    if (i < 0) {
                        return false;
                    }
                    break;
                case EMAIL: // todo: custom validator
                case STRING:
                case PASSWORD: // todo: custom validator
                    String s = this.getValueForStringField(field);
                    if (s.isEmpty()) {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    public static void main(String[] argv) {
        JFrame f = new JFrame();

        f.setBackground(Color.white);
        f.setLayout(new MigLayout("wrap"));
        NiceFormFieldGroup g = new NiceFormFieldGroup("Heading", true);
        g.addField(new NiceFormField(true, "field", NiceForm.FieldType.STRING, "Hello"));

        NiceFormModel m = new NiceFormModel();
        m.addGroup(g);

        NiceForm form = new NiceForm(m);
        f.add(form);
        f.pack();
        f.setVisible(true);
    }
}
