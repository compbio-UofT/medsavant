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

package org.ut.biolab.medsavant.client.view.util.form;

import java.util.ArrayList;

/**
 *
 * @author mfiume
 */
public class NiceFormFieldGroup {
    
   private final ArrayList<NiceFormField> fields;
    private boolean displayHeading;
    private String name;

    public NiceFormFieldGroup(String name, boolean displayHeading) {
        this.name = name;
        this.displayHeading = displayHeading;
        this.fields = new ArrayList<NiceFormField>();
    }

    public void addField(NiceFormField f) {
        this.fields.add(f);
    }

    public ArrayList<NiceFormField> getFields() {
        return fields;
    }

    public boolean displayHeading() {
        return displayHeading;
    }

    public String getName() {
        return name;
    }
    
}
