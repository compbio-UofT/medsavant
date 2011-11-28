/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.query.ServerLogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ServerLogQueryUtil.LogType;
import org.ut.biolab.medsavant.server.log.ServerLogger;
import org.ut.biolab.medsavant.server.worker.AnnotationWorker;
import org.ut.biolab.medsavant.server.worker.PhoneHomeWorker;
import org.ut.biolab.medsavant.server.worker.WorkerChecker;

/**
 *
 * @author mfiume
 */
public class MedSavantServerUtility {

    private static final Object lock = new Object();
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        
        String host = "localhost";
        String username = null;
        int port = 5029;
        String name = null;
        
        boolean isAnnotation = false;
        boolean isPreFormatted = false;
        String annotationFile = null;
        String annotationFormat = null;
        
        try {
            for(int i = 0; i < args.length; i++){
                String arg = args[i];
                if(arg.equals("-h")){
                    host = args[++i];
                } else if (arg.equals("-p")){
                    port = Integer.parseInt(args[++i]);
                } else if (arg.equals("-d")){
                    name = args[++i];
                } else if (arg.equals("-a")){
                    isAnnotation = true;
                    annotationFile = args[++i];
                    annotationFormat = args[++i];
                } else if (arg.equals("-af")){
                    isAnnotation = true;
                    isPreFormatted = true;
                    annotationFile = args[++i];
                    annotationFormat = args[++i];
                } else if (arg.equals("-u")) {
                    username = args[++i];
                } else if (arg.equals("-e")) {
                    ServerLogger.setMailRecipient(args[++i]);
                } else if (arg.equals("--help")){
                    exitWithUsage();
                }
            }
        } catch (Exception e){
            exitWithUsage();
        }
        
        
        
        String password = null;
        
        if(name == null || username == null) exitWithUsage();
        try {
            password = askForPassword(username);
        } catch (IOException ex) {
            System.err.println("Problem getting password");
            System.exit(1);
        }
        
        flushln("=== MedSavant Server Utility ===");
        
        
        flush("Setting up database connection...");
        ConnectionController.setHost(host);
        ConnectionController.setPort(port);
        ConnectionController.setDBName(name);
        flushOK();
        
        flush("Setting credentials...");
        ConnectionController.setCredentials(username, password);
        flushOK();
        
        /*
        ServerLogger.log(MedSavantServerUtility.class, "dbhost: " + host);
        ServerLogger.log(MedSavantServerUtility.class, "dbport: " + port);
        ServerLogger.log(MedSavantServerUtility.class, "dbname: " + name);
        ServerLogger.log(MedSavantServerUtility.class, "username: " + username);
         * 
         */
        
        if(isAnnotation){
            if(annotationFile == null || annotationFormat == null){
                exitWithUsage();
            } else {
                ServerLogQueryUtil.addServerLog(LogType.INFO, "Server adding annotation");
                AddAnnotation.addAnnotation(annotationFile, annotationFormat, isPreFormatted);
                return;
            }
        }    
         
        try {
            flush("Connecting to database...");
            ConnectionController.connectPooled();
            flushOK();
        } catch (SQLException ex) {
            System.err.println("\n\nThere was an error while connecting to the database: ");
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        
        flush("Initiating registration...");
        PhoneHomeWorker phoneHomeSwingWorker = new PhoneHomeWorker();
        phoneHomeSwingWorker.execute();
        flushOK();
        
        flush("Initiating annotater...");
        AnnotationWorker annotationSwingWorker = new AnnotationWorker();
        annotationSwingWorker.execute();
        flushOK();
        
        List<SwingWorker> workers = new ArrayList<SwingWorker>();
        workers.add(phoneHomeSwingWorker);
        workers.add(annotationSwingWorker);
        
        flush("Initiating checker...");
        WorkerChecker workerChecker = new WorkerChecker(workers);
        workerChecker.execute();
        flushOK();
        
        flushln("Server booted");
        ServerLogQueryUtil.addServerLog(LogType.INFO, "Server booted");
        
        synchronized (lock) {
            try { lock.wait() ; }
            catch (Exception e)  {}
        }
        // TODO code application logic here
    }
    
    private static void exitWithUsage(){
        System.out.println(
                "Usage: MedSavantServer -d databasename -u username [OPTION]\n\n"
                + "Options:\n"
                + "-h hostName\t\t\t\tSpecify the host name.\n"
                + "-p portNumber\t\t\t\tSpecify the port number.\n"
                + "-d databaseName\t\t\t\tSpecify the database name (mandatory).\n"
                + "-u username\t\t\t\tSpecify the username to login to the database. Important: this MUST be the user running the database server.\n"
                + "-a annotationFile formatFile\t\tAdd text annotation file \n\t\t\t\t\t(overrides default behaviour)\n"
                + "-af formattedAnnotationFile formatFile\tAdd tabix annotation file.\n"
                + "\t\t\t\t\tIndex file should be in same directory.\n"
                + "\t\t\t\t\t(overrides default behaviour)\n"
                + "-e emailAddress\t\t\t\tSpecify email address for notifications."
                + "");
        System.exit(0);
    }

    private static String askForPassword(String un) throws IOException {
        return new String(System.console().readPassword("Enter password for " + un + ":"));
    }

    private static void flush(String string) {
        System.out.print(string);
        System.out.flush();
    }

    private static void flushln(String string) {
        System.out.println(string);
    }

    private static void flushOK() {
        flushln("OK");
    }
}
