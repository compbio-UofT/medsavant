/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.server.mail;

import java.io.File;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.log.EmailLogger;

public class Mail {

    private static final Log LOG = LogFactory.getLog(Mail.class);
    static String src;
    static String srcName = "MedSavant Server Utility";
    static String pw;
    static String host;
    static int port = -1;
    static String starttls = "true";
    static String auth = "true";
    static String socketFactoryClass = "javax.net.ssl.SSLSocketFactory";
    static String fallback = "false";

    public static void main(String[] args) {
    }

    public synchronized static boolean sendEmail(String to, String subject, String text) {
        return sendEmail(to,subject,text,null);
    }

    public static void setMailCredentials(String username, String password, String smtp, int port) {
        Mail.src = username;
        Mail.pw = password;
        Mail.host = smtp;
        Mail.port = port;
    }

    public synchronized static boolean sendEmail(String to, String subject, String text, File attachment) {
        try {

            if (src == null || pw == null || host == null || port == -1) { return false; }

            if (to.isEmpty()) { return false; }

            LOG.info("Sending email to " + to  + " with subject " + subject);

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

            String s = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\" style=\"margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;\">\n<head style=\"margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;\">\n<!-- If you delete this tag, the sky will fall on your head -->\n<meta name=\"viewport\" content=\"width=device-width\" style=\"margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;\">\n\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" style=\"margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;\">\n<title style=\"margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;\">"
                    + "MedSavant Server Message:"
                    + "</title>\n\n</head>\n<body bgcolor=\"#FFFFFF\" style=\"margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;-webkit-font-smoothing: antialiased;-webkit-text-size-adjust: none;height: 100%;width: 100%;\">\n<!-- BODY -->\n<table class=\"body-wrap\" style=\"margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;width: 100%;\">\n\t<tr style=\"margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;\">\n\t\t<td style=\"margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;\"></td>\n\t\t<td class=\"container\" bgcolor=\"#FFFFFF\" style=\"margin: 0 auto;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;display: block;max-width: 600px;clear: both;\">\n\t\t\t<div class=\"content\" style=\"margin: 0 auto;padding: 15px;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;max-width: 600px;display: block;\">\n\t\t\t<table style=\"margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;width: 100%;\">\n\t\t\t\t<tr style=\"margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;\">\n\t\t\t\t\t<td style=\"margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;\">\n\t\t\t\t\t\t<p style=\"margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;margin-bottom: 10px;font-weight: normal;font-size: 14px;line-height: 1.6;\"><center style=\"margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;\"><img width=\"150\" src=\"http://genomesavant.com/p/assets/img/cover/medsavantlogo.png\" style=\"margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;max-width: 100%;\"></center></p><!-- /hero -->\n\t\t\t\t\t\t<h3 style=\"margin: 0;padding: 0;font-family: &quot;HelveticaNeue-Light&quot;, &quot;Helvetica Neue Light&quot;, &quot;Helvetica Neue&quot;, Helvetica, Arial, &quot;Lucida Grande&quot;, sans-serif;line-height: 1.1;margin-bottom: 15px;color: #000;font-weight: 500;font-size: 27px;\">Server Message</h3>\n\t\t\t\t\t\t<p style=\"margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;margin-bottom: 10px;font-weight: normal;font-size: 14px;line-height: 1.6;\">"
                    + text
                    + "</p>\n\t\t\t\t\t</td>\n\t\t\t\t</tr>\n\t\t\t</table>"
                    + "<p><strong>Need Help?</strong> Feel free to <a href=\"mailto:support@genomesavant.com\">contact the "
                    + "MedSavant Development Team</a> if you have questions or are experiencing trouble.</p>"
                    + "<br>Sent by server on " + (new Date()).toString()
                    + "\n\t\t\t</div>\t\t\t\t\n\t\t</td>\n\t\t<td style=\"margin: 0;padding: 0;font-family: &quot;Helvetica Neue&quot;, &quot;Helvetica&quot;, Helvetica, Arial, sans-serif;\"></td>\n\t</tr>\n</table><!-- /BODY -->\n</body>\n</html>";

            mbp1.setContent(s, "text/html");

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
            LOG.error(ex);
            return false;
        }

    }


}
