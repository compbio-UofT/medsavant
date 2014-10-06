package org.ut.biolab.medsavant.client.cli;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.controller.SettingsController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.ClientNetworkUtils;
import org.ut.biolab.medsavant.client.util.DownloadEvent;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.view.login.LoginEvent;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class Cli {
    /** Possible tasks that the CLI can handle */
    private enum Task {
        NONE, UPLOAD_VCF
    }

    private Task task = Task.NONE;
    private String filename = null;
    private String project = null;
    private String email = null;
    private boolean phasing = false;
    private boolean geneAnnotation = false;

    private final String usage = "usage: java -jar medsavant-cli-*.jar -h host -p port -d dbname -u user -w password\n" +
                    "      [--upload_vcf filename --project name] [--email address] [--phasing] [--gene-annotation]";

    public Cli(String args[]) {
        LongOpt[] longOpts = Cli.getLongOpts();
        Getopt g = new Getopt("MedSavant", args, "h:p:d:u:w:", longOpts);
        int c;

        boolean startGui = true;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 0:
                    // handling longopts
                    startGui = false;
                    LongOpt l = longOpts[g.getLongind()];
                    if (l.getName().equals("help")) {
                        System.out.println(usage);
                        System.exit(0);
                    } else if (l.getName().equals("upload-vcf")) {
                        filename = g.getOptarg();
                        task = Task.UPLOAD_VCF;
                    } else if (l.getName().equals("project")) {
                        project = g.getOptarg();
                    } else if (l.getName().equals("email")) {
                        email = g.getOptarg();
                    } else if (l.getName().equals("phasing")) {
                        phasing = true;
                    } else if (l.getName().equals("gene-annotation")) {
                        geneAnnotation = true;
                    }
                    break;
                case 'h':
                    String host = g.getOptarg();
                    SettingsController.getInstance().setServerAddress(g.getOptarg());
                    break;
                case 'p':
                    int port = Integer.parseInt(g.getOptarg());
                    SettingsController.getInstance().setServerPort(port + "");
                    break;
                case 'd':
                    String dbname = g.getOptarg();
                    SettingsController.getInstance().setDBName(dbname);
                    break;
                case 'u':
                    String username = g.getOptarg();
                    SettingsController.getInstance().setUsername(username);
                    break;
                case 'w':
                    String password = g.getOptarg();
                    SettingsController.getInstance().setPassword(password);
                    break;
                case '?':
                    System.out.println(usage);
                    System.exit(1);
                default:
                    System.out.println("getopt() returned " + c);
            }
        }
        // Validate presence of arguments required for some tasks.
        switch (task) {
            case UPLOAD_VCF:
                if (filename == null) {
                    System.err.println(usage);
                    System.exit(1);
                }
                if (project == null) {
                    System.err.println(usage);
                    System.exit(1);
                }
                break;
            case NONE:
            default:
                System.err.println(usage);
                System.exit(1);
        }

        try {
            Logger.getRootLogger().setLevel(Level.OFF);
            login();
            processTasks();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static LongOpt[] getLongOpts() {
        List<LongOpt> longopts = new ArrayList<LongOpt>();
        longopts.add(new LongOpt("upload-vcf", LongOpt.REQUIRED_ARGUMENT, null, 0));
        longopts.add(new LongOpt("project", LongOpt.REQUIRED_ARGUMENT, null, 0));
        longopts.add(new LongOpt("email", LongOpt.REQUIRED_ARGUMENT, null, 0));
        longopts.add(new LongOpt("phasing", LongOpt.NO_ARGUMENT, null, 0));
        longopts.add(new LongOpt("gene-annotation", LongOpt.NO_ARGUMENT, null, 0));
        return longopts.toArray(new LongOpt[longopts.size()]);
    }

    private void login() throws Exception {
        // let's login

        final SettingsController settingsController = SettingsController.getInstance();

        // This thread helps us make login synchronous, so we don't need to next the next step in the event handler.
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });

        Listener<LoginEvent> loginEventListener = new Listener<LoginEvent>() {
            @Override
            public void handleEvent(LoginEvent event) {
                try {
                    switch (event.getType()) {
                        case LOGGED_IN:
                            break;
                        case LOGGED_OUT:
                        case LOGIN_CANCELLED:
                        case LOGIN_FAILED:
                            throw event.getException();
                    }

                    t.interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        };

        LoginController.getInstance().addListener(loginEventListener);

        t.start();
        LoginController.getInstance().login(
                settingsController.getUsername(),
                settingsController.getPassword(),
                settingsController.getDBName(),
                settingsController.getServerAddress(),
                settingsController.getServerPort());
        // wait for the busy-waiting thread to exit
        t.join();

        LoginController.getInstance().removeListener(loginEventListener);

    }


    private void processTasks() throws Exception {

        switch (task) {
            case UPLOAD_VCF:
                // so far so good. Let's upload!

                ProjectController.getInstance().setProject(project);

                int[] transferIDs = null;

                try {
                    transferIDs = uploadVCFtoServer(filename);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.exit(1);
                }

                obtainDBLock();

                processVCF(email, phasing, geneAnnotation, transferIDs);

                System.out.println("\nThe VCF file is now uploaded and processed. Exiting.");
                System.exit(0);

            case NONE:
            default:
                return;
        }
    }

    private int[] uploadVCFtoServer(String filename) throws ExecutionException, InterruptedException {
        int[] ids = new int[1];
        File f = new File(filename);

        System.out.println("Copying " + filename + " to server.");
        ids[0] = ClientNetworkUtils.copyFileToServer(f, new Listener<DownloadEvent>() {
            @Override
            public void handleEvent(DownloadEvent event) {
                System.out.print(".");
            }
        }).get();
        System.out.println("Done");

        return ids;
    }

    private void obtainDBLock() {
        try {
            if (!getDBLockState()) {
                return;
            }
            System.out.println("Ongoing upload detected. Waiting for it to finish.");
            while (getDBLockState()) {
                Thread.sleep(3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Return True if locked, False otherwise.
     */
    private boolean getDBLockState() throws RemoteException, SessionExpiredException {
        return MedSavantClient.SettingsManager.isProjectLockedForChanges(
                LoginController.getSessionID(),
                ProjectController.getInstance().getCurrentProjectID());
    }

    private void processVCF(String email, boolean phasing, boolean geneAnnotation, int[] transferIDs) throws Exception {
        System.out.println("Parsing and annotating VCF, this may take a while.");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                int columns = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        break;
                    }
                    System.out.print('.');
                    columns++;
                    if (columns >= 80) {
                        System.out.print('\n');
                        columns = 0;
                    }
                }
                System.out.println("Done!");
            }
        });
        t.start();

        String sessionID = LoginController.getSessionID();
        int projectID = ProjectController.getInstance().getCurrentProjectID();

        MedSavantClient.VariantManager.uploadVariants(
                sessionID,
                transferIDs,
                projectID,
                ReferenceController.getInstance().getCurrentReferenceID(),
                new String[][]{},
                ProjectController.getInstance().getContainsRefCalls(sessionID, projectID),
                email,
                true,
                geneAnnotation,
                phasing);

        t.interrupt();
    }

    public static void main(String args[]) {
        Cli cli = new Cli(args);
    }
}
