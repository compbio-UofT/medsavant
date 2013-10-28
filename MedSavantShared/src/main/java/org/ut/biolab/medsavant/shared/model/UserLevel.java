/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
}
