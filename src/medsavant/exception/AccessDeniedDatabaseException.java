/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package medsavant.exception;

/**
 *
 * @author mfiume
 */
public class AccessDeniedDatabaseException extends Exception {

    public String getUsername() {
        return username;
    }

    private final String username;

    public AccessDeniedDatabaseException(String username) {
        this.username = username;
    }

}
