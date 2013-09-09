package org.ut.biolab.medsavant.shared.model;

/**
 *  Models users.
 */
public class User {

    private String name;
    private String password;
    private UserLevel level;

    public User() {};

    public User(String name, UserLevel leve) {
        this.name = name;
        this.level = leve;
    }

    public User(String name, String password, UserLevel level) {
        this.name = name;
        this.password = password;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserLevel getLevel() {
        return level;
    }

    public void setLevel(UserLevel level) {
        this.level = level;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
