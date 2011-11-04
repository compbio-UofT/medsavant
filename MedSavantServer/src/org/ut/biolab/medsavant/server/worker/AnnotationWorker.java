/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.server.worker;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil.Action;
import org.ut.biolab.medsavant.db.util.query.ServerLogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ServerLogQueryUtil.LogType;
import org.ut.biolab.medsavant.server.log.ServerLogger;
import org.ut.biolab.medsavant.server.update.UpdateVariantTable;

/**
 *
 * @author Andrew
 */
public class AnnotationWorker extends SwingWorker {

    private final static int DELAY =  1 * 6000; // 10 minutes

    public static void main(String[] args) throws InterruptedException {

        new AnnotationWorker().execute();

    }

    @Override
    protected Object doInBackground() throws Exception {
        while (true) {
            try {
                ServerLogQueryUtil.addServerLog(LogType.INFO, "Starting pending annotations");
                kickStartPendingAnnotations();
                ServerLogQueryUtil.addServerLog(LogType.INFO, "Done starting pending annotations");            
            } catch (Exception e) {
                ServerLogger.logError(AnnotationWorker.class,e);
                ServerLogger.logByEmail(AnnotationWorker.class, "Uh oh...", e.getMessage(), Level.SEVERE);
            }
            Thread.sleep(DELAY);
        }
    }

    private void kickStartPendingAnnotations() throws SQLException, IOException {
        ResultSet rs = AnnotationLogQueryUtil.getPendingUpdates();

        try {
            while (rs.next()) {

                ServerLogQueryUtil.addServerLog(LogType.INFO, "Starting next annotation");
                
                int projectId = rs.getInt("project_id");
                int referenceId = rs.getInt("reference_id");
                int updateId = rs.getInt("update_id");
                Action action = AnnotationLogQueryUtil.intToAction(rs.getInt("action"));

                AnnotationLogQueryUtil.setAnnotationLogStatus(updateId, AnnotationLogQueryUtil.Status.INPROGRESS);

                try {
                    switch (action) {
                        case ADD_VARIANTS:
                            //TODO: users shouldnt see ids
                            ServerLogQueryUtil.addServerLog(ServerLogQueryUtil.LogType.INFO, "Adding variants to projectid=" + projectId + " referenceid=" + referenceId);
                            UpdateVariantTable.performAddVCF(projectId, referenceId, updateId);
                            //TODO: users shouldnt see ids
                            ServerLogQueryUtil.addServerLog(ServerLogQueryUtil.LogType.INFO, "Done adding variants to projectid=" + projectId + " referenceid=" + referenceId);
                            break;
                        case UPDATE_TABLE:
                            // TODO: users shouldnt see ids
                            ServerLogQueryUtil.addServerLog(ServerLogQueryUtil.LogType.INFO, "Updating table projectid=" + projectId + " referenceid=" + referenceId + " updateid=" + updateId);
                            UpdateVariantTable.performUpdate(projectId, referenceId, updateId);
                            // TODO: users shouldnt see ids
                            ServerLogQueryUtil.addServerLog(ServerLogQueryUtil.LogType.INFO, "Done updating table projectid=" + projectId + " referenceid=" + referenceId + " updateid=" + updateId);
                            break;
                        default:
                            ServerLogQueryUtil.addServerLog(ServerLogQueryUtil.LogType.ERROR, "Unknown annotation action: " + action);
                            break;
                    }
                    
                    AnnotationLogQueryUtil.setAnnotationLogStatus(updateId, AnnotationLogQueryUtil.Status.COMPLETE);
                } catch (Exception e) {
                    ServerLogger.logError(AnnotationWorker.class,e);
                    ServerLogger.logByEmail(AnnotationWorker.class, "Uh oh...", "There was a problem making update " + updateId + ". Here's the error message:\n\n" + e.getLocalizedMessage());
                    AnnotationLogQueryUtil.setAnnotationLogStatus(updateId, AnnotationLogQueryUtil.Status.ERROR);
                }
            }

        } catch (SQLException ex) {
            ServerLogger.logError(AnnotationWorker.class,ex);
            ServerLogger.log(AnnotationWorker.class, ex.getLocalizedMessage(), Level.SEVERE);
        }
    }
}
