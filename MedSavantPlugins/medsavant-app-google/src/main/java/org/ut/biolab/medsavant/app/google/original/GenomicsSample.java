/*
 Copyright 2014 Google Inc. All rights reserved.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.ut.biolab.medsavant.app.google.original;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.java6.auth.oauth2.GooglePromptReceiver;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.genomics.Genomics;
import com.google.api.services.genomics.GenomicsRequest;
import com.google.api.services.genomics.model.ImportReadsetsRequest;
import com.google.api.services.genomics.model.Job;
import com.google.api.services.genomics.model.Read;
import com.google.api.services.genomics.model.SearchReadsRequest;
import com.google.api.services.genomics.model.SearchReadsResponse;
import com.google.api.services.genomics.model.SearchReadsetsRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.IllegalArgumentException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Genomics Java client sample application.
 */
public class GenomicsSample {

    private static GoogleClientSecrets clientSecrets = null;
    private static final String APPLICATION_NAME = "Google-GenomicsSample/1.0";
    private static NetHttpTransport httpTransport;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static CommandLine cmdLine;
    private static final String DEVSTORAGE_SCOPE
            = "https://www.googleapis.com/auth/devstorage.read_write";
    private static final String GENOMICS_SCOPE = "https://www.googleapis.com/auth/genomics";
    private static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
    private static final String TOKEN_PROPERTIES_FILENAME = "genomics.token.properties";
    private static GoogleAuthorizationCodeFlow flow = null;

    private static InputStream getClientSecrets() {
        return ClassLoader.class.getResourceAsStream("/client_secrets.json");
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

    public static GoogleCredential createCredentialWithRefreshToken(TokenResponse tokenResponse) {
        return new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(clientSecrets)
                .build()
                .setFromTokenResponse(tokenResponse);
    }

    private static Genomics buildService(final Credential credential) {
        return new Genomics.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .setRootUrl(cmdLine.rootUrl)
                .setHttpRequestInitializer(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest httpRequest) throws IOException {
                        credential.initialize(httpRequest);
                        httpRequest.setReadTimeout(60000); // 60 seconds
                    }
                }).build();
    }

    private static File getGoogleCloudDotDirectory() {
        return new File(System.getProperty("user.home"), ".google_cloud");
    }

    private static void createDotGoogleCloudDirectory() {
        File f = getGoogleCloudDotDirectory();
        if (!f.exists()) {
            f.mkdirs();
        }
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

    private static Credential exchangeCode() throws IOException {
        GoogleAuthorizationCodeFlow flow = getFlow();
        return new AuthorizationCodeInstalledApp(flow, new GooglePromptReceiver())
                .authorize(System.getProperty("user.name"));
    }

    private static void storeRefreshToken(String refreshToken) {
        Properties properties = new Properties();
        properties.setProperty("refreshtoken", refreshToken);

        String path = getGoogleCloudDotDirectory().getPath() + "/" + TOKEN_PROPERTIES_FILENAME;

        OutputStream output = null;

        try {

            output = new FileOutputStream(new File(getGoogleCloudDotDirectory().getPath(), "/" + TOKEN_PROPERTIES_FILENAME));

            // save properties to project root folder
            properties.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    System.err.println("Failed storing token properties to file " + path);
                    e.printStackTrace();
                }
            }

        }
    }

    private static String loadRefreshToken() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(getGoogleCloudDotDirectory().getPath()
                    + "/" + TOKEN_PROPERTIES_FILENAME));
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return (String) properties.get("refreshtoken");
    }

    private static boolean deleteRefreshToken() {
        String filename = getGoogleCloudDotDirectory().getPath() + "/" + TOKEN_PROPERTIES_FILENAME;
        File f = new File(filename);
        return f.exists() && f.delete();
    }

    private static Credential authenticate() throws IOException {
        // Attempt to load client secrets
        clientSecrets = loadClientSecrets();
        if (clientSecrets == null) {
            System.exit(1);
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

    public static void main(String[] args) {
        try {
            createDotGoogleCloudDirectory();

            cmdLine = new CommandLine(args);

            // Show help
            assertOrDie(!cmdLine.showHelp(), "");

            // Make sure request_type is specified
            assertOrDie(cmdLine.remainingArgs.size() == 1,
                    "Must specify a request_type\n");

            httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            // Route to appropriate request method
            String requestType = cmdLine.remainingArgs.get(0);
            if (requestType.equals("help")) {
                cmdLine.printHelp("", System.err);
                return;
            } else if (requestType.equals("auth")) {
                deleteRefreshToken();
                authenticate();
                return;
            } else {
                Genomics genomics = buildService(authenticate());
                try {
                    executeAndPrint(getRequest(cmdLine, genomics, requestType));
                } catch (IllegalArgumentException e) {
                    cmdLine.printHelp(e.getMessage() + "\n", System.err);
                    System.exit(0);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static GenomicsRequest getRequest(CommandLine cmdLine, Genomics genomics, String requestType)
            throws IOException, IllegalArgumentException {
        if (requestType.equals("importreadsets")) {
            System.out.println("Import read sets");
            return importReadsets(cmdLine, genomics);
        } else if (requestType.equals("searchreadsets")) {
            System.out.println("Search read sets");
            return searchReadsets(cmdLine, genomics);
        } else if (requestType.equals("getreadset")) {
            System.out.println("Get read sets");
            return getReadset(cmdLine, genomics);
        } else if (requestType.equals("getjob")) {
            System.out.println("Get job");
            return getJob(cmdLine, genomics);
        } else if (requestType.equals("searchreads")) {
            System.out.println("Search reads");
            return searchReads(cmdLine, genomics);
        } else {
            List<String> validRequestTypes = Arrays.asList("auth", "help", "importreadsets",
                    "searchreadsets", "getreadset", "getjob", "searchreads");
            throw new IllegalArgumentException("request_type must be one of: " + validRequestTypes + "\n");
        }
    }

    private static void assertOrDie(boolean condition, String headline) throws IOException {
        if (!condition) {
            cmdLine.printHelp(headline, System.err);
            System.exit(0);
        }
    }

    private static void assertOrThrow(boolean condition, String headline) throws IllegalArgumentException {
        if (!condition) {
            throw new IllegalArgumentException(headline);
        }
    }

    private static void executeAndPrint(GenomicsRequest<?> req) throws IOException {
        if (!cmdLine.fields.isEmpty()) {
            req.setFields(cmdLine.fields);
        }
        Object o = req.execute();
        System.out.println("class: " + o.getClass().getCanonicalName());
        System.out.println("result: " + o);

        if (o instanceof SearchReadsResponse) {
            getRecords((SearchReadsResponse) o);
        }
    }

    static Genomics.Readsets.GenomicsImport importReadsets(CommandLine cmdLine, Genomics genomics)
            throws IOException, IllegalArgumentException {
        assertOrThrow(!cmdLine.datasetId.isEmpty(), "Must specify a dataset_id\n");
        assertOrThrow(cmdLine.bamFiles.size() > 0, "Must specify at least one BAM file\n");

        ImportReadsetsRequest content = new ImportReadsetsRequest()
                .setDatasetId(cmdLine.datasetId)
                .setSourceUris(cmdLine.bamFiles);
        return genomics.readsets().genomicsImport(content);
    }

    static Genomics.Readsets.Search searchReadsets(CommandLine cmdLine, Genomics genomics)
            throws IOException, IllegalArgumentException {
        assertOrThrow(!cmdLine.datasetIds.isEmpty(), "Currently, dataset_ids is required. "
                + "This requirement will go away in the future.");

        SearchReadsetsRequest content = new SearchReadsetsRequest().setDatasetIds(cmdLine.datasetIds);
        return genomics.readsets().search(content);
    }

    static Genomics.Readsets.Get getReadset(CommandLine cmdLine, Genomics genomics)
            throws IOException, IllegalArgumentException {
        assertOrThrow(!cmdLine.readsetId.isEmpty(), "Must specify a readset_id");
        return genomics.readsets().get(cmdLine.readsetId);
    }

    static Genomics.Jobs.Get getJob(CommandLine cmdLine, Genomics genomics)
            throws IOException, IllegalArgumentException {
        assertOrThrow(!cmdLine.jobId.isEmpty(), "Must specify a job_id");
        return genomics.jobs().get(cmdLine.jobId);
    }

    static Genomics.Reads.Search searchReads(CommandLine cmdLine, Genomics genomics)
            throws IOException, IllegalArgumentException {
        SearchReadsRequest content = new SearchReadsRequest()
                .setReadsetIds(cmdLine.readsetIds)
                .setDatasetIds(cmdLine.datasetIds)
                .setPageToken(cmdLine.pageToken);

        // Range parameters must all be specified or none.
        if (!cmdLine.sequenceName.isEmpty() || cmdLine.sequenceStart > 0 || cmdLine.sequenceEnd > 0) {
            assertOrThrow(!cmdLine.sequenceName.isEmpty(), "Must specify a sequence_name");
            assertOrThrow(cmdLine.sequenceStart > 0, "sequence_start must be greater than 0");
            // getting this far implies target_start is greater than 0
            assertOrThrow(cmdLine.sequenceEnd >= cmdLine.sequenceStart,
                    "sequence_end must be greater than sequence_start");

            content
                    .setSequenceName(cmdLine.sequenceName)
                    .setSequenceStart(BigInteger.valueOf(cmdLine.sequenceStart.intValue()))
                    .setSequenceEnd(BigInteger.valueOf(cmdLine.sequenceEnd.intValue()));
        }
        return genomics.reads().search(content);
    }

    private static void getRecords(SearchReadsResponse searchReadsResponse) {
        List<Read> reads = searchReadsResponse.getReads();
        for (Read r : reads) {
        }
    }
}
