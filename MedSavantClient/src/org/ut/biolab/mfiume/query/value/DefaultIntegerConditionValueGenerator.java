package org.ut.biolab.mfiume.query.value;

/**
 *
 * @author mfiume
 */
public class DefaultIntegerConditionValueGenerator extends IntegerConditionValueGenerator {

    @Override
    public int[] getExtremeIntegerValues() {
         return new int[] {1,100};
     }
}
