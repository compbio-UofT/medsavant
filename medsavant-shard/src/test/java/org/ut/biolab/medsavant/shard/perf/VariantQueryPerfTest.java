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
package org.ut.biolab.medsavant.shard.perf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.shards.criteria.ShardedCriteriaImpl;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.ut.biolab.medsavant.shard.mapping.VariantEntityGenerator;
import org.ut.biolab.medsavant.shard.mapping.VariantMappingGenerator;
import org.ut.biolab.medsavant.shard.variant.ShardedSessionManager;

/**
 * Performance tests for typical queries on sharded variants.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class VariantQueryPerfTest extends AbstractPerfTest {
    public static final String QUERY_FILE = "src/test/resources/filters.properties";
    private Session s;
    private Criteria c;
    private Properties queries = new Properties();

    @BeforeClass
    public void loadQueries() {
        try {
            queries.load(new FileInputStream(QUERY_FILE));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BeforeMethod
    public void startTest() {
        s = ShardedSessionManager.openSession();
        c = ((ShardedCriteriaImpl) s.createCriteria(VariantEntityGenerator.getInstance().getCompiled()));
    }

    @AfterMethod
    public void finishTest() {
        ShardedSessionManager.closeSession(s);

        System.out.println("Query execution time (ms): " + getTimeInMilis());
    }

    private void timeCriteriaExecution() {
        recordStart();
        c.list();
        recordStop();
    }

    @Test
    public void countAllVariantsTest() {
        c.setProjection(Projections.count(VariantMappingGenerator.getInstance().getId().getColumn()));

        timeCriteriaExecution();
    }

    @Test
    public void loadVariantsTest() {
        c.setFetchSize(500).setMaxResults(500).setFirstResult(0);

        timeCriteriaExecution();
    }

    @Test
    public void chartsStringTest() {
        c.setProjection(Projections.sqlGroupProjection("count('variant_id') as pos, dna_id as value", "value", new String[] { "pos", "value" }, new Type[] { new IntegerType(),
                new StringType() }));

        timeCriteriaExecution();
    }

    @Test
    public void chartsNumericTest() {
        c.setProjection(Projections.sqlGroupProjection("count('variant_id') as pos, floor(position / 1.0E7) as value", "value", new String[] { "pos", "value" }, new Type[] {
                new IntegerType(), new IntegerType() }));

        timeCriteriaExecution();
    }

    @Test
    public void chartsMultipleGroupByTest() {
        c.setProjection(
                Projections.sqlGroupProjection("count('variant_id') as pos, floor(position / 1.0E7) as value1, ref as value2", "value1, value2", new String[] { "pos", "value4",
                        "value2" }, new Type[] { new IntegerType(), new IntegerType(), new StringType() })).add(
                Restrictions.sqlRestriction(queries.getProperty("full_ref")));

        timeCriteriaExecution();
    }

    @Test
    public void distinctTest() {
        c.setProjection(Projections.sqlGroupProjection("dna_id as value", "value", new String[] { "value" }, new Type[] { new StringType() }));

        timeCriteriaExecution();
    }

    @Test
    public void countDistinctTest() {
        // intentionally the same as distinctTest()
        c.setProjection(Projections.sqlGroupProjection("dna_id as value", "value", new String[] { "value" }, new Type[] { new StringType() }));

        timeCriteriaExecution();
    }

    @Test
    public void smallIntervalCountTest() {
        c.setProjection(Projections.count(VariantMappingGenerator.getInstance().getId().getColumn())).add(
                Restrictions.sqlRestriction(queries.getProperty("small_interval")));

        timeCriteriaExecution();
    }

    @Test
    public void smallIntervalTest() {
        c.add(Restrictions.sqlRestriction(queries.getProperty("small_interval"))).setFetchSize(500).setMaxResults(500).setFirstResult(0);

        timeCriteriaExecution();
    }

    @Test
    public void largeIntervalCountTest() {
        c.setProjection(Projections.count(VariantMappingGenerator.getInstance().getId().getColumn())).add(
                Restrictions.sqlRestriction(queries.getProperty("large_interval")));

        timeCriteriaExecution();
    }

    @Test
    public void largeIntervalTest() {
        c.add(Restrictions.sqlRestriction(queries.getProperty("large_interval"))).setFetchSize(500).setMaxResults(500).setFirstResult(0);

        timeCriteriaExecution();
    }

    @Test
    public void stringMatchingCountTest() {
        c.setProjection(Projections.count(VariantMappingGenerator.getInstance().getId().getColumn())).add(Restrictions.sqlRestriction(queries.getProperty("restricted_ref")));

        timeCriteriaExecution();
    }

    @Test
    public void regionSetCountTest() {
        c.setProjection(Projections.count(VariantMappingGenerator.getInstance().getId().getColumn()))
                .add(Restrictions
                        .sqlRestriction(queries.getProperty("region_set")));

        timeCriteriaExecution();
    }

    @Test
    public void regionSetTest() {
        c.add(Restrictions
                .sqlRestriction(queries.getProperty("region_set")))
                .setFetchSize(500).setMaxResults(500).setFirstResult(0);

        timeCriteriaExecution();
    }

    @Test
    public void omimAngelmanSyndromeTest() {
        c.add(Restrictions
                .sqlRestriction(queries.getProperty("angelman")))
                .setFetchSize(500).setMaxResults(500).setFirstResult(0);

        timeCriteriaExecution();
    }

    @Test
    public void omimAngelmanSyndromeCountTest() {
        c.setProjection(Projections.count(VariantMappingGenerator.getInstance().getId().getColumn()))
                .add(Restrictions
                        .sqlRestriction(queries.getProperty("angelman")));

        timeCriteriaExecution();
    }

    @Test
    public void goTest() {
        c.add(Restrictions
                .sqlRestriction(queries.getProperty("go")))
                .setFetchSize(500).setMaxResults(500).setFirstResult(0);
        
        timeCriteriaExecution();
    }
    
    @Test
    public void goCountTest() {
        c.setProjection(Projections.count(VariantMappingGenerator.getInstance().getId().getColumn()))
        .add(Restrictions
                .sqlRestriction(queries.getProperty("go")));
        
        timeCriteriaExecution();
    }
}
