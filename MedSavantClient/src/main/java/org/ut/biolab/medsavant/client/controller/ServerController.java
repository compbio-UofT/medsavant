package org.ut.biolab.medsavant.client.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.view.splash.MedSavantServerInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mfiume on 2/3/2014.
 */
public class ServerController {

    private final ArrayList<Listener> listeners;
    Log LOG = LogFactory.getLog(ServerController.class);

    List<MedSavantServerInfo> servers;
    private String SERVER_FILE_NAME = ".servers";

    private static String KEY_SETTING_LASTSERVER_NICKNAME = "server-nickname";

    private static ServerController instance;
    private MedSavantServerInfo currentServer;

    public static  ServerController getInstance() {
        if (instance == null) {
            instance = new ServerController();
        }
        return instance;
    }

    private ServerController() {

        listeners = new ArrayList<Listener>();
        loadServers();

        String serverNickName = SettingsController.getInstance().getValue(KEY_SETTING_LASTSERVER_NICKNAME);
        if (serverNickName != null) {
            MedSavantServerInfo server = getServerWithName(serverNickName);
            if (server != null) {
                setCurrentServer(server);
            }
        } else {
            if (servers.size() > 0) {
                setCurrentServer(servers.get(0));
            }
        }
    }

    public void setCurrentServer(MedSavantServerInfo server) {
        currentServer = server;
        if (server == null) {
            SettingsController.getInstance().setValue(KEY_SETTING_LASTSERVER_NICKNAME, null);
        } else  {
            SettingsController.getInstance().setValue(KEY_SETTING_LASTSERVER_NICKNAME, server.getNickname());
            LOG.info("Setting server to " + server.getNickname());
        }

        notifyListeners();
    }

    private void notifyListeners() {
        for (Listener l : listeners) {
            l.handleEvent(null);
        }
    }

    private MedSavantServerInfo getServerWithName(String serverNickName) {

        for (MedSavantServerInfo s : servers) {
            if (s.getNickname().equals(serverNickName)) {
                return s;
            }
        }

        return null;
    }

    private File getServerFile() {
        return new File(DirectorySettings.getMedSavantDirectory(), SERVER_FILE_NAME);
    }

    public void addServer(MedSavantServerInfo server) {
        LOG.info("Adding server " + server.getNickname() + " count is " + servers.size());
        servers.add(server);
        saveServers();
    }

    private synchronized void saveServers() {

        LOG.info("Serializing " + servers.size() + " servers");
        FileOutputStream fileout = null;
        ObjectOutputStream out = null;

        try {
            fileout = new FileOutputStream(getServerFile());
            out = new ObjectOutputStream(fileout);
            out.writeObject(servers);
            out.close();
            fileout.close();

            LOG.info("Saved " + servers.size() + " servers");
        } catch (Exception ex) {
            LOG.error("Problem saving servers", ex);
        } finally {
            try {
                out.close();
            } catch (Exception ex) {
            }
            try {
                fileout.close();
            } catch (Exception ex) {
            }
        }


    }

    private synchronized void loadServers() {
        FileInputStream filein = null;
        ObjectInputStream in = null;
        try {
            // create the server file
            if (!getServerFile().exists()) {
                servers = new ArrayList<MedSavantServerInfo>();
                saveServers();
                // load the server file
            } else {
                filein = new FileInputStream(getServerFile());
                in = new ObjectInputStream(filein);
                try {
                    servers = (List<MedSavantServerInfo>) in.readObject();

                // happens if the server class changes, nuke the file and start again
                } catch (InvalidClassException e) {
                    LOG.info("Corrupted server file, recreating");
                    getServerFile().delete();
                    servers = new ArrayList<MedSavantServerInfo>();
                    saveServers();
                }
                in.close();
                filein.close();
            }

            LOG.info("Loaded " + servers.size() + " servers");
        } catch (Exception ex) {
            LOG.error("Problem loading servers", ex);
            servers = new ArrayList<MedSavantServerInfo>();
        } finally {
            try {
                in.close();
            } catch (Exception ex) {
            }
            try {
                filein.close();
            } catch (Exception ex) {
            }
        }
    }

    public void removeServer(MedSavantServerInfo server) {

        LOG.info("Removing server " + server.getNickname() + " count is " + servers.size());

        for (MedSavantServerInfo s : servers) {
            boolean equal = s == server;
            LOG.info(s.getNickname() + " vs " + server.getNickname() + " = " + equal);
        }

        servers.remove(server);

        printServers();

        if (currentServer == server) {
            setCurrentServer(null);
        }

        saveServers();
    }

    private void printServers() {
        LOG.info("Servers:");
        for (MedSavantServerInfo s : servers) {
            LOG.info("\t" + s.getNickname());
        }
    }

    public MedSavantServerInfo getCurrentServer() {
        return currentServer;
    }

    public void addListener(Listener serverListener) {
        listeners.add(serverListener);
    }

    public List<MedSavantServerInfo> getServers() {
        return servers;
    }

    public boolean isServerNamed(String name) {
        for (MedSavantServerInfo s : servers) {
            if (s.getNickname().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
