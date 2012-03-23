/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.controller;

import java.io.Serializable;
import java.rmi.RemoteException;
import org.ut.biolab.medsavant.client.api.ClientCallbackAdapter;
import org.ut.biolab.medsavant.db.util.shared.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.view.util.DialogUtils;

/**
 *
 * @author Andrew
 */
public class CallbackController extends MedSavantServerUnicastRemoteObject implements ClientCallbackAdapter, Serializable {

    private static CallbackController instance;

    public static CallbackController getInstance() throws RemoteException {
        if(instance == null) {
            instance = new CallbackController();
        }
        return instance;
    }

    public CallbackController() throws RemoteException {super();}

    @Override
    public void sessionTerminated(final String message) throws RemoteException {

        //do in new thread to prevent server from blocking.
        Thread t = new Thread(){

            @Override
            public void run() {

                String fullmessage = "Your session was ended by the server.\n\n";

                if (message != null) {
                    fullmessage += message;
                }

                fullmessage += "\n\nMedSavant will now exit.";

                DialogUtils.displayMessage("Session ended", fullmessage);
                //LoginController.logout(); // can't logout because session no longer active. just exit!

                System.exit(0);
            }
        };
        t.start();
    }

}
