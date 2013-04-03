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


    public enum ConditionState { UNSET, SET };

    private String description;
    private String encodedConditions;
    private List<SearchConditionListener> listeners;
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

        listeners = new ArrayList<SearchConditionListener>();
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
        fireSearchConditionChangedEvent();
    }

    public String getDescription() {
        return this.description;
    }

    void addListener(SearchConditionListener l) {
        listeners.add(l);
    }

    protected void fireSearchConditionChangedEvent() {
        for (SearchConditionListener l : listeners) {
            l.searchConditionsChanged(this);
        }
    }

    protected void fireSearchConditionItemAddedEvent(SearchConditionItem item) {
        for (SearchConditionListener l : listeners) {
            l.searchConditionItemAdded(item);
        }
    }

    protected void fireSearchConditionItemRemovedEvent(SearchConditionItem item) {
        for (SearchConditionListener l : listeners) {
            l.searchConditionItemRemoved(item);
        }
    }

    public void setRelation(QueryRelation r) {
        this.relation = r;
        fireSearchConditionChangedEvent();
    }

    void removeListener(SearchConditionListener l) {
        listeners.remove(l);
    }

    void setParent(SearchConditionGroupItem p) {
        parent = p;
    }

    public interface SearchConditionListener {
        public void searchConditionsChanged(SearchConditionItem m);
        public void searchConditionItemRemoved(SearchConditionItem m);
        public void searchConditionItemAdded(SearchConditionItem m);
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
        System.out.println("Encoded conditions for " + this.getName() + " is " + encodedConditions);
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
