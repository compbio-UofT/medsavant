package org.ut.biolab.medsavant.client.view.util.form;

import eu.hansolo.custom.SteelCheckBox;
import eu.hansolo.tools.ColorDef;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.component.PlaceHolderPasswordField;
import org.ut.biolab.medsavant.client.view.component.PlaceHolderTextField;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * Created by mfiume on 2/2/2014.
 */
public class NiceForm extends JPanel implements Listener<NiceFormModel> {

    private NiceFormModel model;
    private HashMap<NiceFormField,JComponent> map;

    public enum FieldType {
        STRING,
        EMAIL,
        NUMBER,
        PASSWORD,
        BOOLEAN
    }

    public NiceForm(NiceFormModel model) {
        this.setOpaque(false);
        setModel(model);
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

    public void refresh() {

        this.removeAll();
        this.setLayout(new MigLayout("insets 0"));

        map = new HashMap<NiceFormField,JComponent>();

        for (NiceFormField field : model.getFields()) {

            boolean addLabel = true;
            JComponent c = null;

            int stringFieldWidth = 16;
            int intFieldWidth = 5;

            switch (field.getType()) {
                case NUMBER:
                    JTextField f0 = new JTextField(field.getName());
                    if (field.getValue() != null) {
                        f0.setText(field.getValue().toString());
                    }
                    f0.setColumns(intFieldWidth);
                    f0.setFont(ViewUtil.getBigInputFont());
                    c = f0;
                    break;
                case STRING:
                case EMAIL:
                    JTextField f1 = new JTextField(field.getName(),stringFieldWidth);
                    if (field.getValue() != null) {
                        f1.setText(field.getValue().toString());
                    }
                    f1.setFont(ViewUtil.getBigInputFont());
                    c = f1;
                    break;
                case PASSWORD:
                    PlaceHolderPasswordField f2 = new PlaceHolderPasswordField("",stringFieldWidth);
                    f2.setPlaceholder("");
                    if (field.getValue() != null) {
                        f2.setText(field.getValue().toString());
                    }
                    f2.setFont(ViewUtil.getBigInputFont());
                    c = f2;
                    break;
                case BOOLEAN:
                    addLabel = false;
                    SteelCheckBox cb = ViewUtil.getSwitchCheckBox(field.getName());
                    if (field.getValue() != null) {
                        cb.setSelected((Boolean)field.getValue());
                    }
                    c = cb;
                    break;
            }

            if (addLabel) {
                this.add(ViewUtil.getEmphasizedSemiBlackLabel(field.getName().toUpperCase()));
            } else {
                this.add(Box.createHorizontalStrut(1));
            }

            if (c != null) {
                Color color = ViewUtil.getSemiBlackColor();
                c.setForeground(color);
                this.add(c,"wrap");
                map.put(field,c);
            }
        }

        this.updateUI();
        this.invalidate();
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
        return true;
    }
}
