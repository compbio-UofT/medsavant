package org.ut.biolab.mfiume.query.condition;

import java.util.Arrays;
import java.util.List;
import org.ut.biolab.mfiume.query.view.SearchConditionEditorView;

/**
 *
 * @author mfiume
 */
public class StringConditionSerializer implements ConditionSerializer {

    private final SearchConditionEditorView editor;

    public StringConditionSerializer(SearchConditionEditorView editor) {
        this.editor = editor;
    }

    @Override
    public SearchConditionEditorView getEditor() {
        return editor;
    }

    String DELIM = ",";

    @Override
    public String serializeConditions(Object s) {
        List values = (List) s;
        StringBuilder result = new StringBuilder();
        for (Object string : values) {
            result.append(string);
            result.append(DELIM);
        }
        return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
    }

    @Override
    public Object deserializeConditions(String s) {
        String[] arr = s.split(DELIM);
        return Arrays.asList(arr);
    }
}
