/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package org.ut.biolab.medsavant.client.view.splash;

import org.ut.biolab.medsavant.client.util.CryptoUtils;

import java.io.Serializable;

/**
 * A class which stores MedSavant Server connection information
 * @author mfiume
 */
public class MedSavantServerInfo implements Serializable, Comparable<MedSavantServerInfo> {

    private String host;
    private int port;
    private String database;
    private String nickname;
    private String username;
    private String encodedPassword;
    private boolean rememberPassword;
    private boolean isEditable = true;

    public MedSavantServerInfo() {
        this("",0,"","Unnamed Server");
    }

    public MedSavantServerInfo(String host, int port, String database, String nickname) {
        System.out.println("Creating new server.!!!");
        this.host = host;
        this.port = port;
        this.database = database;
        this.nickname = nickname;
        this.username = "";
        this.encodedPassword = "";
        this.rememberPassword = false;
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

    public String getPassword() {
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
        return this.getNickname().compareTo(o.getNickname());
    }
}
