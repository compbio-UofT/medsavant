package org.ut.biolab.medsavant.server.worker;

import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.db.util.query.ServerLogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ServerLogQueryUtil.LogType;

/**
 *
 * @author mfiume
 */
public class PhoneHomeWorker extends SwingWorker {

    public static final int DELAY = 5*60000; // 5 minutes
    
    @Override
    protected Object doInBackground() throws Exception {
        while (true) {
            ServerLogQueryUtil.addServerLog(LogType.INFO, "Checking in");
            Thread.sleep(DELAY);
        }
    }
    
}
