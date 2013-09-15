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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.testng.annotations.Test;
import org.ut.biolab.medsavant.shard.mapping.ClassField;
import org.ut.biolab.medsavant.shard.mapping.EntityGenerator;
import org.ut.biolab.medsavant.shard.mapping.SchemaMappingUtils;
import org.ut.biolab.medsavant.shard.mapping.VariantEntityGenerator;
import org.ut.biolab.medsavant.shard.mapping.VariantMappingGenerator;

/**
 * Tests to verify mapping is working.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class VariantMappingTest extends AbstractShardTest {

    @Test
    public void testAttributes() {
        for (ClassField s : VariantEntityGenerator.getInstance().getFields()) {
            System.out.println(s.getName());
        }
    }

    @Test
    public void testColumns() {
        for (String s : SchemaMappingUtils.getColumnsInMapping(VariantMappingGenerator.getInstance())) {
            System.out.println(s);
        }
    }

    @Test
    public void testId() {
        System.out.println(VariantMappingGenerator.getInstance().getId().getColumn());
    }

    @Test
    public void testRemapping() {
        Session session = ShardedSessionManager.openSession();

        String table = ShardedSessionManager.getTable();
        Criteria c = session.createCriteria(VariantEntityGenerator.getInstance().getCompiled()).setProjection(
                Projections.count(VariantMappingGenerator.getInstance().getId().getColumn()));
        Integer res = ((BigDecimal) c.list().get(0)).intValue();
        System.out.println("Table/count: " + table + " - " + res);

        ShardedSessionManager.closeSession(session);

        table = table + "_sub";
        ShardedSessionManager.setTable(table);
        ShardedSessionManager.buildConfig();

        session = ShardedSessionManager.openSession();

        c = session.createCriteria(VariantEntityGenerator.getInstance().getCompiled()).setProjection(Projections.count(VariantMappingGenerator.getInstance().getId().getColumn()));
        res = ((BigDecimal) c.list().get(0)).intValue();
        System.out.println("Table/count: " + table + " - " + res);

        ShardedSessionManager.closeSession(session);
    }

    @Test
    public void testCompilation() {
        // Prepare source somehow.
        String source = "package org.ut.biolab.medsavant.shard.variant; public class Test { static { System.out.println(\"hello\"); } public Test() { System.out.println(\"world\"); } }";

        try {
            // Save source in .java file.
            File root = new File("/tmp"); // On Windows running on C:\, this is
                                          // C:\java.
            File sourceFile = new File(root, "org/ut/biolab/medsavant/shard/variant/Test.java");
            sourceFile.getParentFile().mkdirs();

            new FileWriter(sourceFile).append(source).close();

            // Compile source file.
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            compiler.run(null, null, null, sourceFile.getPath());

            // Load and instantiate compiled class.
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { root.toURI().toURL() });
            Class<?> cls = Class.forName("org.ut.biolab.medsavant.shard.variant.Test", true, classLoader); // Should
            // print "hello".
            Object instance = cls.newInstance(); // Should print "world".
            System.out.println(instance); // Should print "test.Test@hashcode".
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testVariantEntityGenerator() {
        EntityGenerator v = VariantEntityGenerator.getInstance();
        v.compile();

        System.out.println(v.getSource());
    }

    @Test
    public void testGenerateNewField() {
        EntityGenerator v = VariantEntityGenerator.getInstance();
        v.addField(new ClassField("private", "String", "aa", "\"\""));
        v.compile();

        System.out.println(v.getSource());
    }

}
