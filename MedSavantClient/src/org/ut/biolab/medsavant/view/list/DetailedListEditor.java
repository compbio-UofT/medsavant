/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.view.list;

import java.util.List;

/**
 *
 * @author mfiume
 */
public class DetailedListEditor {

    public boolean doesImplementAdding() {
        return false;
    }

    public boolean doesImplementDeleting() {
        return false;
    }

    public boolean doesImplementEditing() {
        return false;
    }
    
    public boolean doesImplementImporting() {
        return false;
    }

    /**
     * For the filter-set editor only.  Add a load button above the other buttons.
     */
    public boolean doesImplementLoading() {
        return false;
    }

    public void addItems() {
    }

    public void deleteItems(List<Object[]> items) {
    }

    public void editItem(Object[] item) {
    }

    public void importItems() {
    }
    
    public void loadItems(List<Object[]> item) {
    }
}
