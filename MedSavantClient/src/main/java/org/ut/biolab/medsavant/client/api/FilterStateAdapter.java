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
package org.ut.biolab.medsavant.client.api;

import java.util.List;


/**
 * Gives MedSavant filter plugins access to the information stored within a <c>FilterState</c> object.
 *
 * @author tarkvara
 */
public interface FilterStateAdapter {

    /**
     * Retrieve the value of a unique element.
     */
    public String getOneValue(String key);

    /**
     * Store an element which will be unique.  For instance, any filter will have a single &lt;table&gt; element.
     */
    public void putOneValue(String key, Object val);

    /**
     * Retrieve a list of non-unique elements.
     */
    public List<String> getValues(String key);

    /**
     * Store a list of elements which are not assumed to be unique.
     */
    public void putValues(String key, List<String> vals);

    /**
     * Generate an XML representation of the filter's state.
     */
    public String generateXML();
}
