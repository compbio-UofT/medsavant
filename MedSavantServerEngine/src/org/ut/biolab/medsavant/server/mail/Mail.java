/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.server.mail;

import java.io.File;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;

public class Mail {

    static String src = CryptoUtils.decrypt("bqgO3l4Mri63fD852DLpZ8ZMClOw+jFu");
    static String srcName = "MedSavant Server Utility";
    static String pw = CryptoUtils.decrypt("OxTfiD1tzb7BvRHGfn+MoA==");
    static String host = "smtp.gmail.com";
    static String port = "465";
    static String starttls = "true";
    static String auth = "true";
    static String socketFactoryClass = "javax.net.ssl.SSLSocketFactory";
    static String fallback = "false";

    public static void main(String[] args) {
        String from = "MedSavant Server Utility";
        Mail.sendEmail(from, "Test Subject", "Test Body", null);
    }

    public synchronized static boolean sendEmail(String to, String subject, String text) {
        return sendEmail(to,subject,text,null);
    }

    public synchronized static boolean sendEmail(String to, String subject, String text, File attachment) {
        try {
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
            msg.setFrom(new InternetAddress(src,srcName));
            InternetAddress[] address = InternetAddress.parse(to);
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject(subject);
            // create and fill the first message part
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText(text);

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

            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }
}
