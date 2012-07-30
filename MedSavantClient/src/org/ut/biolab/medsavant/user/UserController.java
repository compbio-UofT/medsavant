/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.user;

import java.rmi.RemoteException;
import java.sql.SQLException;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.model.UserLevel;
import org.ut.biolab.medsavant.util.Controller;


/**
 *
 * @author mfiume
 */
public class UserController extends Controller<UserEvent> {

    private static UserController instance;

    public static UserController getInstance() {
        if (instance == null) {
            instance = new UserController();
        }
        return instance;
    }

    public void addUser(String name, char[] pass, UserLevel level) throws SQLException, RemoteException {
        MedSavantClient.UserManager.addUser(LoginController.sessionId, name, pass, level);
        fireEvent(new UserEvent(UserEvent.Type.ADDED, name));
    }

    public void removeUser(String name) throws SQLException, RemoteException {
        MedSavantClient.UserManager.removeUser(LoginController.sessionId, name);
        fireEvent(new UserEvent(UserEvent.Type.REMOVED, name));
    }

    public String[] getUserNames() throws SQLException, RemoteException {
        return MedSavantClient.UserManager.getUserNames(LoginController.sessionId);
    }

    public void getUserLevel() {

    }
}
