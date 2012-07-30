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

package org.ut.biolab.medsavant.login;

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
