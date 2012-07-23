/*
 *    Copyright 2010-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.server.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import org.ut.biolab.medsavant.server.mail.Mail;
import org.ut.biolab.medsavant.util.DirectorySettings;


/**
 * Log4J appender which writes to the server log.
 *
 * @author mfiume, tarkvara
 */
public class ServerLogger extends AppenderSkeleton {
    private static final Log LOG = LogFactory.getLog(ServerLogger.class);
    private static final String LOG_PATH = new File(DirectorySettings.getTmpDirectory(), "server.log").getAbsolutePath();
    private static String eMailAddress;
    private BufferedWriter writer;

    @Override
    public synchronized void close() {
        try {
            writer.close();
        } catch (IOException ignored) {
        }
        writer = null;
    }

    @Override
    protected synchronized void append(LoggingEvent le) {
        try {
            if (writer == null) {
                writer = new BufferedWriter(new FileWriter(LOG_PATH));
            }
            writer.write(layout.format(le));
            writer.flush();
        } catch (IOException ignored) {
        }
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    public static void setMailRecipient(String address) {
        eMailAddress = address;
    }

    public static void logByEmail(String subject, String message) {
        message += "\n\nMedSavant Server Utility";
        if (eMailAddress != null) {
            Mail.sendEmail(eMailAddress, subject, message);
            LOG.info("(Also emailed to " + eMailAddress + "): \"" + message.replace("\n", "") + "\"");
        } else {
            LOG.warn("(No email recipient configured): \"" + message.replace("\n", "") + "\"");
        }
    }
}
