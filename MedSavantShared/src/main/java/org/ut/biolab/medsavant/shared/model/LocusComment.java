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
import java.util.Date;
import java.util.Set;

/**
 *
 * @author jim
 */
public class LocusComment implements Serializable {    
    public static final OntologyType ONTOLOGY_TYPE = OntologyType.HPO;
        
    private final Integer commentId; //ignored by server
    private final String user; //Ignored by server
    private final Boolean isApproved; //Only used by server if user has permission
    private final Boolean isIncluded;//Only used by server if user has permission
    private final Boolean isPendingReview;//Only used by server if user has permission
    private final Boolean isDeleted;//Only used by server if user has permission, or if user owns comment
    private final Date creationDate;//ignored by server
    private final Date modificationDate; //ignored by server.
    private final String commentText;       
    private final OntologyTerm ontologyTerm;
    
    public LocusComment(Boolean isApproved, Boolean isIncluded, Boolean isPendingReview, Boolean isDeleted, String comment, OntologyTerm ontologyTerm){
        this(null, null, isApproved, isIncluded, isPendingReview, isDeleted, null, null, comment, ontologyTerm);
    }

    public LocusComment(Integer commentId, String user, Boolean isApproved, Boolean isIncluded, Boolean isPendingReview, Boolean isDeleted, Date creationDate, Date modificationDate, String commentText, OntologyTerm ontologyTerm) {
        this.commentId = commentId;
        this.user = user;
        this.isApproved = isApproved;
        this.isIncluded = isIncluded;
        this.isPendingReview = isPendingReview;
        this.isDeleted = isDeleted;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
        this.commentText = commentText;
        this.ontologyTerm = ontologyTerm;                
    }

    public OntologyTerm getOntologyTerm() {
        return ontologyTerm;
    }

    public String getCommentText() {
        return commentText;
    }

    public String getUser() {
        return user;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public boolean isIncluded() {
        return isIncluded;
    }

    public boolean isPendingReview() {
        return isPendingReview;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public Date getCreationDate() {
        return creationDate;
    }
    
    public Date getModificationDate(){
        return this.modificationDate;
    }
    
}
