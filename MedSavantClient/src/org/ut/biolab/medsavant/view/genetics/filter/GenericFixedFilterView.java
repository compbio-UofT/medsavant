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

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.CustomCondition;

import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.view.util.ViewUtil;


/**
 *
 * @author Andrew
 */
public class GenericFixedFilterView extends FilterView {

    private String id;
    private String alias;
    private String description;
    private Condition c;

    public GenericFixedFilterView(FilterState state, int queryId){
        this(state.getName(), new CustomCondition(state.getValues().get("condition")), state.getValues().get("description"), queryId, state.getID());
    }

    public GenericFixedFilterView(final String alias, final Condition c, String description, int queryId, final String id){
        super(alias, queryId);

        this.alias = alias;
        this.description = description;
        this.id = id;
        this.c = c;

        setBorder(ViewUtil.getMediumBorder());
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        JLabel label = new JLabel("<HTML>" + description + "</HTML>");
        label.setPreferredSize(new Dimension(10,10));
        add(label);
        add(Box.createRigidArea(new Dimension(1,50)));
        add(Box.createHorizontalGlue());


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
            public String getID() {
                return id;
            }

        };
        FilterController.addFilter(f, getQueryID());
    }

    @Override
    public FilterState saveState() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("description", description);
        map.put("condition", replaceAllTableReferences(c.toString()));
        return new FilterState(Filter.Type.GENERIC, alias, id, map);
    }

    private String replaceAllTableReferences(String s){
        return s.replaceAll("t[0-9]+\\.", "");
    }

    @Override
    public void resetView() {
    }

}
