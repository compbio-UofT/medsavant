/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.list;

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

    public boolean doesRefreshAfterAdding() {
        return true;
    }

    public boolean doesRefreshAfterDeleting() {
        return true;
    }

    public boolean doesRefreshAfterEditing() {
        return true;
    }

    public boolean doesImplementImporting() {
        return false;
    }

    public boolean doesImplementExporting() {
        return false;
    }

    /**
     * For the filter-set editor only. Add a load button above the other
     * buttons.
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

    public void exportItems() {
    }

    public void loadItems(List<Object[]> item) {
    }
}
