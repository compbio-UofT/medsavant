package org.ut.biolab.mfiume.query.condition;

import org.ut.biolab.mfiume.query.view.SearchConditionEditorView;

/**
 *
 * @author mfiume
 */
public interface ConditionSerializer {
    public SearchConditionEditorView getEditor();
    public String serializeConditions(Object s);
    public Object deserializeConditions(String s);
}
