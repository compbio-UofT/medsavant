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
package org.ut.biolab.medsavant.shared.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;

import net.sf.samtools.util.SeekableStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;


/**
 * Random access stream for FTP access to BAM files through Picard
 *
 * @author vwilliams
 */
public class SeekableFTPStream extends SeekableStream {

    private static Log LOG = LogFactory.getLog(SeekableFTPStream.class);
    private static final int SOCKET_TIMEOUT = 10000;

    private final String source;
    private final String username;
    private final String password;
    private final String host;
    private final int port;
    private final String fileName;

    private long length;
    private FTPClient ftpClient = null;
    private long position = 0;

    public SeekableFTPStream(URL url) {
        this(url, "anonymous", "");
    }

    public SeekableFTPStream(URL url, String user, String pwd) {

        if (url == null) {
            throw new IllegalArgumentException("URL may not be null");
        }
        if (!url.getProtocol().toLowerCase().equals("ftp")) {
            throw new IllegalArgumentException("Only ftp:// protocol URLs are valid.");
        }

        source = url.toString();
        username = user;
        password = pwd;
        host = url.getHost();
        int p = url.getPort();
        port = p != -1 ? p : url.getDefaultPort();
        fileName = url.getFile();
        length = 0;
    }

    @Override
    public long length() {
        if (length == 0) {
            FTPFile[] files;
            try{
                files = listFiles(fileName);
            }
            catch (IOException e) {
                try {
                    disconnect();
                    files = listFiles(fileName);
                } catch (IOException e1) {
                    LOG.warn("Unable to reconnect getting length");
                    return 0;
                }

            }
            for (int i=0; i<files.length; i++) {
                FTPFile file = files[i];
                if (file != null) {
                    if (file.getName().equals(fileName)) {
                        length = file.getSize();
                        break;
                   }
                }
            }
        }
        return length;

    }

    @Override
    public void seek(long pos) throws IOException {
        position = pos;
        LOG.info("FTP: seek to " + pos);
    }

    @Override
    public int read(byte[] bytes, int offset, int len) throws IOException {
        try {
            return readFromStream(bytes, offset, len);
        } catch (IOException x) {
            LOG.info("Connection closed during read.  Disconnecting and trying again at " + position);
            disconnect();
            return readFromStream(bytes, offset, len);
        }
    }

    private int readFromStream(byte[] bytes, int offset, int len) throws IOException {

        FTPClient client = getFTPClient();
        if (position != 0) {
            client.setRestartOffset(position);
        }
        InputStream is = client.retrieveFileStream(fileName);
        long oldPos = position;
        if (is != null) {
            int n = 0;
            while (n < len) {
               int bytesRead = is.read(bytes, offset+n, len-n);
               if (bytesRead < 0) {
                   if (n == 0) return -1;
                   else break;
               }
               n += bytesRead;
            }
            is.close();
            LOG.info(String.format("FTP read %d bytes at %d: %02x %02x %02x %02x %02x %02x %02x %02x...", len, oldPos, bytes[offset], bytes[offset + 1], bytes[offset + 2], bytes[offset + 3], bytes[offset + 4], bytes[offset + 5], bytes[offset + 6], bytes[offset + 7]));
            try {
                client.completePendingCommand();
            } catch (FTPConnectionClosedException suppressed) {
            } catch (SocketTimeoutException stx) {
                // Accessing 1000 Genomes, we sometimes get a timeout for no apparent reason.
                LOG.info("Timed out during read.  Disconnecting.");
                disconnect();
            }
            position += n;
            return n;
        } else {
            String msg = String.format("Unable to retrieve input stream for file (reply code %d).", client.getReplyCode());
            LOG.error(msg);
            throw new IOException(msg);
        }
    }

    @Override
    public void close() throws IOException {
        if (ftpClient != null) {
            try {
                ftpClient.completePendingCommand();
            } catch (IOException e) {
                LOG.trace("Suppressing IOException from completePendingCommand().");
            }
            try {
                ftpClient.logout();
            } catch (IOException e) {
                LOG.info("Suppressing IOException from logout().");
            }
            disconnect();
        }
    }

    @Override
    public boolean eof() throws IOException {
        return position >= length();
    }

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException("read() not supported for SeekableFTPStreams");
    }

    @Override
    public String getSource() {
        return source;
    }

    public FTPFile[] listFiles(String relPath) throws IOException {
        try {
            return getFTPClient().listFiles(relPath);
        } catch (FTPConnectionClosedException e) {
            disconnect();
            return getFTPClient().listFiles(relPath);
        }
    }

    public void disconnect() throws IOException {
        if (ftpClient != null) {
            try {
                ftpClient.disconnect();
            } finally {
                ftpClient = null;
            }
        }
    }

    private FTPClient getFTPClient() throws IOException {
        if (ftpClient == null) {
            FTPClient client = new FTPClient();
            try {
                client.connect(host, port);
                int reply = client.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    throw new IOException("Unable to connect to " + host);
                }
                if (!client.login(username, password)) {
                    throw new IOException("Unable to login to " + host + " as " + username);
                }
                client.setFileType(FTP.BINARY_FILE_TYPE);
                client.enterLocalPassiveMode();
                client.setSoTimeout(SOCKET_TIMEOUT);
                ftpClient = client;
                client = null;
            } finally {
                if (client != null) {
                    client.disconnect();
                }
            }
        }

        return ftpClient;
    }
}
