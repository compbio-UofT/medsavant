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
package org.ut.biolab.medsavant.client.plugin;

import org.ut.biolab.medsavant.shared.util.RemoteFileCache;
import org.ut.biolab.medsavant.shared.util.NetworkUtils;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.client.settings.VersionSettings;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.util.ClientIOUtils;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.ClientNetworkUtils;
import org.ut.biolab.medsavant.client.util.Controller;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.shared.serverapi.MedSavantSDKInformation;

/**
 * Plugin controller ported over from Savant.
 *
 * @author mfiume, tarkvara
 */
public class AppController extends Controller {

    private static final Log LOG = LogFactory.getLog(AppController.class);
    private static final String UNINSTALL_FILENAME = ".uninstall_apps";
    private static AppController instance;
    private File uninstallFile;
    private List<String> pluginsToRemove = new ArrayList<String>();
    private Map<String, AppDescriptor> knownPlugins = new HashMap<String, AppDescriptor>();
    private Map<String, MedSavantApp> loadedPlugins = new HashMap<String, MedSavantApp>();
    private Map<String, String> pluginErrors = new LinkedHashMap<String, String>();
    private PluginLoader pluginLoader;
    private PluginIndex repositoryIndex = null;

    /**
     * SINGLETON *
     */
    public static synchronized AppController getInstance() {
        if (instance == null) {
            instance = new AppController();
        }
        return instance;
    }

    /**
     * Private constructor. Should only be called by getInstance().
     */
    private AppController() {
        try {
            uninstallFile = new File(DirectorySettings.getMedSavantDirectory(), UNINSTALL_FILENAME);

            LOG.debug(String.format("Uninstall list %s.", UNINSTALL_FILENAME));
            if (uninstallFile.exists()) {
                deleteFileList(uninstallFile);
            }
            copyBuiltInPlugins();
        } catch (Exception ex) {
            LOG.error("Error loading plugins.", ex);
        }
    }

    /**
     * Try to load all JAR files in the given directory.
     */
    public void loadPlugins(File pluginsDir) {
        LOG.info("Loading plugins in " + pluginsDir.getAbsolutePath());
        File[] files = pluginsDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jar");
            }
        });
        for (File f : files) {
            try {
                addPlugin(f);
            } catch (PluginVersionException x) {
                LOG.warn(String.format("No compatible plugins found in %s.", f));
            }
        }

        // Check to see if we have any outdated plugins.
        if (pluginErrors.size() > 0) {
            List<String> updated = new ArrayList<String>();
            for (String s : pluginErrors.keySet()) {
                // Plugin is invalid, and we don't have a newer version.
                if (checkForPluginUpdate(s)) {
                    updated.add(s);
                }
            }
            if (updated.size() > 0) {
                DialogUtils.displayMessage("Plugins Updated", String.format("<html>The following plugins were updated to be compatible with MedSavant %s:<br><br><i>%s</i></html>", VersionSettings.getVersionString(), ClientMiscUtils.join(updated, ", ")));
                for (String s : updated) {
                    pluginErrors.remove(s);
                }
            }
            if (pluginErrors.size() > 0) {
                StringBuilder errorStr = null;
                for (String s : pluginErrors.keySet()) {
                    if (errorStr == null) {
                        errorStr = new StringBuilder();
                    } else {
                        errorStr.append("<br>");
                    }
                    errorStr.append(s);
                    errorStr.append(" – ");
                    errorStr.append(pluginErrors.get(s));
                }
                if (errorStr != null) {
                    // The following dialog will only report plugins which we can tell are faulty before calling loadPlugin(), typically
                    // by checking the version in plugin.xml.
                    //                  System.out.println("Showing dialog");
//                    JOptionPane.showMessageDialog(null, String.format("<html>The following plugins could not be loaded:<br><br><i>%s</i><br><br>They will not be available to MedSavant.</html>", errorStr),"Plugins Not Loaded", JOptionPane.ERROR_MESSAGE);
                    DialogUtils.displayMessage("Plugins Not Loaded", String.format("<html>The following plugins could not be loaded:<br><br><i>%s</i><br><br>They will not be available to MedSavant.</html>", errorStr));
                }
            }
        }

        Set<URL> jarURLs = new HashSet<URL>();
        for (AppDescriptor desc : knownPlugins.values()) {
            try {
                if (!pluginErrors.containsKey(desc.getID())) {
                    jarURLs.add(desc.getFile().toURI().toURL());
                }
            } catch (MalformedURLException ignored) {
            }
        }
        if (jarURLs.size() > 0) {
            pluginLoader = new PluginLoader(jarURLs.toArray(new URL[0]), getClass().getClassLoader());

            for (final AppDescriptor desc : knownPlugins.values()) {
                if (!pluginErrors.containsKey(desc.getID())) {
                    new Thread("PluginLoader-" + desc) {
                        @Override
                        public void run() {
                            try {
                                loadPlugin(desc);
                            } catch (Throwable x) {
                                LOG.error(String.format("Unable to load %s.", desc.getName()), x);
                                pluginErrors.put(desc.getID(), x.getClass().getName());
                                fireEvent(new PluginEvent(PluginEvent.Type.ERROR, desc.getID()));
                            }
                        }
                    }.start();
                }
            }
        }
    }

    public List<AppDescriptor> getDescriptors() {
        List<AppDescriptor> result = new ArrayList<AppDescriptor>();
        result.addAll(knownPlugins.values());
        Collections.sort(result);
        return result;
    }

    /**
     * @deprecated
     */
    public void getGeneManiaData() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String directoryPath = DirectorySettings.getCacheDirectory().getAbsolutePath();
                if (!(new File(directoryPath + "/done.txt")).exists()) {
                    URL pathToGMData = NetworkUtils.getKnownGoodURL("http://genomesavant.com/serve/data/genemania/gmdata.zip");
                    System.out.println("Downloding GeneMania data from " + pathToGMData.toString());
                    try {
                        if (true) {
                            throw new IOException("Temporarily preventing gm data from downloading. Because it's so large it should only be downloaded once and on demand");
                        }
                        File data = RemoteFileCache.getCacheFile(pathToGMData);
                        System.out.println("data is" + data.getAbsolutePath());
                        ZipFile zipData = new ZipFile(data.getAbsolutePath());
                        Enumeration entries = zipData.entries();
                        while (entries.hasMoreElements()) {
                            ZipEntry entry = (ZipEntry) entries.nextElement();
                            if (entry.isDirectory()) {
                                (new File(directoryPath + "/" + entry.getName())).mkdirs();
                                continue;
                            }
                            //System.err.println("Extracting file: " + entry.getName());
                            copyInputStream(zipData.getInputStream(entry),
                                    new BufferedOutputStream(new FileOutputStream(directoryPath + "/" + entry.getName())));
                        }
                        zipData.close();
                        FileWriter fstream = new FileWriter(directoryPath + "/done.txt");
                        BufferedWriter out = new BufferedWriter(fstream);
                        out.write("This file indicates that the GeneMANIA data has finished downloading.");
                        out.close();
                    } catch (IOException ex) {
                        Logger.getLogger(AppController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    /**
     * @deprecated
     */
    private static final void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
    }

    public MedSavantApp getPlugin(String id) {
        return loadedPlugins.get(id);
    }

    public List<MedSavantApp> getPluginsOfClass(Class c) {
        List<MedSavantApp> results = new ArrayList<MedSavantApp>();
        for (AppDescriptor ad : this.getDescriptors()) {
            MedSavantApp appInstance = getPlugin(ad.getID());
            if (c.isInstance(appInstance)) {
                results.add(appInstance);
            }
        }
        LOG.info(results.size() + " apps of class " + c.getSimpleName());
        return results;
    }

    public boolean queuePluginForRemoval(String id) {
        FileWriter fstream = null;
        boolean success = false;
        try {
            AppDescriptor info = knownPlugins.get(id);
            LOG.info(String.format("Adding plugin %s to uninstall list %s.", info.getFile().getAbsolutePath(), uninstallFile.getPath()));

            if (!uninstallFile.exists()) {
                uninstallFile.createNewFile();
            }

            // append to the remove file
            fstream = new FileWriter(uninstallFile, true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(info.getFile().getAbsolutePath() + "\n");
            out.close();

            pluginsToRemove.add(id);

            fireEvent(new PluginEvent(PluginEvent.Type.QUEUED_FOR_REMOVAL, id));

            success = true;
        } catch (IOException ex) {
            LOG.error(String.format("Error uninstalling plugin: %s.", uninstallFile), ex);
        } finally {
            try {
                fstream.close();
            } catch (IOException ignored) {
            }
        }

        return success;
    }

    public boolean isPluginQueuedForRemoval(String id) {
        return pluginsToRemove.contains(id);
    }

    public String getPluginStatus(String id) {
        if (pluginsToRemove.contains(id)) {
            return "Queued for removal";
        }
        if (loadedPlugins.get(id) != null) {
            return "Loaded";
        }
        String err = pluginErrors.get(id);
        if (err != null) {
            return err;
        }
        if (knownPlugins.get(id) != null) {
            // Plugin is valid, but hasn't shown up in the loadedPlugins map.
            return "Loading";
        }
        return "Unknown";
    }

    private void deleteFileList(File fileListFile) {
        BufferedReader br = null;
        String line = "";
        try {
            br = new BufferedReader(new FileReader(fileListFile));

            while ((line = br.readLine()) != null) {
                LOG.info(String.format("Uninstalling %s.", line));
                if (!new File(line).delete()) {
                    throw new IOException("Delete of " + line + " failed");
                }
            }
        } catch (IOException ex) {
            LOG.error(String.format("Problem uninstalling %s.", line), ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
            }
        }
        fileListFile.delete();
    }

    private void copyBuiltInPlugins() {
        File destDir = DirectorySettings.getPluginsDirectory();
        File srcDir = null;
        if (ClientMiscUtils.MAC) {
            srcDir = new File(com.apple.eio.FileManager.getPathToApplicationBundle() + "/Contents/Plugins");
            if (srcDir.exists()) {
                try {
                    ClientIOUtils.copyDir(srcDir, destDir);
                    return;
                } catch (Exception ignored) {
                    // We should expect to see this when running in the debugger.
                }
            }
        }
        try {
            srcDir = new File("plugins");
            ClientIOUtils.copyDir(srcDir, destDir);
        } catch (Exception x) {
            LOG.error(String.format("Unable to copy builtin plugins from %s to %s.", srcDir.getAbsolutePath(), destDir), x);
        }
    }

    private void loadPlugin(AppDescriptor desc) throws Throwable {
        LOG.debug(String.format("loadPlugin(\"%s\")", desc.getID()));
        Class pluginClass = pluginLoader.loadClass(desc.getClassName());
        MedSavantApp plugin = (MedSavantApp) pluginClass.newInstance();
        plugin.setDescriptor(desc);
        loadedPlugins.put(desc.getID(), plugin);
        LOG.debug(String.format("Firing LOADED event to %s listeners.", listeners.size()));
        fireEvent(new PluginEvent(PluginEvent.Type.LOADED, desc.getID()));
    }

    /**
     * Try to add a plugin from the given file. It is inserted into our internal
     * data structures, but not yet loaded.
     */
    public AppDescriptor addPlugin(File f) throws PluginVersionException {
        LOG.info(String.format("Loading plugin from %s", f.getAbsolutePath()));
        AppDescriptor desc = AppDescriptor.fromFile(f);
        if (desc != null) {
            LOG.debug(String.format("Found usable %s in %s.", desc, f.getName()));
            AppDescriptor existingDesc = knownPlugins.get(desc.getID());
            if (existingDesc != null && existingDesc.getVersion().compareTo(desc.getVersion()) >= 0) {
                LOG.debug(String.format("   Ignored %s due to presence of existing %s.", desc, existingDesc));
                return null;
            }
            knownPlugins.put(desc.getID(), desc);
            if (desc.isCompatible()) {
                if (existingDesc != null) {
                    LOG.debug(String.format("   Replaced %s.", existingDesc));
                    pluginErrors.remove(desc.getID());
                }
            } else {
                LOG.info(String.format("Found incompatible %s (SDK version %s) in %s.", desc, desc.getSDKVersion(), f.getName()));
                pluginErrors.put(desc.getID(), "Invalid SDK version (" + desc.getSDKVersion() + " vs " + MedSavantSDKInformation.getSDKVersion() + ")");
                throw new PluginVersionException("Invalid SDK version (" + desc.getSDKVersion() + " vs " + MedSavantSDKInformation.getSDKVersion() + ")");
            }
        }
        return desc;
    }

    /**
     * Copy the given file to the plugins directory, add it, and load it.
     *
     * @param selectedFile
     */
    public void installPlugin(File selectedFile) throws Throwable {
        File pluginFile = new File(DirectorySettings.getPluginsDirectory(), selectedFile.getName());
        LOG.info("Copying file " + selectedFile.getAbsolutePath() + " to " + pluginFile.getAbsolutePath());
        ClientIOUtils.copyFile(selectedFile, pluginFile);
        LOG.info("Getting plugin information...");
        AppDescriptor desc = addPlugin(pluginFile);
        LOG.info("Got plugin information");
        if (desc != null) {
            LOG.info("Loading plugin...");
            if (pluginLoader == null) {
                pluginLoader = new PluginLoader(new URL[]{pluginFile.toURI().toURL()}, getClass().getClassLoader());
            }
            pluginLoader.addJar(pluginFile);
            loadPlugin(desc);
            LOG.info("Done loading plugin");
        }
    }

    private boolean checkForPluginUpdate(String id) {
        try {
            if (repositoryIndex == null) {
                repositoryIndex = new PluginIndex(VersionSettings.PLUGIN_URL);
            }
            URL updateURL = repositoryIndex.getPluginURL(id);
            if (updateURL != null) {
                LOG.debug(String.format("Downloading updated version of %s from %s.", id, updateURL));
                addPlugin(ClientNetworkUtils.downloadFile(updateURL, DirectorySettings.getPluginsDirectory(), null));
                return true;
            }
        } catch (IOException x) {
            LOG.error(String.format("Unable to install update for %s.", id), x);
        } catch (PluginVersionException x) {
            LOG.error(String.format("Update for %s not loaded.", id));
        }
        return false;


    }

    class PluginLoader extends URLClassLoader {

        PluginLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        void addJar(File f) {
            try {
                addURL(f.toURI().toURL());
            } catch (MalformedURLException ignored) {
            }
        }
    }
}