package org.ut.biolab.mfiume.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.mfiume.query.SearchConditionGroupItem.QueryRelation;
import org.ut.biolab.mfiume.query.view.SearchConditionEditorView;

/**
 *
 * @author mfiume
 */
public class SearchConditionItem implements Serializable {

    public enum ConditionState {

        UNSET, SET
    };
    private String description;
    private String encodedConditions;
    private List<SearchConditionListener> orderlisteners;
    private final String name;
    private QueryRelation relation;
    private SearchConditionGroupItem parent;

    public SearchConditionItem(String name, SearchConditionGroupItem parent) {
        this(name, QueryRelation.AND, parent);
    }

    public SearchConditionItem(String name, QueryRelation r, SearchConditionGroupItem parent) {

        this.name = name;
        this.parent = parent;
        this.relation = r;

        orderlisteners = new ArrayList<SearchConditionListener>();

    }

    public String getName() {
        return name;
    }

    public QueryRelation getRelation() {
        return this.relation;
    }

    public void changeRelationTo(QueryRelation r) {
        this.relation = r;
    }

    public SearchConditionGroupItem getParent() {
        return parent;
    }

    public void setDescription(String s) {
        this.description = s;
        fireSearchConditionsEditedEvent(this);
    }

    public String getDescription() {
        return this.description;
    }


    public void addListener(SearchConditionListener l) {
        orderlisteners.add(l);
    }

    protected void fireSearchConditionOrderChangedEvent() {
        for (SearchConditionListener l : orderlisteners) {
            l.searchConditionsOrderChanged(this);
        }
    }

    protected void fireSearchConditionsEditedEvent(SearchConditionItem m) {
        for (SearchConditionListener l : orderlisteners) {
            l.searchConditionEdited(m);
        }
    }

    protected void fireSearchConditionItemAddedEvent(SearchConditionItem item) {
        for (SearchConditionListener l : orderlisteners) {
            l.searchConditionItemAdded(item);
        }
    }

    protected void fireSearchConditionItemRemovedEvent(SearchConditionItem item) {
        for (SearchConditionListener l : orderlisteners) {
            l.searchConditionItemRemoved(item);
        }
    }

    public void setRelation(QueryRelation r) {
        this.relation = r;
        fireSearchConditionsEditedEvent(this);
    }

    void removeListener(SearchConditionListener l) {
        orderlisteners.remove(l);
    }

    void setParent(SearchConditionGroupItem p) {
        parent = p;
    }

    public interface SearchConditionListener {

        public void searchConditionsOrderChanged(SearchConditionItem m);

        public void searchConditionItemRemoved(SearchConditionItem m);

        public void searchConditionItemAdded(SearchConditionItem m);

        public void searchConditionEdited(SearchConditionItem m);
    }

    /*public void setDescription(String s) {
     System.out.println(s);
     this.description = s;
     }

     public String getConditionDescription() {
     return description;
     }
     */
    public void setSearchConditionEncoding(String s) {
        this.encodedConditions = s;
    }

    public String getSearchConditionEncoding() {
        //System.out.println("Encoded conditions for " + this.getName() + " is " + encodedConditions);
        return encodedConditions;
    }

    public ConditionState getState() {
        if (encodedConditions == null) {
            return ConditionState.UNSET;
        } else {
            return ConditionState.SET;
        }
    }
}
