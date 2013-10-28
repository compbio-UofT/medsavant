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
package org.ut.biolab.medsavant.client.login;

/**
 *
 * @author mfiume
 */
public class LoginEvent {

    public enum Type { LOGGED_IN, LOGGED_OUT, LOGIN_FAILED };

    private final Type type;
    private final Exception exception;

    public LoginEvent(Type type) {
        this.type = type;
        this.exception = null;
    }

    public LoginEvent(Exception ex) {
        this.type = Type.LOGIN_FAILED;
        this.exception = ex;
    }

    public Exception getException() {
        return exception;
    }

    public Type getType() {
        return type;
    }
}
