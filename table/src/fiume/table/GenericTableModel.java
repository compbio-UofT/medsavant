/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fiume.table;

import com.jidesoft.grid.DefaultContextSensitiveTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mfiume, AndrewBrook
 */
public class GenericTableModel extends DefaultContextSensitiveTableModel {

    List<Class> columnClasses;
    private boolean[] columnsVisible;
    private List<String> columnNames;

    public GenericTableModel(List<List> data, List<String> columnNames, List<Class> columnClasses) {
        super(Util.listToVector(data), Util.listToVector(columnNames));
        this.columnClasses = columnClasses;
        this.columnsVisible = new boolean[columnClasses.size()];
        for(int i = 0; i < columnClasses.size(); i++){
            columnsVisible[i] = true;
        }
        this.columnNames = columnNames;
    }

    public GenericTableModel(List<List> data, List<String> columnNames) {
        this(data, columnNames,null);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnClasses == null || columnClasses.size() == 0) return String.class;
        return (Class) columnClasses.get(columnIndex);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    protected int getNumber (int col) {
        int n = col;    // right number to return
        int i = 0;
        do {
            if (!(columnsVisible[i])) n++;
            i++;
        } while (i < n);
        // If we are on an invisible column,
        // we have to go one step further
        while (!(columnsVisible[n])) n++;
        return n;
    }

    @Override
    public int getColumnCount () {
        int n = 0;
        for (int i = 0; i < columnsVisible.length; i++)
            if (columnsVisible[i]) n++;
        return n;
    }

    @Override
    public Object getValueAt (int row, int col) {
        //Object[] array = (Object[])(this.getDataVector().elementAt(row));
        //return array[getNumber(col)];
        return super.getValueAt(row, getNumber(col));
    }

    @Override
    public String getColumnName (int col) {
        return super.getColumnName(getNumber(col));
    }

    /*public List<String> getVisibleColumns(){
        List<String> result = new ArrayList<String>();
        for(int i = 0; i < columnsVisible.length; i++){
            if(columnsVisible[i]){
                result.add(columnNames);
            }
        }
        return result;
    }*/

    public boolean[] getVisibleColumns(){
        return this.columnsVisible;
    }

    public void setVisibleColumns(boolean[] visibility){
        if(visibility.length != columnsVisible.length) return;
        columnsVisible = visibility;
    }

    public void setColumnVisible(int pos, boolean visible){
        columnsVisible[pos] = visible;
    }

    public void resetVisible(){
        for(int i = 0; i < columnsVisible.length; i++){
            columnsVisible[i] = true;
        }
    }

    public int getTotalColumnCount(){
        return columnNames.size();
    }

    public String getOriginalColumnName (int col) {
        return super.getColumnName(col);
    }

    public boolean isColumnVisible(int col){
        return columnsVisible[col];
    }

    

}
