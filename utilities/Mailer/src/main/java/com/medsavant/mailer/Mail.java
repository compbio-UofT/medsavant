/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package com.medsavant.mailer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Mail {

    private static final Log LOG = LogFactory.getLog(Mail.class);
    static String src;
    static String srcName = "MedSavant Development Team";
    static String pw;
    static String host = "smtp.gmail.com";
    static int port = 465;
    static String starttls = "true";
    static String auth = "true";
    static String socketFactoryClass = "javax.net.ssl.SSLSocketFactory";
    static String fallback = "false";

    public static void main(String[] args) throws IOException {
        CommandLineParser parser = new GnuParser();
        Options ops = getOptions();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(ops, args);

            // print help
            if (line.hasOption('h') || line.getOptions().length == 0) {
                printHelp();
                return;
            }
            
            // parse args
            
            String email = null;
            String emailPass = null;
            String mailingList = null;
            String subject = null;
            String htmlFile = null;
            
            for (Option o : line.getOptions()) {
                switch(o.getOpt().charAt(0)) {
                    case 's':
                        subject = o.getValue();
                        break;
                    case 'e':
                        htmlFile = o.getValue();
                        break;
                    case 'u':
                        email = o.getValue();
                        break;
                    case 'p':
                        emailPass = o.getValue();
                        break;
                    case 'l':
                        mailingList = o.getValue();
                        break;
                }
            }
            
            setMailCredentials(email,emailPass,host,port);
            
            String text = readFileIntoString(new File(htmlFile));
            
            sendEmail(mailingList,subject,text);
            
        } catch (org.apache.commons.cli.ParseException exp) {
            
            printHelp();
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        }
    }

    public synchronized static boolean sendEmail(String to, String subject, String text) {
        return sendEmail(to, subject, text, null);
    }

    public static Options getOptions() {
        // create Options object
        Options options = new Options();

        options.addOption("h", false, "help");
        options.addOption("u", true, "Gmail username");
        options.addOption("p", true, "Gmail password");
        //options.addOption("h", true, "email host");
        //options.addOption("o", true, "email port");
        options.addOption("l", true, "mailing list (comma separated list of emails)");
        
        options.addOption("s", true, "subject");
        options.addOption("e", true, "path to html file");

        return options;

    }

    public static void setMailCredentials(String username, String password, String smtp, int port) {
        Mail.src = username;
        Mail.pw = password;
        Mail.host = smtp;
        Mail.port = port;
    }

    public synchronized static boolean sendEmail(String to, String subject, String text, File attachment) {
        try {

            if (src == null || pw == null || host == null || port == -1) {
                return false;
            }

            if (to.isEmpty()) {
                return false;
            }

            LOG.info("Sending email to " + to + " with subject " + subject);

            // create some properties and get the default Session
            Properties props = new Properties();
            props.put("mail.smtp.user", src);
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.starttls.enable", starttls);
            props.put("mail.smtp.auth", auth);
            props.put("mail.smtp.socketFactory.port", port);
            props.put("mail.smtp.socketFactory.class", socketFactoryClass);
            props.put("mail.smtp.socketFactory.fallback", fallback);
            Session session = Session.getInstance(props, null);
            // create a message
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(src, srcName));
            InternetAddress[] address = InternetAddress.parse(to);
            msg.setRecipients(Message.RecipientType.BCC, address);
            msg.setSubject(subject);
            // create and fill the first message part
            MimeBodyPart mbp1 = new MimeBodyPart();

            mbp1.setContent(text, "text/html");

            // create the Multipart and add its parts to it
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);

            if (attachment != null) {
                // create the second message part
                MimeBodyPart mbp2 = new MimeBodyPart();
                // attach the file to the message
                FileDataSource fds = new FileDataSource(attachment);
                mbp2.setDataHandler(new DataHandler(fds));
                mbp2.setFileName(fds.getName());
                mp.addBodyPart(mbp2);
            }

            // add the Multipart to the message
            msg.setContent(mp);
            // set the Date: header
            msg.setSentDate(new Date());
            // send the message
            Transport transport = session.getTransport("smtp");
            transport.connect(host, src, pw);
            transport.sendMessage(msg, msg.getAllRecipients());
            transport.close();

            LOG.info("Mail sent");
            
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex);
            return false;
        }

    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("savant-mailer", getOptions());
    }

    private static String readFileIntoString(File file) throws IOException {
        return IOUtils.toString(new FileInputStream(file));
    }

}
