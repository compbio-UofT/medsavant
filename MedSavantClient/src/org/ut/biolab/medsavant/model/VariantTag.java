package org.ut.biolab.medsavant.model;

/**
 *
 * @author mfiume
 */
public class VariantTag {
    
    public final String key;
    public final String value;

    public VariantTag(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return key + " = " + value;
    }
    
    
}
