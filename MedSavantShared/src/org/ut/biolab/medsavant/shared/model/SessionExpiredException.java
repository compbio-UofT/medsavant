package org.ut.biolab.medsavant.shared.model;

import java.io.Serializable;

/**
 *
 * @author mfiume
 */
public class SessionExpiredException extends Exception implements Serializable {

    public SessionExpiredException() {
        super("Session expired");
    }

}
