/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.shared.model;


/**
 * We currently have three levels of users (plus one for <i>non</i>-users):
 *
 * <dl>
 * <dt><code>ADMIN</code></dt><dd>has full read and write access, and can create/delete users.</dd>
 * <dt><code>USER</code></dt><dd>has read access to data tables, and can create temp tables for importing variants.</dd>
 * <dt><code>GUEST</code></dt><dd>has read access to data tables.</dd>
 * <dt><code>NONE</code></dt><dd>not a known user; has no access to database.</dd>
 * </dl>
 *
 * @author tarkvara
 */
public enum UserLevel {
    ADMIN,
    USER,
    GUEST,
    NONE;

    @Override
    public String toString() {
        switch (this) {
            case ADMIN:
                return "Administrator";
            case USER:
                return "User";
            case GUEST:
                return "Guest";
            case NONE:
                return "None";
            default:
                return "Unknown";
        }
    }

    public static UserLevel translate(String level) {
        if (level.equals("Administrator")) {
            return UserLevel.ADMIN;
        } else if (level.equals("User")) {
            return UserLevel.USER;
        } else if (level.equals("Guest")) {
            return UserLevel.GUEST;
        } else if (level.equals("None")) {
            return UserLevel.NONE;
        } else {
            return null;
        }
    }
}
