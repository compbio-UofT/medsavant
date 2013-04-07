package org.ut.biolab.mfiume.query.value;

import com.healthmarketscience.sqlbuilder.Condition;

/**
 *
 * @author mfiume
 */
public abstract class DatabaseConditionGenerator {

    public abstract Condition getCondition(String selectionEncoding);

}
