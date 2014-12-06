package org.ut.biolab.medsavant.client.app;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.controller.SettingsController;

import java.util.ArrayList;
import java.util.List;

public class Cli {
    private enum Task {
        NONE, UPLOAD
    }
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
        } else {
            System.err.println("Nothing to do. Quitting.");
        }
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
