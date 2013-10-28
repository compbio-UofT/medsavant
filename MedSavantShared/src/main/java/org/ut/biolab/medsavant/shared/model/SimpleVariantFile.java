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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.shared.model;

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
