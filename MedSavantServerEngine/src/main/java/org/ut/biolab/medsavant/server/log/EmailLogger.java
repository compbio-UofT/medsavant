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
import org.ut.biolab.medsavant.shared.util.DirectorySettings;

/**
 * Log4J appender which writes to the server log.
 *
 * @author mfiume, tarkvara
 */
public class EmailLogger extends AppenderSkeleton {

    private static final Log LOG = LogFactory.getLog(EmailLogger.class);
    private static final String LOG_PATH = new File(DirectorySettings.getTmpDirectory(), "server.log").getAbsolutePath();
    private static String emailAddress;
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
        emailAddress = address;
    }

    public static void logByEmail(String subject, String message) {
        logByEmail(subject,message,null);
    }

    public static void logByEmail(String subject, String message, String cc) {
        message += "\n\nSent by the MedSavant Server Utility";
        long time = System.currentTimeMillis();
        if (emailAddress != null && !emailAddress.isEmpty()) {
            Mail.sendEmail(emailAddress, time + " - " + subject, message);
        } else {
            LOG.warn("Cannot send email, no email recipient configured");
        }
        if (cc != null) {
            Mail.sendEmail(cc, time + " - " + subject, message);
        }
    }
}
