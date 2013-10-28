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
package org.ut.biolab.medsavant.shared.importing;

import java.io.Serializable;
import java.util.TreeMap;

/**
 *
 * @author mfiume
 */
public class BEDFormat extends FileFormat implements Serializable {

    @Override
    public String getName() {
        return "BED";
    }

    @Override
    public TreeMap<Integer, String> getFieldNumberToFieldNameMap() {
        TreeMap<Integer,String> map = new TreeMap<Integer,String>();
        map.put(0, "Chromosome");
        map.put(1, "Start");
        map.put(2, "End");
        map.put(3, "Name");
        return map;
    }

    @Override
    public TreeMap<Integer, Class> getFieldNumberToClassMap() {
        TreeMap<Integer,Class> map = new TreeMap<Integer,Class>();
        map.put(0, String.class);
        map.put(1, String.class); // todo: 1 and 2 should be ints!!
        map.put(2, String.class);
        map.put(3, String.class);
        return map;
    }



}
