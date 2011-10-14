/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.model;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.util.List;
import org.ut.biolab.medsavant.olddb.MedSavantDatabase;
import org.ut.biolab.medsavant.olddb.QueryUtil;
import org.ut.biolab.medsavant.olddb.table.VariantTableSchema;
import org.ut.biolab.medsavant.db.model.Range;

/**
 *
 * @author AndrewBrook
 */
public abstract class RangeFilter extends QueryFilter {
    
    private RangeSet ranges;
    
    public RangeFilter(RangeSet ranges) {
        super();
        this.ranges = ranges;
    }
    
    public RangeFilter() {
        super();
    }
    
    public RangeSet getRangeSet(){
        return ranges;
    }
    
    public Condition[] getConditions(){
        Condition[] conditions = new Condition[ranges.getSize()];
        DbColumn posCol = MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(VariantTableSchema.ALIAS_POSITION);      
        DbColumn chrCol = MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(VariantTableSchema.ALIAS_CHROM);   
        Object[] chrs = ranges.getChrs();
        int pos = 0;
        for(Object o : chrs){
            String chrName = (String)o;
            List<Range> rangesInChr = ranges.getRanges(chrName);
            for(Range r : rangesInChr){
                Condition posCondition = QueryUtil.getRangeCondition(posCol, r);
                Condition chrCondition = BinaryCondition.equalTo(chrCol, chrName);
                conditions[pos] = ComboCondition.and(posCondition, chrCondition);
                pos++;
            }
        }  
        return conditions;
    }
    
    public void merge(RangeSet newRanges){
        if(this.ranges == null){
            this.ranges = newRanges;
        } else {
            this.ranges.merge(newRanges);            
        }
    }

}
