/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
