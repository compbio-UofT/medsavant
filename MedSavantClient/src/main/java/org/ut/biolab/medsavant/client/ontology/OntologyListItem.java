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

package org.ut.biolab.medsavant.client.ontology;

import org.ut.biolab.medsavant.shared.model.OntologyType;


/**
 * Little class which lets us populate a combo-box with ontology names.
 *
 * @author zig
 */
public class OntologyListItem {

    public static final OntologyListItem[] DEFAULT_ITEMS = new OntologyListItem[] {
        new OntologyListItem(OntologyType.GO),
        new OntologyListItem(OntologyType.HPO),
        new OntologyListItem(OntologyType.OMIM)
    };

    private final OntologyType type;

    public OntologyListItem(OntologyType ont) {
        type = ont;
    }

    @Override
    public String toString() {
        return OntologyFilter.ontologyToTitle(type);
    }

    public OntologyType getType() {
        return type;
    }
}
