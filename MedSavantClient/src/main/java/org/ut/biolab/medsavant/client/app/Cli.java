package org.ut.biolab.medsavant.client.app;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.controller.SettingsController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.ClientNetworkUtils;
import org.ut.biolab.medsavant.client.util.DownloadEvent;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.appapi.MedSavantApp;
import org.ut.biolab.medsavant.shared.serverapi.SettingsManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class Cli {
    private enum Task {
        NONE, UPLOAD
    }

    private static VariantManagerAdapter variantManager = MedSavantClient.VariantManager;
    private static SettingsManagerAdapter settingsManager = MedSavantClient.SettingsManager;

    public Cli(String args[]) {
        LongOpt[] longOpts = Cli.getLongOpts();
        Getopt g = new Getopt("MedSavant", args, "", longOpts);
        int c;

        Task task = Task.NONE;
        String filename = null;
        String project = null;
        String email = null;
        boolean phasing = false;
        boolean geneAnnotation = false;

        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 0:
                    // handling longopts
                    LongOpt l = longOpts[g.getLongind()];
                    if (l.getName().equals("upload")) {
                        filename = g.getOptarg();
                        task = Task.UPLOAD;
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
                case '?':
                    System.err.println(MedSavantClient.usage());
                    System.exit(1);
                    break; // getopt() already printed an error
                default:
                    System.out.print("getopt() returned " + c + "\n");
            }
        }

        if (task == Task.UPLOAD) {
            if (filename == null) {
                System.err.println("Upload requires a file");
            }
            if (project == null) {
                System.err.println("Upload requires a project name");
            }

            // let's login and set project!

            SettingsController settingsController = SettingsController.getInstance();
            try {
                MedSavantClient.SessionManager.registerNewSession(settingsController.getUsername(), settingsController.getPassword(), settingsController.getDBName());
                ProjectController.getInstance().setProject(project);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

            // so far so good. Let's upload!

            int[] transferIDs = null;

            try {
                transferIDs = uploadVCFtoServer(filename);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }

            System.out.println("Variants have been uploaded and are now being processed.");

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Uploading");

                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            return;
                        }
                        System.out.print('.');
                    }
                }
            });

            try {
                processVCF(email, phasing, geneAnnotation, transferIDs);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

            t.interrupt();

            System.out.println("Done!");

        } else {
            System.err.println("Nothing to do. Quitting.");
        }
    }

    private void processVCF(String email, boolean phasing, boolean geneAnnotation, int[] transferIDs) throws Exception {
        String sessionID = LoginController.getSessionID();
        int projectID = ProjectController.getInstance().getCurrentProjectID();

        variantManager.uploadVariants(
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
    }

    private int[] uploadVCFtoServer(String filename) throws ExecutionException, InterruptedException {
        int[] ids = new int[1];
        File f = new File(filename);

        ids[0] = ClientNetworkUtils.copyFileToServer(f, new Listener<DownloadEvent>() {
                @Override
                public void handleEvent(DownloadEvent event) {
                    System.out.println(event.getProgress());
                }
        }).get();

        return ids;
    }

    public static String getUsage() {
        return "[--upload filename] [--project name] [--email address] [--phasing] [--gene-annotation]";
    }

    public static LongOpt[] getLongOpts() {
        List<LongOpt> longopts = new ArrayList<LongOpt>();
        longopts.add(new LongOpt("upload", LongOpt.REQUIRED_ARGUMENT, null, 0));
        longopts.add(new LongOpt("project", LongOpt.REQUIRED_ARGUMENT, null, 0));
        longopts.add(new LongOpt("email", LongOpt.REQUIRED_ARGUMENT, null, 0));
        longopts.add(new LongOpt("phasing", LongOpt.NO_ARGUMENT, null, 0));
        longopts.add(new LongOpt("gene-annotation", LongOpt.NO_ARGUMENT, null, 0));
        return longopts.toArray(new LongOpt[longopts.size()]);
    }
}
