package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.model.GeneralLog;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

/**
 * Adapter class for mapping Solr documents to ServerLog objects.
 */
public class SearcheableGeneralLog {

    private GeneralLog log;
    private String user;
    private String event;
    private String description;
    private Timestamp timestamp;

    private UUID uuid;

    public SearcheableGeneralLog(GeneralLog log) {
        this.log = log;
        this.addUUID();
    }

    public SearcheableGeneralLog() {};

    private void addUUID() {
        this.uuid = UUID.randomUUID();
    }

    @Field("user")
    public void setUser(String user) {
        this.user = user;
    }

    @Field("event")
    public void setEvent(String event) {
        this.event = event;
    }

    @Field("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @Field("timestamp")
    public void setTimestamp(Date timestamp) {
        this.timestamp = new Timestamp(timestamp.getTime());
    }

    public GeneralLog getLog() {
        this.log = new GeneralLog(user, event, description, timestamp);
        return log;
    }

    public String getUser() {
        return user;
    }

    public String getEvent() {
        return event;
    }

    public String getDescription() {
        return description;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public UUID getUuid() {
        return uuid;
    }
}
