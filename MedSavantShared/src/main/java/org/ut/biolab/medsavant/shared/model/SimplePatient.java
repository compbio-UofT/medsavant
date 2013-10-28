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
import java.util.List;

/**
 *
 * @author Andrew
 */
public class SimplePatient implements Serializable {

    private int id;
    private String hid;
    private List<String> dnaIds;

    public SimplePatient(int id, String hid, List<String> dnaIds){
        this.id = id;
        this.hid = hid;
        this.dnaIds = dnaIds;
    }

    public int getId(){
        return this.id;
    }

    public String getHospitalId(){
        return this.hid;
    }

    public List<String> getDnaIds(){
        return this.dnaIds;
    }

    @Override
    public String toString(){
        return hid;
    }
}
