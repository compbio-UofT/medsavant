package org.ut.biolab.mfiume.query.view;

import java.util.Arrays;
import java.util.List;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.value.FloatConditionValueGenerator;
import org.ut.biolab.mfiume.query.value.IntegerConditionValueGenerator;
import org.ut.biolab.mfiume.query.value.StringConditionValueGenerator;

/**
 *
 * @author mfiume
 */
public class NumberSearchConditionEditorView extends SearchConditionEditorView {

    private final FloatConditionValueGenerator generator;

    public NumberSearchConditionEditorView(SearchConditionItem i, final FloatConditionValueGenerator g) {
        super(i);
        this.generator = g;
    }

    @Override
    public void loadViewFromSearchConditionParameters(String encoding) throws ConditionRestorationException {
        float[] selectedValues;
        if (encoding == null) {
            selectedValues = null;
        } else {
            selectedValues = NumberSearchConditionEditorView.unencodeConditions(encoding);
        }

        System.out.println("Asking generator for values");
        final float[] values = generator.getExtremeFloatValues();
        this.removeAll();


    }
    /**
     * Serialization
     */
    private static String DELIM = ",";

    public static float[] unencodeConditions(String s) {
        String[] arr = s.split(DELIM);
        float[] values = new float[arr.length];
        for (int i = 0; i < arr.length; i++) {
            values[i] = Float.parseFloat(s);
        }
        return values;
    }
}
