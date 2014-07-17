/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.app.google;

import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import java.io.IOException;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;

/**
 *
 * @author jim
 */
public class MedSavantPromptReceiver implements VerificationCodeReceiver{           
    @Override
    public String getRedirectUri() throws IOException {
        return GoogleOAuthConstants.OOB_REDIRECT_URI;

    }

    @Override
    public String waitForCode() throws IOException {
        return DialogUtils.displayInputMessage("Enter Authentication Token", "Please enter the authentication token from your web browser.", "");
    }

    @Override
    public void stop() throws IOException {
        
    }
    
}
