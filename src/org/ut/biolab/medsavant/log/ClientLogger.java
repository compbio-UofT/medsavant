package org.ut.biolab.medsavant.log;

import org.ut.biolab.medsavant.db.log.BriefLogFormatter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mfiume
 */
public class ClientLogger {

    private static String logPath = "client.log";
    private static BufferedWriter writer;
    private static Logger logger;
    private static boolean logOpen;

    public static void log(Class c, String string) {
        log(c, string, Level.INFO);
    }

    private static void openLogFile() throws IOException {
        FileHandler handler = new FileHandler(logPath, true);
        handler.setFormatter(new BriefLogFormatter());
        logger = Logger.getLogger("org.ut.biolab.medsavant.server");
        logger.addHandler(handler);
        logOpen = true;
    }

    public static void log(Class c, String msg, Level level) {
        try {
            if (!logOpen) {
                openLogFile();
            }
            logger.log(level, "'{'{0}'} '{1}", new Object[]{c.toString(), msg});
        } catch (IOException ex) {
        }
    }

    public static void setLogStatus(boolean b) {
        if (b) {
        } else {
            try {
                writer.close();
            } catch (IOException ex) {
            }
            writer = null;
        }
    }
}
