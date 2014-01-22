/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.app;

import org.ut.biolab.medsavant.client.view.app.builtin.task.TaskManagerApp;

/**
 *
 * @author mfiume
 */
public class AppDirectory {

    private static TaskManagerApp taskManager;
    private static AccountManagerApp accountManager;

    public static TaskManagerApp getTaskManager() {
        if (taskManager == null) {
            taskManager = new TaskManagerApp();
        }
        return taskManager;
    }

    public static AccountManagerApp getAccountManager() {
        if (accountManager == null) {
            accountManager = new AccountManagerApp();
        }
        return accountManager;
    }

}
