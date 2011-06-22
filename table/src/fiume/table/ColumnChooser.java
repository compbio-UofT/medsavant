/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fiume.table;

import com.jidesoft.grid.SortableTable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;


/**
 *
 * @author AndrewBrook
 */
public class ColumnChooser extends JDialog {

    private JPanel panel;
    private GenericTableModel model;
    private SearchableTablePanel table;
    private List<JCheckBox> boxes = new ArrayList<JCheckBox>();

    public ColumnChooser(SearchableTablePanel table, GenericTableModel model){
        super();
        this.model = model;
        this.table = table;
        this.setTitle("Choose Columns to Display");
        this.setMinimumSize(new Dimension(260,100));
        this.setLayout(new BorderLayout());

        this.add(createFiller(), BorderLayout.NORTH);
        this.add(createFiller(), BorderLayout.SOUTH);
        this.add(createFiller(), BorderLayout.WEST);
        this.add(createFiller(), BorderLayout.EAST);

        //checkboxes
        panel = new JPanel();
        panel.setLayout(new GridLayout(model.getTotalColumnCount() + 2, 1, 0, 5));
        for(int i = 0; i < model.getTotalColumnCount(); i++){
            addColumn(model.getOriginalColumnName(i), model.isColumnVisible(i));
        }
        JButton submitButton = new JButton("Apply");
        submitButton.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                apply();
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });
        panel.add(createFiller());
        panel.add(submitButton);
        this.add(panel, BorderLayout.CENTER);

        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }

    private void addColumn(String name, boolean visible){
        JCheckBox box = new JCheckBox(name);
        box.setSelected(visible);
        panel.add(box);
        boxes.add(box);
    }

    private JPanel createFiller(){
        JPanel filler = new JPanel();
        filler.setPreferredSize(new Dimension(10,10));
        return filler;
    }

    private void apply(){
        for(int i = 0; i < boxes.size(); i++){
            model.setColumnVisible(i, boxes.get(i).isSelected());
        }
        table.updateView();
        this.dispose();
    }


}
