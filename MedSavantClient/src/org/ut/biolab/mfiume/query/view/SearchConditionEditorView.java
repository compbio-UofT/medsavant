package org.ut.biolab.mfiume.query.view;

import java.io.Serializable;
import javax.swing.JPanel;
import org.ut.biolab.mfiume.query.SearchConditionItem;

/**
 *
 * @author mfiume
 */
public abstract class SearchConditionEditorView extends JPanel implements Serializable {

    protected final SearchConditionItem item;

    public SearchConditionEditorView(SearchConditionItem i) {
        this.item = i;
    }

    public void saveSearchConditionParameters(String encoding) {
        item.setSearchConditionEncoding(encoding);
    }
    public abstract void loadViewFromSearchConditionParameters(String encoding) throws ConditionRestorationException;
    public void loadViewFromExistingSearchConditionParameters() throws ConditionRestorationException {
        String encoding = item.getSearchConditionEncoding();
        System.out.println("Loading " + item.getName() + " from " + encoding);
        loadViewFromSearchConditionParameters(encoding);
    }

    public class ConditionRestorationException extends Exception {
        public ConditionRestorationException(String msg) {
            super(msg);
        }
    }

}
