/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.CustomCondition;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.view.genetics.filter.FilterState.FilterType;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;

/**
 *
 * @author Andrew
 */
public class GenericFixedFilterView extends FilterView {
    
    public static FilterView createGenericFixedView(String alias, Condition c, String description, int queryId, String id){
        return new GenericFixedFilterView(new JPanel(), alias, c, description, queryId, id);
    }
    
    private String id;
    private String alias;
    private String description;
    private Condition c;
    
    public GenericFixedFilterView(FilterState state, int queryId){
        this(new JPanel(), state.getName(), new CustomCondition(state.getValues().get("condition")), state.getValues().get("description"), queryId, state.getId());
    }
        
    private GenericFixedFilterView(JComponent container, final String alias, final Condition c, String description, int queryId, final String id){
        super(alias, container, queryId);
        
        this.alias = alias;
        this.description = description;
        this.id = id;
        this.c = c;
        
        container.setBorder(ViewUtil.getMediumBorder());
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));        
        
        JLabel label = new JLabel("<HTML>" + description + "</HTML>");
        label.setPreferredSize(new Dimension(10,10));
        container.add(label);
        container.add(Box.createRigidArea(new Dimension(1,50)));
        container.add(Box.createHorizontalGlue()); 
        
        //apply the conditions
        Filter f = new QueryFilter() {

            @Override
            public Condition[] getConditions() {                       
                return new Condition[]{c};
            }

            @Override
            public String getName() {
                return alias;
            }

            @Override
            public String getId() {
                return id;
            }

        };
        FilterController.addFilter(f, getQueryId());
    }

    @Override
    public FilterState saveState() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("description", description);
        map.put("condition", replaceAllTableReferences(c.toString()));
        return new FilterState(FilterType.GENERIC, alias, id, map);
    }
    
    private String replaceAllTableReferences(String s){
        return s.replaceAll("t[0-9]+\\.", "");
    }
    
}
