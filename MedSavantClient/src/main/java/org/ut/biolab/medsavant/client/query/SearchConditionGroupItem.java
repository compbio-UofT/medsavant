/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ut.biolab.medsavant.client.query.SearchConditionItem.SearchConditionListener;
import org.ut.biolab.medsavant.client.query.view.SearchConditionItemView;

/**
 *
 * @author mfiume
 */
public class SearchConditionGroupItem extends SearchConditionItem implements SearchConditionListener {

    private final List<SearchConditionItem> items;
    private static int groupNo;
    private final int thisGroupNo;
    
    
    public enum QueryRelation {

        AND {
            @Override
            public String toString() {
                return "and";
            };
        }, OR {
            @Override
            public String toString() {
                return "or";
            };
        }
    };

    public SearchConditionGroupItem(SearchConditionGroupItem parent) {
        this(QueryRelation.AND, parent);
    }

    public SearchConditionGroupItem(QueryRelation r, SearchConditionItem i, SearchConditionGroupItem parent) {
        super(null, r, parent);

        thisGroupNo = (++groupNo);

        items = new ArrayList<SearchConditionItem>();
        if (i != null) {
            items.add(i);
            i.setParent(this);
            i.addListener(this);
        }
       
    }

    public boolean isGroup(){
        return true;
    }
    
    @Override
    public String getName() {
        return "Group " + thisGroupNo;
    }

    public boolean isFirstItem(SearchConditionItem item) {
        return items.indexOf(item) == 0;
    }

    public SearchConditionGroupItem(QueryRelation r, SearchConditionGroupItem parent) {
        this(r, null, parent);
    }

    @Override
    public String toString() {
        String s = "";

        for (int i = 0; i < items.size(); i++) {
            SearchConditionItem item = items.get(i);
            if (item instanceof SearchConditionGroupItem) {
                s += "(" + item.toString() + ")";
            } else {
                s += item.toString();
            }
            if (i != items.size()) {
                s += " " + this.getRelation() + " ";
            }
        }

        return s;
    }
    
    
    protected String toXML(int indent){
        String tab = "";
        for(int i = 0; i < indent; ++i){
           tab += "\t";
        }
        
        String xml = tab + "<Group ";
        xml += " queryRelation=\""+escape(getRelation().toString()) + "\"";
        if(getDescription() != null){
            xml += " description=\""+escape(getDescription())+"\"";
        }
        xml += ">\n";
        
        for(SearchConditionItem sci : items){
            xml += sci.toXML(indent+1);
        }        
        xml += tab+"</Group>\n";        
        return xml;
    }
    
    public String toXML(){
        return this.toXML(0);        
    }

    
    public void removeItem(SearchConditionItem i) {        
        i.removeListener(this);
        items.remove(i);
        i.setParent(null);

        // the only child is a group
        // Commented out, June 8,2013
        // The group functionality is being used to search Genomic Regions (sets
        // of (chromosome, position) tuples), and it's easier if these always 
        // stay grouped.
        /*
        if (items.size() == 1 && items.get(0) instanceof SearchConditionGroupItem) {

            SearchConditionGroupItem child = (SearchConditionGroupItem) items.get(0);
            for (SearchConditionItem c : child.getItems()) {
                c.removeListener(child);
                child.items.remove(i);
                c.setParent(null);

                c.addListener(this);
                c.setParent(this);
                items.add(c);
            }

            child.removeListener(this);
            this.items.remove(child);
            child.setParent(null);

        }
*/
        // remove the group entirely, parent notifies of update
        if (items.isEmpty() && this.getParent() != null) {
            this.getParent().removeItem(this);

            // notify listeners of change
        } else {
            fireSearchConditionOrderChangedEvent();
            fireSearchConditionItemRemovedEvent(i);
        }
    }

    public void addItem(SearchConditionItem i, int atIndex) {
       
        i.addListener(this);
        i.setParent(this);
        items.add(atIndex, i);
        fireSearchConditionOrderChangedEvent();
        fireSearchConditionItemAddedEvent(i);
    }

    public void addItem(SearchConditionItem i) {
        addItem(i, items.size()); // add to the end
    }

    public void createGroupFromItem(SearchConditionItem i) {        
        i.removeListener(this);
        int indexOfItem = items.indexOf(i);
        items.remove(i);
        SearchConditionGroupItem g = new SearchConditionGroupItem(QueryRelation.AND, i, this);
        addItem(g, indexOfItem);
    }

    public void moveItemToGroupAtIndex(SearchConditionItem i, SearchConditionGroupItem g, int newIndex) {
        i.removeListener(this);
        items.remove(i);
        i.setParent(null);
        g.addItem(i);
        g.moveItemToIndex(i, newIndex);
        fireSearchConditionOrderChangedEvent();
    }

    public void moveItemToGroup(SearchConditionItem i, SearchConditionGroupItem g) {
        i.removeListener(this);
        items.remove(i);
        i.setParent(null);
        g.addItem(i);

        if(items.size() < 1){
            if(getParent() != null){
                getParent().removeItem(this);
            }
        }else{
            fireSearchConditionOrderChangedEvent();
        }
    }

    public void moveItemToIndex(SearchConditionItem i, int newIndex) {
        int currentIndex = items.indexOf(i);
        items.remove(i);
        items.add(newIndex, i);
        fireSearchConditionOrderChangedEvent();
    }

    public void clearItems() {
        for (SearchConditionItem i : items) {
            i.removeListener(this);
        }
        this.items.removeAll(items);
        fireSearchConditionOrderChangedEvent();
    }

    public List<SearchConditionItem> getItems() {
        return this.items;
    }

    @Override
    public void searchConditionsOrderChanged(SearchConditionItem m) {
        this.fireSearchConditionOrderChangedEvent();
    }

    @Override
    public void searchConditionItemRemoved(SearchConditionItem m) {
        this.fireSearchConditionItemRemovedEvent(m);
    }

    @Override
    public void searchConditionItemAdded(SearchConditionItem m) {
        this.fireSearchConditionItemAddedEvent(m);
    }

        @Override
    public void searchConditionEdited(SearchConditionItem m) {
        this.fireSearchConditionsEditedEvent(m);
    }
}
