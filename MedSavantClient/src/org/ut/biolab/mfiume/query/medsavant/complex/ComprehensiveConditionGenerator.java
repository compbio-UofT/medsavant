package org.ut.biolab.mfiume.query.medsavant.complex;

import com.healthmarketscience.sqlbuilder.Condition;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.view.SearchConditionItemView;

/**
 *
 * @author mfiume
 */
public interface ComprehensiveConditionGenerator {

    public String getName();

    public String category();

    public Condition getConditionsFromEncoding(String encoding) throws Exception;

    public SearchConditionItemView generateViewFromItem(SearchConditionItem item);
}
