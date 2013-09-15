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

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.shards.criteria.ShardedCriteriaImpl;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.testng.annotations.Test;
import org.ut.biolab.medsavant.shard.common.EntityStyle;
import org.ut.biolab.medsavant.shard.mapping.ClassField;
import org.ut.biolab.medsavant.shard.mapping.EntityGenerator;
import org.ut.biolab.medsavant.shard.mapping.VariantEntityGenerator;
import org.ut.biolab.medsavant.shard.mapping.VariantMappingGenerator;

/**
 * Tests to verify different types of queries.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class VariantQueryTest extends AbstractShardTest {

    @Test
    public void testSelectVariantsWithRestrictions() {
        Session session = ShardedSessionManager.openSession();

        // note: beware of this commented out query - it does implicit sorting
        // and many comparisons across shards - unusably slow
        // List<Variant> variantList =
        // session.createQuery("select p from Variant p").setMaxResults(10).list();

        Criteria crit = session.createCriteria(VariantEntityGenerator.getInstance().getCompiled());
        crit.add(Restrictions.lt(VariantMappingGenerator.getInstance().getId().getColumn(), 1));
        List<Variant> variantList = crit.list();

        Iterator<Variant> iterator = variantList.iterator();
        while (iterator.hasNext()) {
            Variant v = (Variant) iterator.next();
            System.out.println(v);
        }

        ShardedSessionManager.closeSession(session);
    }

    @Test
    public void testCountVariants() {
        Session s = ShardedSessionManager.openSession();

        Criteria c = s.createCriteria(VariantEntityGenerator.getInstance().getCompiled()).setProjection(
                Projections.count(VariantMappingGenerator.getInstance().getId().getColumn()));
        Integer res = ((BigDecimal) c.list().get(0)).intValue();

        ShardedSessionManager.closeSession(s);
        System.out.println("Count: " + res);
    }

    @Test
    public void testSelectVariantsWithLimit() {
        Session session = ShardedSessionManager.openSession();

        Criteria crit = session.createCriteria(VariantEntityGenerator.getInstance().getCompiled()).setMaxResults(2);
        List<Object> variantList = crit.list();

        Iterator<Object> iterator = variantList.iterator();
        while (iterator.hasNext()) {
            Object v = (Object) iterator.next();
            System.out.println(v);
        }
        ShardedSessionManager.closeSession(session);
    }

    @Test
    public void testSelectVariantsWithLimitAndOffset() {
        Session session = ShardedSessionManager.openSession();

        Criteria crit = session.createCriteria(VariantEntityGenerator.getInstance().getCompiled()).setFetchSize(2).setMaxResults(2).setFirstResult(6);
        List<Object> variantList = crit.list();

        Iterator<Object> iterator = variantList.iterator();
        while (iterator.hasNext()) {
            Object v = iterator.next();
            System.out.println(v);
        }
        ShardedSessionManager.closeSession(session);
    }

    @Test
    public void testArithmetic() {
        Session session = ShardedSessionManager.openSession();
        Criteria c = ((ShardedCriteriaImpl) session.createCriteria(VariantEntityGenerator.getInstance().getCompiled()))
                .setFetchSize(2)
                .setMaxResults(2)
                .setFirstResult(6)
                .setProjection(
                        Projections.sqlProjection("floor(3.14) as value, position as pos", new String[] { "pos", "value" }, new Type[] { new IntegerType(), new IntegerType() }));

        List<Object[]> os = c.list();
        for (Object[] o : os) {
            for (Object x : o) {
                System.out.println(x);
            }
        }

        ShardedSessionManager.closeSession(session);
    }

    @Test
    public void testDistinct() {
        Session session = ShardedSessionManager.openSession();

        Criteria c = ((ShardedCriteriaImpl) session.createCriteria(VariantEntityGenerator.getInstance().getCompiled())).setFetchSize(10).setMaxResults(10)
                .setProjection(Projections.sqlGroupProjection("dna_id as value", "value", new String[] { "value" }, new Type[] { new StringType() }));
        // .setProjection(Projections.distinct(Projections.property("dna_id")));

        List<Object[]> os = c.list();
        for (Object[] o : os) {
            for (Object x : o)
                System.out.println(x.toString());
        }

        ShardedSessionManager.closeSession(session);
    }

    @Test
    public void testArithmeticWithGroupBy() {
        Session session = ShardedSessionManager.openSession();
        Criteria c = ((ShardedCriteriaImpl) session.createCriteria(VariantEntityGenerator.getInstance().getCompiled()))
                .setFetchSize(4)
                .setMaxResults(4)
                .setProjection(
                        Projections.sqlGroupProjection("position as pos, floor(3.14) as value", "value", new String[] { "pos", "value" }, new Type[] { new IntegerType(),
                                new IntegerType() }));

        List<Object[]> os = c.list();
        for (Object[] o : os) {
            for (Object x : o) {
                System.out.println(x);
            }
        }

        ShardedSessionManager.closeSession(session);
    }

    @Test
    public void testMultiColumnGroupBy() {
        Session session = ShardedSessionManager.openSession();
        Criteria c = ((ShardedCriteriaImpl) session.createCriteria(VariantEntityGenerator.getInstance().getCompiled()))
                .setFetchSize(4)
                .setMaxResults(4)
                .setProjection(
                        Projections.sqlGroupProjection("position as pos, upload_id as value1, chrom as value2", "value1, value2", new String[] { "pos", "value1", "value2" },
                                new Type[] { new IntegerType(), new IntegerType(), new StringType() }));

        List<Object[]> os = c.list();
        for (Object[] o : os) {
            for (Object x : o) {
                System.out.println(x.toString());
            }
        }

        ShardedSessionManager.closeSession(session);
    }

    @Test
    public void testCountDistinct() {
        Session s = ShardedSessionManager.openSession();

        Criteria c = ((ShardedCriteriaImpl) s.createCriteria(VariantEntityGenerator.getInstance().getCompiled())).setProjection(Projections.sqlGroupProjection("dna_id as value",
                "value", new String[] { "value" }, new Type[] { new StringType() }));

        System.out.println(c.list().size());

        ShardedSessionManager.closeSession(s);
    }

    @Test
    public void testCountGroupBy() {
        Session s = ShardedSessionManager.openSession();

        Criteria c = ((ShardedCriteriaImpl) s.createCriteria(VariantEntityGenerator.getInstance().getCompiled())).setAggregateGroupByProjection(
                Projections.count(VariantMappingGenerator.getInstance().getId().getColumn()), Projections.groupProperty("variant_type"));
        List<Object[]> os = c.list();
        for (Object[] o : os) {
            for (Object x : o) {
                System.out.println(x.toString());
            }
        }

        ShardedSessionManager.closeSession(s);
    }

    @Test
    public void testMaxGroupBy() {
        Session s = ShardedSessionManager.openSession();

        Criteria c = ((ShardedCriteriaImpl) s.createCriteria(VariantEntityGenerator.getInstance().getCompiled())).setAggregateGroupByProjection(Projections.max("position"),
                Projections.groupProperty("variant_type"));
        List<Object[]> os = c.list();
        for (Object[] o : os) {
            for (Object x : o) {
                System.out.println(x.toString());
            }
        }

        ShardedSessionManager.closeSession(s);
    }

    @Test
    public void testMinGroupBy() {
        Session s = ShardedSessionManager.openSession();

        Criteria c = ((ShardedCriteriaImpl) s.createCriteria(VariantEntityGenerator.getInstance().getCompiled())).setAggregateGroupByProjection(Projections.min("qual"),
                Projections.groupProperty("variant_type"));
        List<Object[]> os = c.list();
        for (Object[] o : os) {
            for (Object x : o) {
                System.out.println(x.toString());
            }
        }

        ShardedSessionManager.closeSession(s);
    }

    @Test
    public void testCriteriaOnDynamicallyGeneratedClasses() {
        EntityGenerator e = VariantEntityGenerator.getInstance();
        e.compile();
        ShardedSessionManager.setClassInMapping();
        ShardedSessionManager.buildConfig();

        Session session = ShardedSessionManager.openSession();

        // show the original class
        Criteria crit = session.createCriteria(e.getCompiled()).setFetchSize(2).setMaxResults(2).setFirstResult(6);
        List<Object> variantList = crit.list();

        Iterator<Object> iterator = variantList.iterator();
        while (iterator.hasNext()) {
            Object o = iterator.next();
            System.out.println(ReflectionToStringBuilder.toString(o, EntityStyle.getInstance()));
        }

        ShardedSessionManager.closeSession(session);

        // modify class and try again
        e.addField(new ClassField("private", "String", "aa", "\"\""));
        e.compile();
        ShardedSessionManager.setClassInMapping();
        ShardedSessionManager.buildConfig();

        session = ShardedSessionManager.openSession();

        crit = session.createCriteria(e.getCompiled()).setFetchSize(2).setMaxResults(2).setFirstResult(6);
        variantList = crit.list();

        iterator = variantList.iterator();
        while (iterator.hasNext()) {
            Object o = iterator.next();
            System.out.println(ReflectionToStringBuilder.toString(o, EntityStyle.getInstance()));
        }

        ShardedSessionManager.closeSession(session);
    }

}
