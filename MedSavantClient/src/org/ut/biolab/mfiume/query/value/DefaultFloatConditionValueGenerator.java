package org.ut.biolab.mfiume.query.value;

/**
 *
 * @author mfiume
 */
public class DefaultFloatConditionValueGenerator extends FloatConditionValueGenerator {

    @Override
    public float[] getExtremeFloatValues() {
         return new float[] {1.0f,50.0f};
     }
}
