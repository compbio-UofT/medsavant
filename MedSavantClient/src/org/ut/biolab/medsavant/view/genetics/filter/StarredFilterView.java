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

package org.ut.biolab.medsavant.view.genetics.filter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.model.StarredVariant;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.view.genetics.filter.FilterState.FilterType;


/**
 *
 * @author Andrew
 */
public class StarredFilterView extends FilterView {
    private static final Log LOG = LogFactory.getLog(StarredFilterView.class);
    public static final String FILTER_NAME = "Marked as Important";
    public static final String FILTER_ID = "starred_filter";
    
    static FilterView getStarredFilterView(int queryId) {
        return new StarredFilterView(queryId, new JPanel());
    }
    
    public StarredFilterView(FilterState state, int queryId) {
        this(queryId, new JPanel());
    }
    
    public StarredFilterView(int queryId, JPanel container) {
        super(FILTER_NAME, container, queryId);
        createContentPanel(container);
    }

    private void createContentPanel(JPanel p) {
        p.setLayout(new BorderLayout());
        p.setMaximumSize(new Dimension(1000, 80));
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));     
        JTextArea label = new JTextArea("Filtering on variants that have been marked as important by any user.");
        label.setOpaque(false);
        label.setLineWrap(true);
        label.setWrapStyleWord(true);
        p.add(label);
        
        Filter f = new QueryFilter() {

            @Override
            public Condition[] getConditions() {
                try {
                    Set<StarredVariant> starred = MedSavantClient.VariantManager.getStarredVariants(
                            LoginController.sessionId, 
                            ProjectController.getInstance().getCurrentProjectID(), 
                            ReferenceController.getInstance().getCurrentReferenceID());
                    if (starred.isEmpty()) {
                        return new Condition[]{BinaryCondition.equalTo(0, 1)};
                    }
                    Iterator<StarredVariant> it = starred.iterator();
                    
                    TableSchema table = MedSavantClient.CustomTablesAdapter.getCustomTableSchema(LoginController.sessionId, MedSavantClient.ProjectQueryUtilAdapter.getVariantTablename(
                            LoginController.sessionId, 
                            ProjectController.getInstance().getCurrentProjectID(),
                            ReferenceController.getInstance().getCurrentReferenceID(),
                            true));
                    DbColumn uploadColumn = table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_UPLOAD_ID);
                    DbColumn fileColumn = table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_FILE_ID);
                    DbColumn variantColumn = table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_VARIANT_ID);
                    
                    Condition[] conditions = new Condition[starred.size()];
                    
                    int i = 0;
                    while (it.hasNext()) {
                        StarredVariant sv = it.next();
                        
                        Condition[] current = new Condition[3];
                        current[0] = BinaryCondition.equalTo(uploadColumn, sv.getUploadId());
                        current[1] = BinaryCondition.equalTo(fileColumn, sv.getFileId());
                        current[2] = BinaryCondition.equalTo(variantColumn, sv.getVariantId());                        
                        conditions[i] = ComboCondition.and(current);

                        i++;
                    }
                    
                    return conditions;
                    
                } catch (SQLException ex) {
                    LOG.error("Error getting starred variants.", ex);
                } catch (RemoteException ex) {
                    LOG.error("Error getting starred variants.", ex);
                }
                return new Condition[0]; 
            }

            @Override
            public String getName() {
                return FILTER_NAME;
            }

            @Override
            public String getId() {
                return FILTER_ID;
            }
        };

        FilterController.addFilter(f, getQueryId());
    }
    
    
    @Override
    public FilterState saveState() {
        return new FilterState(FilterType.STARRED, FILTER_NAME, FILTER_ID, new HashMap<String, String>());
    }   
}
