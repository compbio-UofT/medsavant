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

package org.ut.biolab.medsavant.util;

import java.util.List;

/**
 *
 * @author mfiume, Andrew
 */
public abstract class DataRetriever<T> {

    public abstract List<T> retrieve(int start, int limit);
    public abstract int getTotalNum();
    public abstract void retrievalComplete();
    
    /**
     * Simplest possible data-retriever just pulls records out of an existing list.
     */
    public static DataRetriever createPrefetchedDataRetriever(final List data) {
        return new DataRetriever() {
            @Override
            public List retrieve(int start, int limit) {
               return data.subList(start, Math.min(start+limit, data.size()));
            }
            
            @Override
            public int getTotalNum() {
                return data.size();
            }

            @Override
            public void retrievalComplete(){
            };
        };
    }

}
