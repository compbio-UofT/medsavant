/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.annotation;

/**
 *
 * @author jim
 */
public interface MedSavantFieldValue<T> {
    public MedSavantField getField();
    public T getValue();
}
