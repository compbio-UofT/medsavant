/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.model.structure.CustomTables;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.view.util.PeekingPanel;
import org.ut.biolab.medsavant.view.genetics.filter.FilterPanel;
import org.ut.biolab.medsavant.view.genetics.filter.FilterProgressPanel;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class GeneticsFilterPage extends SubSectionView {

    private static class FilterSQLPanel extends JPanel implements FiltersChangedListener {
        private final JTextArea content;

        public FilterSQLPanel() {
            this.setBorder(ViewUtil.getBigBorder());
            content = new JTextArea();
            content.setEditable(false);
            this.setLayout(new BorderLayout());
            this.add(new JScrollPane(content));
            updateSQL();
            FilterController.addFilterListener(this);
        }

        public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
            updateSQL();
        }

        private void updateSQL() {
            Condition[][] conditions = FilterController.getQueryFilterConditions();
               
            SelectQuery q = new SelectQuery();
            //q.addFromTable(table.getTable());
            VariantQueryUtil.addConditionsToQuery(q, conditions);
            
            String s = q.toString();
            Logger.getLogger(GeneticsFilterPage.class.getName()).log(Level.WARNING, s);
            this.content.setText(s);
        }
    }

    private JPanel view;
    private FilterPanel fp;
    private FilterProgressPanel history;
    
    public GeneticsFilterPage(SectionView parent) {
        super(parent);
    }

    public String getName() {
        return "Filter";
    }

    public JPanel getView(boolean update) {
        if (view == null || update) {
            view = new JPanel();
            view.setLayout(new BorderLayout());
            fp = new FilterPanel();
            view.add(fp,BorderLayout.CENTER);
            
            if(history != null) FilterController.removeFilterListener(history);
            history = new FilterProgressPanel();
            view.add(new PeekingPanel("History", BorderLayout.EAST, history, true), BorderLayout.WEST);
            
            // uncomment the next line to show the master SQL statement
            //view.add(new PeekingPanel("SQL", BorderLayout.SOUTH, new FilterSQLPanel(), true), BorderLayout.NORTH);
        } else {
            fp.refreshSubPanels();
        }

        return view;
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
        ThreadController.getInstance().cancelWorkers(getName());
    }
}
