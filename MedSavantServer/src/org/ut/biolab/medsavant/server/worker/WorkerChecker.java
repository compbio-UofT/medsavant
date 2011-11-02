package org.ut.biolab.medsavant.server.worker;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.db.util.query.ServerLogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ServerLogQueryUtil.LogType;
import org.ut.biolab.medsavant.server.log.ServerLogger;

/**
 *
 * @author mfiume
 */
public class WorkerChecker extends SwingWorker {

    public static final int DELAY = 30*60000; // 30 minutes
    private List<SwingWorker> workers;
    
    public WorkerChecker(List<SwingWorker> workers) {
        this.workers = workers;
    }
    
    @Override
    protected Object doInBackground() throws Exception {
        while (true) {
            rebootWorkersIfDead();
            Thread.sleep(DELAY);
        }
    }

    private void rebootWorkersIfDead() {
        boolean foundDeadWorker = false;
        List<SwingWorker> newWorkers = new ArrayList<SwingWorker>();
        for (SwingWorker w : workers) {
            if (w.isCancelled() || w.isDone()) {
                foundDeadWorker = true;
                SwingWorker newWorker = null;
                if (w instanceof PhoneHomeWorker) {
                    newWorker = new PhoneHomeWorker();
                } else if (w instanceof AnnotationWorker) {
                    newWorker = new AnnotationWorker();
                } else {
                    ServerLogger.log(WorkerChecker.class, "Unrecognized worker " + w.getClass().getName() + "; could not reboot", Level.SEVERE);
                }
                if (newWorker != null) {
                    newWorker.execute();
                    newWorkers.add(newWorker);
                }
            } else {
                newWorkers.add(w);
            }
        }
        if (foundDeadWorker) {
            ServerLogQueryUtil.addServerLog(LogType.INFO, "Found dead worker(s); attempted restart");
        } else {
           ServerLogQueryUtil.addServerLog(LogType.INFO, "All workers running");
        }
        workers = newWorkers;
    }
}
