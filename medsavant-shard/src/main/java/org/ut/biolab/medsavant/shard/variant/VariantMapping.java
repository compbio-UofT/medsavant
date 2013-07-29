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
package org.ut.biolab.medsavant.shard.variant;

import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.ut.biolab.medsavant.shard.common.MetaEntity;

/**
 * Provider of information related to mapping of variants via Hibernate.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class VariantMapping {
    private static MetaEntity<Variant> model;
    private static PersistentClass mapping;

    static {
        model = new MetaEntity<Variant>(Variant.class);
        mapping = VariantShardUtil.getConfig().getClassMapping(Variant.class.getName());
    }

    /**
     * Retrieve columns in the table the entity is mapped to.
     * 
     * @return names of columns
     */
    public static String[] getColumnNames() {
        String[] atts = model.getAttributeNames();
        String[] res = new String[atts.length];
        for (int i = 0; i < atts.length; i++) {
            res[i] = ((Column) (mapping.getProperty(atts[i]).getColumnIterator().next())).getName();
        }

        return res;
    }

    /**
     * Retrieves the name of the column serving as ID.
     */
    public static String getIdColumn() {
        return mapping.getIdentifierProperty().getName();
    }

    /**
     * Count the number of columns in the mapping.
     * 
     * @return number of columns
     */
    public static int getColumnCount() {
        return model.getAttributesCount();
    }
}
