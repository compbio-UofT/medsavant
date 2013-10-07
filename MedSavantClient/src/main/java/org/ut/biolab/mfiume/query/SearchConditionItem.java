package org.ut.biolab.mfiume.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.NameValuePair;
import org.ut.biolab.mfiume.query.SearchConditionGroupItem.QueryRelation;

import org.apache.commons.lang3.StringEscapeUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.savant.analytics.savantanalytics.AnalyticsAgent;

/**
 *
 * @author mfiume
 */
public class SearchConditionItem implements Serializable {

    private String explanation;

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


        try {
            AnalyticsAgent.log(
                    new NameValuePair[]{
                        new NameValuePair("search-event", "ConditionCreated"),
                        new NameValuePair("condition-name", name)
                    });
        } catch (Exception e) {
        }
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

    public boolean isGroup() {
        return false;
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

    public void setSearchConditionEncoding(String s) {
        this.encodedConditions = s;
    }

    public String getSearchConditionEncoding() {
        return encodedConditions;
    }

    public ConditionState getState() {
        if (encodedConditions == null) {
            return ConditionState.UNSET;
        } else {
            return ConditionState.SET;
        }
    }

    protected String escape(String s) {
        if (s == null) {
            return "";
        } else {
            return StringEscapeUtils.escapeXml(s);
        }
    }

    protected String toXML(int indent) {
        String tab = "";
        for (int i = 0; i < indent; ++i) {
            tab += "\t";
        }
        String xml = tab + "<Item";
        xml += " description=\"" + escape(description) + "\"";
        xml += " encodedConditions=\"" + escape(encodedConditions) + "\"";
        xml += " name=\"" + escape(name) + "\"";
        xml += " queryRelation=\"" + escape(relation.toString()) + "\"";
        xml += ">\n";
        xml += tab + "</Item>\n";
        return xml;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
        this.fireSearchConditionsEditedEvent(this);
    }

    public String getExplanation() {
        return explanation;
    }

    public String toXML() {
        return toXML(0);
    }
}
