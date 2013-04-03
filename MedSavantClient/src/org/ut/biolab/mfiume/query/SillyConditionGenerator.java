package org.ut.biolab.mfiume.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.ut.biolab.mfiume.query.value.DefaultStringConditionValueGenerator;
import org.ut.biolab.mfiume.query.view.SearchConditionItemView;
import org.ut.biolab.mfiume.query.view.StringSearchConditionEditorView;

/**
 *
 * @author mfiume
 */
public class SillyConditionGenerator implements ConditionViewGenerator {

    @Override
    public SearchConditionItemView generateViewForItem(SearchConditionItem item) {
        StringSearchConditionEditorView editor = new StringSearchConditionEditorView(item, new DefaultStringConditionValueGenerator());
        SearchConditionItemView view = new SearchConditionItemView(item, editor);
        return view;
    }

    @Override
    public Map<String,List<String>> getAllowableItemNames() {
        List<String> ops = new ArrayList<String>();
        ops.add("String");
        ops.add("Integer");
        ops.add("Float");
        List<String> ls = new ArrayList<String>();
        ls.add("a");
        ls.add("b");
        ls.add("c");

        Map<String,List<String>> map = new HashMap<String,List<String>>();
        map.put("Words",ops);
        map.put("Letters",ls);

        return map;
    }

}
