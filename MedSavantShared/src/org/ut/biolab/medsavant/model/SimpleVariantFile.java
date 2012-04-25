/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.model;

import java.io.Serializable;

/**
 *
 * @author Andrew
 */
public class SimpleVariantFile implements Serializable {
    
    private int uploadId;
    private int fileId;
    private String name;
    private String date;
    private String user;
    
    public SimpleVariantFile(int uploadId, int fileId, String name, String date, String user){
        this.uploadId = uploadId;
        this.fileId = fileId;
        this.name = name;
        this.date = date;
        this.user = user;
    }

    public int getUploadId() {
        return uploadId;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public int getFileId() {
        return fileId;
    }
    
    public String getUser() {
        return user;
    }

    public String toString() {
        return name;
    }
    
}
