package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.model.User;
import org.ut.biolab.medsavant.shared.model.UserLevel;

/**
 * Adapter class for mapping users.
 */
public class SearcheableUser {

    private User user;

    public SearcheableUser() {
        this.user = new User();
    }

    public SearcheableUser(User user) {
        this.user = user;
    }

    @Field("name")
    public void setName(String name) {
        user.setName(name);
    }

    @Field("password")
    public void setPassword(String password) {
        user.setPassword(password);
    }

    @Field("level")
    public void setLevel(String level) {
        user.setLevel(UserLevel.translate(level));
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return user.getName();
    }

    public UserLevel getLevel() {
        return user.getLevel();
    }

    public String getPassword() {
        return user.getPassword();
    }
}