/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.shared.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 *
 * @author Andrew
 */
public class GeneralLog extends MedsavantEntity implements Serializable {

    private String user;
    private String event;
    private String description;
    private Timestamp timestamp;

    public GeneralLog(String event, String description, Timestamp timestamp) {
        this(null, event, description, timestamp);
    }

    public GeneralLog(String user, String event, String description, Timestamp timestamp) {
        this.user = user;
        this.event = event;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public String getEvent() {
        return event;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getUser() {
        return user;
    }

}
