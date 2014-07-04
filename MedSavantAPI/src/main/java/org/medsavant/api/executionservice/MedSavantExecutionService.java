/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.executionservice;

import java.util.List;
import java.util.concurrent.Future;
import org.medsavant.api.common.MedSavantServerComponent;
import org.medsavant.api.common.impl.MedSavantServerJob;

/**
 *
 * @author jim
 */
public interface MedSavantExecutionService extends MedSavantServerComponent{

    int getMaxQueuedLongJobs();

    int getMaxRunningLongJobs();

    Void runJobInCurrentThread(MedSavantServerJob msj) throws Exception;

    Future submitLongJob(MedSavantServerJob msj);

    List<Future<Void>> submitLongJobs(List<MedSavantServerJob> msjs) throws InterruptedException;

    Future submitShortJob(MedSavantServerJob msj);

    List<Future<Void>> submitShortJobs(List<MedSavantServerJob> msjs) throws InterruptedException;
    
    public void shutdown() throws InterruptedException;
}
