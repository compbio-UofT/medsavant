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
package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.model.RegionSet;

/**
 * Adapter class for mapping Solr documents to RegionSet objects.
 */
public class SearcheableRegionSet {

    private int id;
    private String name;
    private int size;

    private RegionSet regionSet;

    public SearcheableRegionSet() {  }

    public SearcheableRegionSet(RegionSet regionSet) {
        this.regionSet = regionSet;
    }

    @Field("id")
    public void setId(int id) {
        this.id = id;
    }

    @Field("name")
    public void setName(String name) {
        this.name = name;
    }

    @Field("size")
    public void setSize(int size) {
        this.size = size;
    }

    public RegionSet getRegionSet() {
        this.regionSet = new RegionSet(id,name,size );
        return regionSet;
    }

    public int getId() {
        return regionSet.getID();
    }

    public String getName() {
        return regionSet.getName();
    }

    public int getSize() {
        return regionSet.getSize();
    }
}

