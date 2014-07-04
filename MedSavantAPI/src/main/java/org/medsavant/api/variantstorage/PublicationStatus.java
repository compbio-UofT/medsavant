/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.variantstorage;

/**
 *
 * @author jim
 */
public interface PublicationStatus {
    public void setPending();
    public void setPublished();
    public void setDeleted();
    public boolean isPending();
    public boolean isPublished();
    public boolean isDeleted();
}
