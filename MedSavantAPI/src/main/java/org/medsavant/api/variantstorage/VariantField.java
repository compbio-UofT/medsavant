
package org.medsavant.api.variantstorage;

/**
 * 
 * @author jim
 */
public interface VariantField {

    /**
     * 
     * @return The class of this column. 
     */
    public Class getColumnClass();           
    
    /**
     * @return A human-friendly name for this column.  
     */
    public String getName();

    /**
     * 
     * @return a boolean indicating if this column is unique
     */
    public boolean isUnique();
    
    @Override
    public int hashCode();
    
    @Override
    public boolean equals(Object o);
}
