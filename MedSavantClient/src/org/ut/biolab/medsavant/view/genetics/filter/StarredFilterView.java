/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.model.StarredVariant;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.view.genetics.filter.FilterState.FilterType;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class StarredFilterView extends FilterView {

    public static final String FILTER_NAME = "Marked as Important";
    public static final String FILTER_ID = "starred_filter";
    
    static FilterView getStarredFilterView(int queryId) {
        return new StarredFilterView(queryId, new JPanel());
    }
    
    public StarredFilterView(FilterState state, int queryId){
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
        p.add(new JLabel("Filtering on variants that have been marked as important by some user. "));
        
        Filter f = new QueryFilter() {

            @Override
            public Condition[] getConditions() {
                try {
                    Set<StarredVariant> starred = MedSavantClient.VariantQueryUtilAdapter.getStarredVariants(
                            LoginController.sessionId, 
                            ProjectController.getInstance().getCurrentProjectId(), 
                            ReferenceController.getInstance().getCurrentReferenceId());
                    Iterator<StarredVariant> it = starred.iterator();
                    
                    TableSchema table = MedSavantClient.CustomTablesAdapter.getCustomTableSchema(LoginController.sessionId, MedSavantClient.ProjectQueryUtilAdapter.getVariantTablename(
                            LoginController.sessionId, 
                            ProjectController.getInstance().getCurrentProjectId(),
                            ReferenceController.getInstance().getCurrentReferenceId(),
                            true));
                    DbColumn uploadColumn = table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_UPLOAD_ID);
                    DbColumn fileColumn = table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_FILE_ID);
                    DbColumn variantColumn = table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_VARIANT_ID);
                    
                    Condition[] conditions = new Condition[starred.size()];
                    
                    int i = 0;
                    while(it.hasNext()){
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
                    Logger.getLogger(StarredFilterView.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RemoteException ex) {
                    Logger.getLogger(StarredFilterView.class.getName()).log(Level.SEVERE, null, ex);
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
