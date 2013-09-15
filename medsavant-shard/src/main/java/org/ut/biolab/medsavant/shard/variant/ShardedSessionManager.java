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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.shards.ShardedConfiguration;
import org.hibernate.shards.cfg.ConfigurationToShardConfigurationAdapter;
import org.hibernate.shards.cfg.ShardConfiguration;
import org.hibernate.shards.strategy.ShardStrategyFactory;
import org.ut.biolab.medsavant.shard.mapping.ClassField;
import org.ut.biolab.medsavant.shard.mapping.MappingProperty;
import org.ut.biolab.medsavant.shard.mapping.VariantEntityGenerator;
import org.ut.biolab.medsavant.shard.mapping.VariantMappingGenerator;
import org.ut.biolab.medsavant.shard.strategy.PositionShardStrategyFactory;

/**
 * Manager of sharded sessions.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class ShardedSessionManager {
    // TODO: check whether this is true
    public static final long MAX_VARIANT_POSITION = 250000000;
    private static final String RESOURCE_PREFIX = "hibernate";
    private static final String RESOURCE_SUFFIX = ".cfg.xml";
    private static final Integer VIRTUAL_SHARD_NO = 32;
    private static Integer shardNo;
    private static SessionFactory sessionFactory;
    private static ShardStrategyFactory shardStrategyFactory;
    private static Configuration config;
    private static List<ShardConfiguration> shardConfigs;
    private static String classInMapping;

    static {
        try {
            // autodetect shards
            shardConfigs = new ArrayList<ShardConfiguration>();
            shardNo = 0;
            boolean loadedAll = false;
            while (!loadedAll) {
                try {
                    Configuration c = new Configuration().configure(RESOURCE_PREFIX + shardNo + RESOURCE_SUFFIX);
                    shardConfigs.add(new ConfigurationToShardConfigurationAdapter(c));
                    shardNo++;
                } catch (HibernateException ex) {
                    // loaded all resources
                    loadedAll = true;
                }
            }

            // prepare shard utils
            shardStrategyFactory = buildShardStrategyFactory();
            setClassInMapping();
            buildConfig();
        } catch (Throwable ex) {
            ex.printStackTrace();
            sessionFactory = null;
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    private static Map<Integer, Integer> createVirtualShards(Integer virt, Integer phys) {
        Map<Integer, Integer> virtualShardMap = new HashMap<Integer, Integer>();
        for (int i = 0; i < virt; i++) {
            virtualShardMap.put(i, i % phys);
        }

        return virtualShardMap;
    }

    /**
     * Rebuilds shards configuration.
     * 
     * Necessary to run after changing class/table.
     */
    public static synchronized void buildConfig() {
        // initialize config
        config = new Configuration();
        config.configure(RESOURCE_PREFIX + "0" + RESOURCE_SUFFIX);
        config.addXML(VariantMappingGenerator.getInstance().getMapping());

        // build sharded config
        ShardedConfiguration shardedConfig = new ShardedConfiguration(config, shardConfigs, shardStrategyFactory, createVirtualShards(VIRTUAL_SHARD_NO, shardNo));
        sessionFactory = shardedConfig.buildShardedSessionFactory();
    }

    private static ShardStrategyFactory buildShardStrategyFactory() {
        ThreadFactory factory = new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            }
        };

        final ThreadPoolExecutor exec = new ThreadPoolExecutor(10, 50, 60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), factory);

        ShardStrategyFactory shardStrategyFactory = new PositionShardStrategyFactory(MAX_VARIANT_POSITION, shardNo, exec);
        return shardStrategyFactory;
    }

    /**
     * Retrieves number of shards in use.
     * 
     * @return shard count
     */
    public static Integer getShardNo() {
        return shardNo;
    }

    /**
     * Exposes active configuration.
     * 
     * @return configuration
     */
    public static Configuration getConfig() {
        return config;
    }

    /**
     * Exposes shard-specific configuration.
     * 
     * @param shardId
     *            id of the shard
     * @return configuration of the given shard
     */
    public static ShardConfiguration getConfig(int shardId) {
        return (shardId < 0 || shardId >= shardConfigs.size()) ? null : shardConfigs.get(shardId);
    }

    /**
     * Updates the configuration with a mapping pointing to the current class.
     * 
     * @return true if the mapping was changed, false otherwise
     */
    public static synchronized boolean setClassInMapping() {
        if (classInMapping == null) {
            VariantEntityGenerator.getInstance().compile();
        }
        if (!VariantEntityGenerator.getInstance().getClassName().equals(classInMapping)) {
            classInMapping = VariantEntityGenerator.getInstance().getClassName();
            VariantMappingGenerator.getInstance().setClassName(VariantEntityGenerator.getInstance().getPackage(), VariantEntityGenerator.getInstance().getClassName());

            List<MappingProperty> properties = new ArrayList<MappingProperty>();
            for (ClassField f : VariantEntityGenerator.getInstance().getFields()) {
                MappingProperty p = new MappingProperty(f.getName(), f.getName(), f.getType().toLowerCase(), f.getName().toLowerCase().equals("position") ? true : false);
                if (p.isId()) {
                    VariantMappingGenerator.getInstance().setId(p);
                } else {
                    properties.add(p);
                }
            }
            VariantMappingGenerator.getInstance().setProperties(properties);

            // buildConfig();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Retrieves the table currently used in the mapping.
     * 
     * @return
     */
    public static String getTable() {
        return VariantMappingGenerator.getInstance().getTable();
    }

    /**
     * Updates the configuration with a mapping pointing to a new table.
     * 
     * @param table
     *            name of the new table
     * @return true if the table was changed, false otherwise
     */
    public static synchronized boolean setTable(String table) {
        if (!getTable().equals(table)) {
            VariantMappingGenerator.getInstance().setTable(table);
            // buildConfig();
            return true;
        }
        return false;
    }

    /**
     * Opens a new session.
     * 
     * @return session
     */
    public static Session openSession() {
        return getSessionFactory().openSession();
    }

    /**
     * Closes the given session.
     * 
     * @param session
     */
    public static void closeSession(Session session) {
        session.close();
    }
}
