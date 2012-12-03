/*
 *    Copyright 2012 University of Toronto
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

package org.ut.biolab.medsavant.client.region;

import java.util.List;

import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.model.RegionSet;


/**
 * Class which wraps up a collection of GenomicRegions and makes them look like a RegionSet.
 *
 * @author tarkvara
 */
public class AdHocRegionSet extends RegionSet {

    final List<GenomicRegion> regions;

    public AdHocRegionSet(String name, List<GenomicRegion> r) {
        super(-1, name, r.size());
        regions = r;
    }
}
