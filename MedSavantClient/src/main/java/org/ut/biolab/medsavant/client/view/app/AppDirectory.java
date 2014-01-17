/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.client.view.app;

import org.ut.biolab.medsavant.client.view.app.task.TaskManagerApp;

/**
 *
 * @author mfiume
 */
public class AppDirectory {

    private static TaskManagerApp taskManager;
    
    static void registerTaskManager(TaskManagerApp tm) {
        taskManager = tm;
    }

    public static TaskManagerApp getTaskManager() {
        return taskManager;
    }
    
    
}
