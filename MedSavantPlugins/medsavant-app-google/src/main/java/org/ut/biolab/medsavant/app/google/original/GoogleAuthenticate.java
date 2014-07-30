/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 *
 * All rights reserved. No warranty, explicit or implicit, provided. THE
 * SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE FOR
 * ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.app.google.original;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.genomics.Genomics;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Properties;
import javax.annotation.Nullable;
import org.ut.biolab.medsavant.app.google.MedSavantPromptReceiver;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;

/**
 *
 * @author mfiume
 */
public class GoogleAuthenticate {

    //public static String clientSecretsFilename = "/Users/mfiume/Desktop/client_secrets.json";
    private static final String APPLICATION_NAME = "Google-GenomicsSample/1.0";
    private static final String TOKEN_PROPERTIES_FILENAME = "genomics.token.properties";

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String rootUrl = "https://www.googleapis.com/";

    private static GoogleClientSecrets clientSecrets = null;
    private static NetHttpTransport httpTransport;

    private static final String DEVSTORAGE_SCOPE
            = "https://www.googleapis.com/auth/devstorage.read_write";
    private static final String GENOMICS_SCOPE = "https://www.googleapis.com/auth/genomics";
    private static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
    private static GoogleAuthorizationCodeFlow flow = null;

    private static InputStream getClientSecrets() {
        return GoogleAuthenticate.class.getResourceAsStream("/oauth/client_secrets.json");
    }

    private static GoogleClientSecrets loadClientSecrets() {
        try {

            InputStream inputStream = getClientSecrets();
            return GoogleClientSecrets.load(JSON_FACTORY,
                    new InputStreamReader(inputStream));
        } catch (Exception e) {
            System.err.println("Could not load client_secrets.json");
        }

        return null;
    }

    private static Credential authenticate() throws IOException, GeneralSecurityException {
        // Attempt to load client secrets

        clientSecrets = loadClientSecrets();
        if (clientSecrets == null) {
            throw new IOException("Error loading client_secrets.json");
        }
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        if (httpTransport == null) {
            throw new IOException("Error creating trusted transport");
        }

        // Attempt to Load existing Refresh Token
        @Nullable
        String storedRefreshToken = loadRefreshToken();

        // Check to see if the an existing refresh token was loaded.
        // If so, create a credential and call refreshToken() to get a new
        // access token.
        Credential credential;
        if (storedRefreshToken != null) {
            // Request a new Access token using the refresh token.
            credential = createCredentialWithRefreshToken(
                    new TokenResponse().setRefreshToken(storedRefreshToken));
            credential.refreshToken();
        } else {
            // If there is no refresh token (or token.properties file), start the OAuth
            // authorization flow.
            // Exchange the auth code for an access token and refesh token
            
            credential = exchangeCode();

            // Store the refresh token for future use.
            storeRefreshToken(credential.getRefreshToken());
        }
        return credential;
    }

    private static GoogleCredential createCredentialWithRefreshToken(TokenResponse tokenResponse) {
        return new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(clientSecrets)
                .build()
                .setFromTokenResponse(tokenResponse);
    }

    private static Credential exchangeCode() throws IOException {
        DialogUtils.displayMessage("<html>In order to use this app, you need"
                    + "to grant<br/>access using your Google account.<br/><br/>"
                    + "You'll now be directed to Google to do so.</html>");
        GoogleAuthorizationCodeFlow flow = getFlow();
        return new AuthorizationCodeInstalledApp(flow, new MedSavantPromptReceiver())
                .authorize(System.getProperty("user.name"));
    }

    private static GoogleAuthorizationCodeFlow getFlow() {
        if (flow == null) {
            flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport,
                    JSON_FACTORY,
                    clientSecrets,
                    Arrays.asList(DEVSTORAGE_SCOPE, GENOMICS_SCOPE, EMAIL_SCOPE))
                    .setAccessType("offline")
                    .setApprovalPrompt("force")
                    .build();
        }
        return flow;
    }

    private static void storeRefreshToken(String refreshToken) {
        Properties properties = new Properties();
        properties.setProperty("refreshtoken", refreshToken);

        
        File gp = new File(getGoogleCloudDotDirectory().getPath());
        if(!gp.exists()){
            if(!gp.mkdirs()){
                System.err.println("Couldn't create directory "+gp.getPath());
            }
        }
        
        File tokenPropPath = new File(getGoogleCloudDotDirectory().getPath(), TOKEN_PROPERTIES_FILENAME);
        
        OutputStream output = null;

        try {
            output = new FileOutputStream(tokenPropPath);
            // save properties to project root folder
            properties.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    System.err.println("Failed storing token properties to file " + tokenPropPath.getAbsolutePath());
                    e.printStackTrace();
                }
            }

        }
    }

    private static String loadRefreshToken() {
        Properties properties = new Properties();
        try {
            File inputFile = new File(getGoogleCloudDotDirectory(), TOKEN_PROPERTIES_FILENAME);
            properties.load(new FileInputStream(inputFile));
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return (String) properties.get("refreshtoken");
    }

    private static File getGoogleCloudDotDirectory() {
        return new File(System.getProperty("user.home"), ".google_cloud");
    }

    public static Genomics buildService() throws IOException, GeneralSecurityException {

        final Credential credential = authenticate();

        return new Genomics.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .setRootUrl(rootUrl)
                .setHttpRequestInitializer(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest httpRequest) throws IOException {
                        credential.initialize(httpRequest);
                        httpRequest.setReadTimeout(60000); // 60 seconds
                    }
                }).build();
    }
}
