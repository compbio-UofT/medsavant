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
    }

    public SearcheableGeneralLog() {};

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
        return log.getUser();
    }

    public String getEvent() {
        return log.getEvent();
    }

    public String getDescription() {
        return log.getDescription();
    }

    public Timestamp getTimestamp() {
        return log.getTimestamp();
    }
}
