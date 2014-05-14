/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.client.view.login;

import org.ut.biolab.medsavant.client.util.CryptoUtils;

import java.io.Serializable;
import java.util.UUID;

/**
 * A class which stores MedSavant Server connection information
 *
 * @author mfiume
 */
public final class MedSavantServerInfo implements Serializable, Comparable<MedSavantServerInfo> {

    // create an identifier for this server, to allow comparisons
    // across serialized / deserialized versions
    private String uniqueID;
    
    // server host name
    private String host;
    
    // server port
    private int port;
    
    // database on server
    private String database;
    
    // nice name to display
    private String nickname;
    
    // user name and encoded password
    private String username;
    private String encodedPassword;
    
    // whether to remember the password when saved
    private boolean rememberPassword;
    
    // whether this server's info should be editable in the UI
    // 04-01-14: not used, but possible to use in the future
    // to support public servers
    private boolean isEditable = true;

    public MedSavantServerInfo() {
        this("", 0, "", "Unnamed Server");
    }

    public MedSavantServerInfo(String host, int port, String database, String nickname) {
        this(host, port, database, nickname, null);
    }
    
    private MedSavantServerInfo(String host, int port, String database, String nickname, String uniqueID) {
        
        this.host = host;
        this.port = port;
        this.database = database;
        this.nickname = nickname;
        this.username = "";
        this.encodedPassword = "";
        this.rememberPassword = false;
        
         if (uniqueID == null) {
            this.uniqueID = UUID.randomUUID().toString();
        } else {
            this.uniqueID = uniqueID;
        }
    }

    public MedSavantServerInfo(MedSavantServerInfo server) {
        this(server.host, server.port, server.database, server.nickname, server.uniqueID);
        this.setEditable(server.isEditable);
        this.setUsername(server.username);
        this.setPassword(CryptoUtils.decrypt(server.encodedPassword));
        this.setRememberPassword(server.rememberPassword);
    }

    public String getUniqueID() {
        return uniqueID;
    }

    private void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        System.out.println("SETTING UUID " + this.getUniqueID() + " NICKNAME TO " + this.getNickname());
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.encodedPassword = CryptoUtils.encrypt(password);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getNickname() {
        return nickname;
    }

    public String getUsername() {
        return username;
    }

    protected String getPassword() {
        return CryptoUtils.decrypt(encodedPassword);
    }

    public boolean isRememberPassword() {
        return rememberPassword;
    }

    public void setRememberPassword(boolean rememberPassword) {
        this.rememberPassword = rememberPassword;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    @Override
    public String toString() {
        return getNickname();
    }

    @Override
    public int compareTo(MedSavantServerInfo o) {
        return this.getUniqueID().compareTo(o.getUniqueID());
    }
}
