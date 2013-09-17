package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;

import java.util.UUID;

/**
 *  Helper class for setting the UUID of entities.
 */
public abstract class SearcheableMedsavantEntity {

    @Field("uuid")
    public abstract void setUUID(String uuid);

    public abstract UUID getUUID();
}
