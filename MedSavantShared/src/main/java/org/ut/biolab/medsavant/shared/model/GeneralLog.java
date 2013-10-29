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
package org.ut.biolab.medsavant.shared.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 *
 * @author Andrew
 */
public class GeneralLog implements Serializable {

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
