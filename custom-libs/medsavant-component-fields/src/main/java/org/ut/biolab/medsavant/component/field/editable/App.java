package org.ut.biolab.medsavant.component.field.editable;

import javax.swing.JFrame;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        JFrame f = new JFrame();
        StringEditableField field = new StringEditableField();
        field.setValidator(new EditableFieldValidator<String>() {

            @Override
            public boolean validate(String value) {
                return !value.isEmpty();
            }

            @Override
            public String getDescriptionOfValidValue() {
                return "Must not be blank";
            }
            
        });
        field.setValue("Hello");
        f.add(field);
        f.pack();
        f.setVisible(true);
    }
}
