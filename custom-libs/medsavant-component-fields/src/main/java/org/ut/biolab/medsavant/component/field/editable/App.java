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
        field.setValue("Hello");
        f.add(field);
        f.pack();
        f.setVisible(true);
    }
}
