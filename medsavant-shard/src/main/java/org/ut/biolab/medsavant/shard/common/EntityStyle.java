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
package org.ut.biolab.medsavant.shard.common;

import java.util.Collection;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Style determining how meta information about an entity is obtained.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class EntityStyle extends ToStringStyle {

    private static final long serialVersionUID = -6907977923855540120L;
    private static ToStringStyle instance;

    protected EntityStyle() {
        // singleton pattern
        setArrayContentDetail(true);
        setUseShortClassName(true);
        setUseClassName(false);
        setUseIdentityHashCode(false);
        setFieldSeparator(",");
    }

    public static ToStringStyle getInstance() {
        if (instance == null) {
            instance = new EntityStyle();
        }
        return instance;
    };

    @Override
    public void appendDetail(StringBuffer buffer, String fieldName, Object value) {
        if (!value.getClass().getName().startsWith("java")) {
            buffer.append(ReflectionToStringBuilder.toString(value, instance));
        } else {
            super.appendDetail(buffer, fieldName, value);
        }
    }

    @Override
    public void appendDetail(StringBuffer buffer, String fieldName, Collection value) {
        appendDetail(buffer, fieldName, value.toArray());
    }
}