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
package org.ut.biolab.medsavant.client.filter;

/**
 * Boolean filter as special case of string list with two items.
 *
 * @author tarkvara
 */
public class BooleanFilterView extends StringListFilterView {

    public BooleanFilterView(FilterState state, int queryID) throws Exception {
        super(state, queryID);
    }

    public BooleanFilterView(WhichTable t, String colName, int queryID, String alias) throws Exception {
        super(t, colName, queryID, alias, true);
    }
}
