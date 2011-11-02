/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.vcf;

import java.util.List;

/**
 *
 * @author AndrewBrook
 */
public class SortedVariantSet extends VariantSet {
    
    public SortedVariantSet(){
        super();
    }
    
    @Override
    void addRecords(List<VariantRecord> rs) {
        for(VariantRecord v : rs){
            int i = getInsertLocation(v);
            records.add(i, v);
        }
    }
    
    private int getInsertLocation(VariantRecord v){
        int min = 0;
        int max = records.size()-1;
        int mid = (min+max)/2;
        while(true){
            if(min > max){
                return mid;
            }
            VariantRecord v2 = records.get(mid);
            int compare = v.compareTo(v2);
            if(compare > 0){
                min = mid + 1;
            } else if (compare < 0){
                max = mid - 1;
            } else {
                return mid;
            }           
            mid = (min+max)/2;
        }
    }
    
}
