package org.ut.biolab.mfiume.query;

import com.healthmarketscience.sqlbuilder.Condition;
import java.util.List;
import java.util.Map;
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
