package org.ut.biolab.mfiume.query.view;

import java.awt.Dimension;
import java.io.Serializable;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import org.ut.biolab.mfiume.query.SearchConditionItem;

/**
 *
 * @author mfiume
 */
public abstract class SearchConditionEditorView extends JPanel implements Serializable {

    protected final SearchConditionItem item;

    public SearchConditionEditorView(SearchConditionItem i) {
        this.setOpaque(false);        
        this.item = i;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    protected Dimension getDialogDimension(){
        return null;
    }
     
    public void saveSearchConditionParameters(String encoding) {
        item.setSearchConditionEncoding(encoding);
    }
    public abstract void loadViewFromSearchConditionParameters(String encoding) throws ConditionRestorationException;
    public void loadViewFromExistingSearchConditionParameters() throws ConditionRestorationException {
        String encoding = item.getSearchConditionEncoding();
        loadViewFromSearchConditionParameters(encoding);
    }

    public class ConditionRestorationException extends Exception {
        public ConditionRestorationException(String msg) {
            super(msg);
        }
    }

    /*
    public Dimension getDialogSize(){ //todo 
        
    } */
}
