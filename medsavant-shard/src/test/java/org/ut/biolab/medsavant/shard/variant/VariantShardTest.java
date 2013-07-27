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
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.testng.annotations.Test;

/**
 * Tests to verify sharding is working.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class VariantShardTest {
    @Test
    public void testSelectVariantsWithRestrictions() {
        Session session = ShardedConnectionController.openSession();

        // note: beware of this commented out query - it does implicit sorting
        // and many comparisons across shards - unusably slow
        // List<Variant> variantList =
        // session.createQuery("select p from Variant p").setMaxResults(10).list();

        Criteria crit = session.createCriteria(Variant.class);
        crit.add(Restrictions.lt(Variant.id, 1));
        List<Variant> variantList = crit.list();

        Iterator<Variant> iterator = variantList.iterator();
        while (iterator.hasNext()) {
            Variant v = (Variant) iterator.next();
            System.out.println(v);
        }
        ShardedConnectionController.closeSession(session);
    }

    @Test
    public void testCountVariants() {
        Session s = ShardedConnectionController.openSession();

        Criteria c = s.createCriteria(Variant.class).setProjection(Projections.count(Variant.id));
        Integer res = ((BigDecimal) c.list().get(0)).intValue();

        ShardedConnectionController.closeSession(s);
        System.out.println("Count: " + res);
    }

    @Test
    public void testSelectVariantsWithLimit() {
        Session session = ShardedConnectionController.openSession();

        Criteria crit = session.createCriteria(Variant.class).setMaxResults(2);
        List<Variant> variantList = crit.list();

        Iterator<Variant> iterator = variantList.iterator();
        while (iterator.hasNext()) {
            Variant v = (Variant) iterator.next();
            System.out.println(v);
        }
        ShardedConnectionController.closeSession(session);
    }

    @Test
    public void testSelectVariantsWithLimitAndOffset() {
        Session session = ShardedConnectionController.openSession();

        Criteria crit = session.createCriteria(Variant.class).setFetchSize(2).setMaxResults(2).setFirstResult(6);
        List<Variant> variantList = crit.list();

        Iterator<Variant> iterator = variantList.iterator();
        while (iterator.hasNext()) {
            Variant v = (Variant) iterator.next();
            System.out.println(v);
        }
        ShardedConnectionController.closeSession(session);
    }
}
