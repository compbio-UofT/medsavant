/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ut.biolab.medsavant.shared.format;

public class UserRole {

    private final Integer roleId;
    private final String roleName;
    private final String database;
    private final String roleDescription;

    public UserRole(Integer roleId, String roleName, String roleDescription, String database) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.database = database;
        this.roleDescription = roleDescription;
    }

    public UserRole(String roleName, String description) {
        this(null, roleName, description, null);        
    }

    public int getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public String getDatabase() {
        return database;
    }

    @Override
    public String toString() {
        return roleName;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.roleId;
        hash = 53 * hash + (this.database != null ? this.database.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserRole other = (UserRole) obj;
        if (this.roleId != other.roleId) {
            return false;
        }
        if ((this.database == null) ? (other.database != null) : !this.database.equals(other.database)) {
            return false;
        }
        return true;
    }

}
