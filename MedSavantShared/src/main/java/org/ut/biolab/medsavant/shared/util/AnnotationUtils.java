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

package org.ut.biolab.medsavant.shared.util;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Annotation;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.AnnotationManagerAdapter;

/**
 * Contains methods to help with annotations.
 * @author jim
 */
public class AnnotationUtils {                
    public Map<String, Set<CustomField>> getAnnotationFieldsByTag(String sessionId, AnnotationManagerAdapter ama) throws SQLException, RemoteException,  SessionExpiredException{
        //
        //tag -> AnnotationFormat -> tag.
        Map<String, Set<CustomField>> amap = new HashMap<String, Set<CustomField>>();
        //read in all annotations
        Annotation[] annotations = ama.getAnnotations(sessionId);
        for(Annotation annotation : annotations){
            AnnotationFormat af = ama.getAnnotationFormat(sessionId, annotation.getID());
            CustomField[] customFields = af.getCustomFields();
            for(CustomField customField : customFields){                
                Set<String> tags = customField.getTags();
                for(String tag : tags){
                    Set<CustomField> cs = amap.get(tag);
                    if(cs == null){
                        cs = new HashSet<CustomField>();                        
                    }
                    cs.add(customField);
                    amap.put(tag, cs);                    
                }
            }            
        }
        return amap;
    }
}