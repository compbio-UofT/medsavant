/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.awt.BorderLayout;
import java.rmi.RemoteException;
import java.sql.SQLException;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.db.FatalDatabaseException;
import org.ut.biolab.medsavant.db.NonFatalDatabaseException;
import org.ut.biolab.medsavant.listener.ReferenceListener;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.view.genetics.filter.FilterPanel;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class GeneticsFilterPage extends SubSectionView implements ReferenceListener {

    private static final Log LOG = LogFactory.getLog(GeneticsFilterPage.class);

    private JPanel view;
    private static FilterPanel fp;

    public GeneticsFilterPage(SectionView parent) {
        super(parent);
        ReferenceController.getInstance().addReferenceListener(this);
        fp = getFilterPanel();
    }

    @Override
    public String getName() {
        return "search bar";
    }

    @Override
    public JPanel getView(boolean update) {
        if (view == null || update) {
            view = ViewUtil.getClearPanel();
            view.setName(this.getName());
            view.setLayout(new BorderLayout());
            view.add(fp,BorderLayout.CENTER);

            //if (history != null) FilterController.removeFilterListener(history);
            //history = new FilterProgressPanel();
            //view.add(new PeekingPanel("History", BorderLayout.EAST, history, true), BorderLayout.WEST);

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

    public static FilterPanel getFilterPanel() {
        if (fp == null) {
            fp = new FilterPanel();
        }
        return fp;
    }

    @Override
    public void referenceAdded(String name) {}

    @Override
    public void referenceRemoved(String name) {}

    @Override
    public void referenceChanged(String prnameojectName) {
        if (fp != null) {
            fp.clearAll();
            fp.refreshSubPanels();
        }
    }


    private static class FilterSQLPanel extends JPanel implements FiltersChangedListener {
        private final JTextArea content;

        public FilterSQLPanel() {
            this.setBorder(ViewUtil.getBigBorder());
            content = new JTextArea();
            content.setEditable(false);
            setLayout(new BorderLayout());
            add(new JScrollPane(content));
            updateSQL();
            FilterController.addFilterListener(this);
        }

        @Override
        public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
            updateSQL();
        }

        private void updateSQL() {
            Condition[][] conditions = FilterController.getQueryFilterConditions();

            SelectQuery q = new SelectQuery();
            try {
                MedSavantClient.VariantQueryUtilAdapter.addConditionsToQuery(q, conditions);
            } catch (RemoteException ex) {
                LOG.error("Error adding conditions to query.", ex);
            }

            String s = q.toString();
            LOG.info(s);
            content.setText(s);
        }
    }


}
