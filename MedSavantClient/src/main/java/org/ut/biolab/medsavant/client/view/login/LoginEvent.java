/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.client.view.login;

/**
 *
 * @author mfiume
 */
public class LoginEvent {

    public enum Type { LOGGED_IN, LOGGED_OUT, LOGIN_FAILED, LOGIN_CANCELLED };

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
