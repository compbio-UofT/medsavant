/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.Condition;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class GenericFixedFilterView extends FilterView {
    
    public static FilterView createGenericFixedView(String alias, Condition c, String description, int queryId, String id){
        return new GenericFixedFilterView(new JPanel(), alias, c, description, queryId, id);
    }
        
    private GenericFixedFilterView(JComponent container, final String alias, final Condition c, String description, int queryId, final String id){
        super(alias, container);   
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
        FilterController.addFilter(f, queryId);
    }
    
}
