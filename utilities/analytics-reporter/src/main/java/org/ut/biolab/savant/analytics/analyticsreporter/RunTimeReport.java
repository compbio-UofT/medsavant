package org.ut.biolab.savant.analytics.analyticsreporter;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mfiume
 */
class RunTimeReport extends Report {

    public RunTimeReport(String softwareName) {
        super("RunTime - " + softwareName);
    }

    @Override
    void runReport() {
        try {
            addInfoLog("Downloading events");
            List<SavantEvent> startEvents = SavantEvents.getEventsFromDatabase(SavantEvents.KEY_MESSAGE,SavantEvents.VALUE_ONSESSIONSTART);
            List<SavantEvent> endEvents = SavantEvents.getEventsFromDatabase(SavantEvents.KEY_MESSAGE,SavantEvents.VALUE_ONSESSIONEND);

            for (SavantEvent startEvent : startEvents) {
                String sessionID = startEvent.getEventValueForKey(SavantEvents.KEY_SESSIONID);
                SavantEvent matchingEndEvent = SavantEvents.getFirstEventFromSet(SavantEvents.KEY_SESSIONID, sessionID, endEvents);
                if (matchingEndEvent != null) {

                    // compute session time
                    long endTime = Long.parseLong(matchingEndEvent.getEventValueForKey(SavantEvents.KEY_SERVERTIME));
                    long startTime = Long.parseLong(startEvent.getEventValueForKey(SavantEvents.KEY_SERVERTIME));

                    System.out.println("Time " + (endTime-startTime));
                }
            }

            addInfoLog("Done downloading events");

        } catch (Exception ex) {
            addErrorLog(ex.getLocalizedMessage());
            ex.printStackTrace();
            return;
        }
    }
}
