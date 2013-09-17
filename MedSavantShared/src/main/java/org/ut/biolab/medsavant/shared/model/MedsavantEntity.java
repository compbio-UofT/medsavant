package org.ut.biolab.medsavant.shared.model;

import java.util.UUID;

/**
 * Base entity, contains data common to all entities.
 */
public class MedsavantEntity {

    private UUID uuid;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
