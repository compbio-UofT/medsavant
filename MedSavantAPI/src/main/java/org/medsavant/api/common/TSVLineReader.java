/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.common;

/**
 *
 * @author jim
 */
public interface TSVLineReader<T extends TSVLineReader>{
    public T fromTSV(String[] t);
}
