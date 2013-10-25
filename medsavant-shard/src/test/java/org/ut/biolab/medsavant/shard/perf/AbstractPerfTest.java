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

import org.testng.annotations.BeforeClass;
import org.ut.biolab.medsavant.shard.AbstractShardTest;
import org.ut.biolab.medsavant.shard.variant.ShardedSessionManager;

/**
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class AbstractPerfTest extends AbstractShardTest{
    private long start;
    private long stop;

    public void recordStart() {
        start = System.nanoTime();
    }

    public void recordStop() {
        stop = System.nanoTime();
    }

    public double getTimeInMilis() {
        return ((double) (stop - start)) / 1000000;
    }

    @BeforeClass
    public void init() {
        System.out.println("Number of shards: " + ShardedSessionManager.getShardNo());
    }

}
