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
