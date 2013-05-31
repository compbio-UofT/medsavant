/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.user;

/**
 *
 * @author tarkvara
 */
public class UserEvent {
    public enum Type { ADDED, REMOVED };

    private final Type type;
    private final String name;

    public UserEvent(Type t, String n) {
        type = t;
        name = n;
    }
}
