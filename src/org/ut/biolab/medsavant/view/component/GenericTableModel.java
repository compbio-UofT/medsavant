
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.component;

import com.jidesoft.grid.DefaultContextSensitiveTableModel;
import java.util.List;

/**
 *
 * @author mfiume
 */
public class GenericTableModel extends DefaultContextSensitiveTableModel {

    List<Class> columnClasses;

    public GenericTableModel(List<List> data, List<String> columnNames, List<Class> columnClasses) {
        super(Util.listToVector(data), Util.listToVector(columnNames));
        this.columnClasses = columnClasses;
    }

    public GenericTableModel(List<List> data, List<String> columnNames) {
        this(data, columnNames,null);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnClasses == null || columnClasses.isEmpty()) return String.class;
        return (Class) columnClasses.get(columnIndex);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}