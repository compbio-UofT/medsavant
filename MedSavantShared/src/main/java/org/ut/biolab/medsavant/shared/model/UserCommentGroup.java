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
package org.ut.biolab.medsavant.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;

/**
 *
 * @author jim
 */
public class UserCommentGroup implements Serializable {    
    
    private final int projectId;
    private final int referenceId;
    private final String chrom;
    private final long startPosition;
    private final long endPosition;
    private final String ref;
    private final String alt;
    
    private final int userCommentGroupId;
    private final Set<OntologyTerm> ontologyTermsCovered;
    private List<UserComment> comments;

    public UserCommentGroup(int groupId, int projectId, int referenceId, String chrom, long startPosition, long endPosition, String ref, String alt, Date modification, List<UserComment> comments) {
        this.userCommentGroupId = groupId;
        this.projectId = projectId;
        this.referenceId = referenceId;
        this.chrom = chrom;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.ref = ref;
        this.alt = alt;
        this.ontologyTermsCovered = new HashSet<OntologyTerm>();
                
        if(comments != null){            
            for(UserComment lc : comments){
                this.ontologyTermsCovered.add(lc.getOntologyTerm());
            }
            this.comments = comments;
        }else{
            this.comments = new ArrayList<UserComment>(0);
        }
    }

    /**
     *
     * @return A set of all the ontology terms that are referenced by the
     * comments.
     */
    public Set<OntologyTerm> getOntologyTermsCovered() {
        return ontologyTermsCovered;
    }
    
    public int getUserCommentGroupId(){
        return this.userCommentGroupId;
    }
    
    public int getNumComments() {
        return comments.size();
    }
    
    public Iterator<UserComment> iterator() {
        return new Iterator<UserComment>() {
            private final Iterator<UserComment> it = comments.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public UserComment next() {
                return it.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Removal not supported");
            }
        };
    }        
       
}
