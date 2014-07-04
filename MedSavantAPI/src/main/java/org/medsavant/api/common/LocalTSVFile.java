/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.common;

import java.util.Collection;

/**
 *
 * @author jim
 */
public interface LocalTSVFile extends Iterable<TSVLineWriter>{
    public LocalTSVFile createTemporaryFile();    
    public void append(Collection<? extends TSVLineWriter> lines);           
    public Collection<TSVLineWriter> getLines(int start, int end);
    public boolean delete();    
}
