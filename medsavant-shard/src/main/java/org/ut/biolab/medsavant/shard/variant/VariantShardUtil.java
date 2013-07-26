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
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.shards.ShardedConfiguration;
import org.hibernate.shards.cfg.ConfigurationToShardConfigurationAdapter;
import org.hibernate.shards.cfg.ShardConfiguration;
import org.hibernate.shards.strategy.ShardStrategyFactory;

/**
 * Constructor of factories and necessary sharding utils.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class VariantShardUtil {
    // TODO: check whether this is true
    private static final long MAX_VARIANT_POSITION = 250000000;
    private static final String RESOURCE_PREFIX = "hibernate";
    private static final String RESOURCE_SUFFIX = ".cfg.xml";
    private static final Integer VIRTUAL_SHARD_NO = 32;
    private static Integer shardNo;
    private static SessionFactory sessionFactory;

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

    static {
        try {
            // initial config
            Configuration config = new Configuration();
            config.configure(RESOURCE_PREFIX + "0" + RESOURCE_SUFFIX);
            config.addResource("variant.hbm.xml");

            // autodetect shards
            List<ShardConfiguration> shardConfigs = new ArrayList<ShardConfiguration>();
            shardNo = 0;
            boolean loadedAll = false;
            while (!loadedAll) {
                try {
                    Configuration c = new Configuration().configure(RESOURCE_PREFIX + shardNo + RESOURCE_SUFFIX);
                    shardConfigs.add(new ConfigurationToShardConfigurationAdapter(c));
                } catch (HibernateException ex) {
                    // loaded all resources
                    loadedAll = true;
                }
                shardNo++;
            }

            // prepare shard strategy factory
            ShardStrategyFactory shardStrategyFactory = buildShardStrategyFactory();
            ShardedConfiguration shardedConfig = new ShardedConfiguration(config, shardConfigs, shardStrategyFactory, createVirtualShards(VIRTUAL_SHARD_NO, shardNo));
            sessionFactory = shardedConfig.buildShardedSessionFactory();
        } catch (Throwable ex) {
            ex.printStackTrace();
            sessionFactory = null;
        }
    }

    static ShardStrategyFactory buildShardStrategyFactory() {
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

    static Integer getShardNo() {
        return shardNo;
    }
}
