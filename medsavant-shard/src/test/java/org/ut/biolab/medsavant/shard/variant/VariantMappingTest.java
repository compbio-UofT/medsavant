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

import java.math.BigDecimal;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.testng.annotations.Test;
import org.ut.biolab.medsavant.shard.common.MetaEntity;
import org.ut.biolab.medsavant.shard.mapping.VariantMapping;

/**
 * Tests to verify mapping is working.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class VariantMappingTest extends AbstractShardTest {

    @Test
    public void testAttributes() {
        MetaEntity<Variant> m = new MetaEntity<Variant>(Variant.class);
        for (String s : m.getAttributeNames()) {
            System.out.println(s);
        }
    }

    @Test
    public void testColumns() {
        for (String s : VariantMapping.getColumnNames()) {
            System.out.println(s);
        }
    }

    @Test
    public void testId() {
        System.out.println(VariantMapping.getIdColumn());
    }

    @Test
    public void testRemapping() {
        Session session = ShardedSessionManager.openSession();

        String table = ShardedSessionManager.getTable();
        Criteria c = session.createCriteria(Variant.class).setProjection(Projections.count(VariantMapping.getIdColumn()));
        Integer res = ((BigDecimal) c.list().get(0)).intValue();
        System.out.println("Table/count: " + table + " - " + res);

        ShardedSessionManager.closeSession(session);

        table = table + "_sub";
        ShardedSessionManager.setTable(table);

        session = ShardedSessionManager.openSession();

        c = session.createCriteria(Variant.class).setProjection(Projections.count(VariantMapping.getIdColumn()));
        res = ((BigDecimal) c.list().get(0)).intValue();
        System.out.println("Table/count: " + table + " - " + res);

        ShardedSessionManager.closeSession(session);
    }
}
