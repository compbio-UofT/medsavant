/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.mfiume.query;

import com.healthmarketscience.sqlbuilder.Condition;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.ut.biolab.mfiume.query.view.SearchConditionItemView;

/**
 *
 * @author mfiume
 */
public interface ConditionViewGenerator {
    public SearchConditionItemView generateViewForItem(SearchConditionItem item);
    public Condition generateConditionForItem(SearchConditionItem item) throws Exception;
    public Map<String,List<String>> getAllowableItemNames();
}
