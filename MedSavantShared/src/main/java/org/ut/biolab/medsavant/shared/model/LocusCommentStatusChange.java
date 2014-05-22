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

import java.util.Date;

/**
 *
 * @author jim
 */
public class LocusCommentStatusChange extends UserComment{
    private final UserComment parentComment;
           
    public LocusCommentStatusChange(UserComment parentComment, Boolean approveParent, Boolean includeParent, Boolean markParentPending, Boolean markParentDeleted, String comment) {                       
        //super(approveParent, includeParent, markParentPending, markParentDeleted, comment, parentComment.getOntologyTerm());
        
        //Status change comment tracks the original status of the parent comment.  
        super(parentComment.isApproved(), parentComment.isIncluded(), parentComment.isPendingReview(), parentComment.isDeleted(), comment, parentComment.getOntologyTerm());

        //Parent comment contains the new status.
        this.parentComment = new UserComment(approveParent, includeParent, markParentPending, markParentDeleted, parentComment.getCommentText(), parentComment.getOntologyTerm());                
    }
    
    public boolean statusChanged() {
        return parentComment != null
                && !(parentComment.isApproved() == isApproved()
                && parentComment.isDeleted() == isDeleted()
                && parentComment.isIncluded() == isIncluded()
                && parentComment.isPendingReview() == isPendingReview());
    }

    public UserComment getOriginalComment(){
        return parentComment;
    }      
    
    public Integer getOriginalCommentID(){
        return parentComment.commentId;
    }
       
}
