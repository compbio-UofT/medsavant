package org.ut.biolab.medsavant.client.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.view.login.MedSavantServerInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mfiume on 2/3/2014.
 */
public class ServerController {

    private final ArrayList<Listener<ServerController>> listeners;
    Log LOG = LogFactory.getLog(ServerController.class);

    List<MedSavantServerInfo> servers;
    MedSavantServerInfo tmpServer;
    
    private final String SERVER_FILE_NAME = ".servers";
    private static final String KEY_SETTING_LASTSERVER_NICKNAME = "server-nickname";

    private static ServerController instance;
    private MedSavantServerInfo currentServer;

    //private final Semaphore semaphore = new Semaphore(1, true);
    //private static final Object initializationLock = new Object();
    
    public synchronized static ServerController getInstance() {
        if (instance == null) {
            instance = new ServerController();
        }
        return instance;
    }

    private ServerController() {
        
        listeners = new ArrayList<Listener<ServerController>>();
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
    
    public void addTemporaryServer(MedSavantServerInfo server) {
        tmpServer = server;
        saveServers();
    }
    
    public synchronized void saveServers() {
        saveServers(true);
    }
    
    public synchronized void saveServers(boolean notifyListeners) {

        //LOG.info("Serializing " + servers.size() + " servers");
        FileOutputStream fileout = null;
        Writer out = null;
        
        // remove passwords before saving
        List<MedSavantServerInfo> serversWithPasswordsRemoved = new ArrayList<MedSavantServerInfo>();
        for (MedSavantServerInfo s : servers) {
            MedSavantServerInfo clone = new MedSavantServerInfo(s);
            if (!clone.isRememberPassword()) {
                clone.setPassword("");
            }
            //LOG.info("Saved " + clone.getNickname());
            serversWithPasswordsRemoved.add(clone);
        }

        try {
            fileout = new FileOutputStream(getServerFile());
            out = new OutputStreamWriter(fileout,"UTF-8");
            Gson gson = new GsonBuilder().create();
            gson.toJson(serversWithPasswordsRemoved, out);
            out.close();
            fileout.close();

            LOG.info("Saved " + serversWithPasswordsRemoved.size() + " servers");
            
        } catch (Exception ex) {
            LOG.error("Problem saving servers", ex);
            ex.printStackTrace();
            
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

        if (notifyListeners) {
            notifyListeners();
        }
    }

    private synchronized void loadServers() {
        FileInputStream filein = null;
        Reader in = null;
        try {
            // create the server file
            if (!getServerFile().exists()) {
                servers = new ArrayList<MedSavantServerInfo>();
                saveServers();
                // load the server file
            } else {
                filein = new FileInputStream(getServerFile());
                in = new InputStreamReader(filein);
                LOG.info("Deserializing servers");
                try {
                    Gson gson = new GsonBuilder().create();
                    java.lang.reflect.Type type = new TypeToken<List<MedSavantServerInfo>>(){}.getType();
                    servers = gson.fromJson(in, type);

                // happens if the server class changes, nuke the file and start again
                } catch (Exception e) {
                    LOG.info("Corrupted server file, recreating " + e);
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

        servers.remove(server);
        
        if (currentServer == server) {
            setCurrentServer(null);
        }

        saveServers();
    }
    
    public MedSavantServerInfo getServerNamed(String name) {
        for (MedSavantServerInfo s : servers) {
            if (s.getNickname().equals(name)) {
                return s;
            }
        }
        return null;
    }

    public MedSavantServerInfo getCurrentServer() {
        return currentServer;
    }

    public void addListener(Listener<ServerController> serverListener) {
        listeners.add(serverListener);
    }

    public List<MedSavantServerInfo> getServers() {
        return servers;
    }

    public boolean isServerNamed(String name) {
        return getServerNamed(name) != null;
    }
}
