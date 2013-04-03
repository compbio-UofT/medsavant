package org.ut.biolab.mfiume.query.value;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author mfiume
 */
public class DefaultStringConditionValueGenerator extends StringConditionValueGenerator {

    @Override
    public List<String> getStringValues() {
         return Arrays.asList(new String[] {"a","b","c"});
     }
}
