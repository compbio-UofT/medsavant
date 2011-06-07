/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.filter;

import fiume.vcf.VariantRecord;
import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.medsavant.model.PostProcessFilter;
import org.ut.biolab.medsavant.model.record.VariantRecordModel;
import org.ut.biolab.medsavant.util.Util;

/**
 *
 * @author mfiume
 */
public class VariantRecordFilter extends PostProcessFilter {
    private final int fieldNum;

    public VariantRecordFilter(List<String> acceptableValues, int fieldNum) {
        this.acceptableValues = acceptableValues;
        this.fieldNum = fieldNum;
    }
    private List<String> acceptableValues;

    @Override
    public List<VariantRecord> filterResults(List<VariantRecord> results) {

        List<VariantRecord> filteredResults = new ArrayList<VariantRecord>();
        for (VariantRecord r : results) {
            if (accept(r)) {
                filteredResults.add(r);
            }
        }
        return filteredResults;
    }

    private boolean accept(VariantRecord r) {
        if (acceptableValues.contains(VariantRecordModel.getValueOfFieldAtIndex(fieldNum, r).toString())) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return Util.getListFilterToString(getName(), acceptableValues);
    }

    @Override
    public String getName() {
        return VariantRecordModel.getFieldNameForIndex(fieldNum);
    }
};
