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
package org.ut.biolab.medsavant.shared.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;


/**
 *
 * @author Andrew
 */
public class RegionSet implements Serializable {

    private final int id;
    private final String name;
    private final int size;

    public RegionSet(int id, String name, int size) {
        this.id = id;
        this.name = name;
        this.size = size;
    }

    public int getID(){
        return id;
    }

    public String getName(){
        return name;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString(){
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RegionSet) {
            RegionSet that = (RegionSet)o;
            return id == that.id && StringUtils.equals(name, that.name) && size == that.size;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.id;
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 79 * hash + this.size;
        return hash;
    }
}
