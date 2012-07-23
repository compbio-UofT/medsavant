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
package org.ut.biolab.medsavant.plugin;

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

import org.ut.biolab.medsavant.settings.VersionSettings;
import org.ut.biolab.medsavant.settings.DirectorySettings;
import org.ut.biolab.medsavant.util.*;
import org.ut.biolab.medsavant.view.util.DialogUtils;


/**
 * Plugin controller ported over from Savant.
 *
 * @author mfiume, tarkvara
 */
public class PluginController extends Controller {

    private static final Log LOG = LogFactory.getLog(PluginController.class);
    private static final String UNINSTALL_FILENAME = ".uninstall_plugins";

    private static PluginController instance;

    private File uninstallFile;
    private List<String> pluginsToRemove = new ArrayList<String>();
    private Map<String, PluginDescriptor> knownPlugins = new HashMap<String, PluginDescriptor>();
    private Map<String, MedSavantPlugin> loadedPlugins = new HashMap<String, MedSavantPlugin>();
    private Map<String, String> pluginErrors = new LinkedHashMap<String, String>();
    private PluginLoader pluginLoader;
    private PluginIndex repositoryIndex = null;


    /** SINGLETON **/
    public static synchronized PluginController getInstance() {
        if (instance == null) {
            instance = new PluginController();
        }
        return instance;
    }

    /**
     * Private constructor.  Should only be called by getInstance().
     */
    private PluginController() {
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
        File[] files = pluginsDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jar");
            }
        });
        for (File f: files) {
            try {
                addPlugin(f);
            } catch (PluginVersionException x) {
                LOG.warn(String.format("No compatible plugins found in %s.", f));
            }
        }

        // Check to see if we have any outdated plugins.
        if (pluginErrors.size() > 0) {
            List<String> updated = new ArrayList<String>();
            for (String s: pluginErrors.keySet()) {
                // Plugin is invalid, and we don't have a newer version.
                if (checkForPluginUpdate(s)) {
                    updated.add(s);
                }
            }
            if (updated.size() > 0) {
                DialogUtils.displayMessage("Plugins Updated", String.format("<html>The following plugins were updated to be compatible with MedSavant %s:<br><br><i>%s</i></html>", VersionSettings.VERSION, ClientMiscUtils.join(updated, ", ")));
                for (String s: updated) {
                    pluginErrors.remove(s);
                }
            }
            if (pluginErrors.size() > 0) {
                StringBuilder errorStr = null;
                for (String s: pluginErrors.keySet()) {
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
                    DialogUtils.displayMessage("Plugins Not Loaded", String.format("<html>The following plugins could not be loaded:<br><br><i>%s</i><br><br>They will not be available to MedSavant.</html>", errorStr));
                }
            }
        }

        Set<URL> jarURLs = new HashSet<URL>();
        for (PluginDescriptor desc: knownPlugins.values()) {
            try {
                if (!pluginErrors.containsKey(desc.getID())) {
                    jarURLs.add(desc.getFile().toURI().toURL());
                }
            } catch (MalformedURLException ignored) {
            }
        }
        if (jarURLs.size() > 0) {
            pluginLoader = new PluginLoader(jarURLs.toArray(new URL[0]), getClass().getClassLoader());

            for (final PluginDescriptor desc: knownPlugins.values()) {
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

    public List<PluginDescriptor> getDescriptors() {
        List<PluginDescriptor> result = new ArrayList<PluginDescriptor>();
        result.addAll(knownPlugins.values());
        Collections.sort(result);
        return result;
    }

    public List<PluginDescriptor> getDescriptorsOfType(PluginDescriptor.Type t) {
        List<PluginDescriptor> result = new ArrayList<PluginDescriptor>();
        for (PluginDescriptor desc: knownPlugins.values()) {
            if (desc.getType() == t) {
                result.add(desc);
            }
        }
        Collections.sort(result);
        return result;
    }

    public void getGeneManiaData() {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                URL pathToGMData = NetworkUtils.getKnownGoodURL("http://genomesavant.com/serve/data/genemania/gmdata.zip");
                try {
                    File data = RemoteFileCache.getCacheFile(pathToGMData);
                    System.out.println("data is" + data.getAbsolutePath());
                    ZipFile zipData = new ZipFile(data.getAbsolutePath());
                    Enumeration entries = zipData.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = (ZipEntry) entries.nextElement();
                        if (entry.isDirectory()) {
                            (new File(entry.getName())).mkdirs();
                            continue;
                        }
                        System.err.println("Extracting file: " + entry.getName());
                        copyInputStream(zipData.getInputStream(entry),
                                new BufferedOutputStream(new FileOutputStream(entry.getName())));
                    }
                    zipData.close();
                } catch (IOException ex) {
                    Logger.getLogger(PluginController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        Thread t = new Thread (r);
        t.start();
    }
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
    public MedSavantPlugin getPlugin(String id) {
        return loadedPlugins.get(id);
    }

    public void queuePluginForRemoval(String id) {
        FileWriter fstream = null;
        try {
            PluginDescriptor info = knownPlugins.get(id);
            LOG.info(String.format("Adding plugin %s to uninstall list %s.", info.getFile().getAbsolutePath(), uninstallFile.getPath()));

            if (!uninstallFile.exists()) {
                uninstallFile.createNewFile();
            }
            fstream = new FileWriter(uninstallFile, true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(info.getFile().getAbsolutePath() + "\n");
            out.close();

            DialogUtils.displayMessage("Uninstallation Complete", "Please restart MedSavant for changes to take effect.");
            pluginsToRemove.add(id);

            fireEvent(new PluginEvent(PluginEvent.Type.QUEUED_FOR_REMOVAL, id));

        } catch (IOException ex) {
            LOG.error(String.format("Error uninstalling plugin: %s.", uninstallFile), ex);
        } finally {
            try {
                fstream.close();
            } catch (IOException ignored) {
            }
        }
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


    private void loadPlugin(PluginDescriptor desc) throws Throwable {
        LOG.debug(String.format("loadPlugin(\"%s\")", desc.getID()));
        Class pluginClass = pluginLoader.loadClass(desc.getClassName());
        MedSavantPlugin plugin = (MedSavantPlugin)pluginClass.newInstance();
        plugin.setDescriptor(desc);
        loadedPlugins.put(desc.getID(), plugin);
        LOG.debug(String.format("Firing LOADED event to %s listeners.", listeners.size()));
        fireEvent(new PluginEvent(PluginEvent.Type.LOADED, desc.getID()));
    }

    /**
     * Try to add a plugin from the given file.  It is inserted into our internal
     * data structures, but not yet loaded.
     */
    public PluginDescriptor addPlugin(File f) throws PluginVersionException {
        PluginDescriptor desc = PluginDescriptor.fromFile(f);
        if (desc != null) {
            LOG.debug(String.format("Found usable %s in %s.", desc, f.getName()));
            PluginDescriptor existingDesc = knownPlugins.get(desc.getID());
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
                LOG.debug(String.format("Found incompatible %s (SDK version %s) in %s.", desc, desc.getSDKVersion(), f.getName()));
                pluginErrors.put(desc.getID(), "Invalid SDK version (" + desc.getSDKVersion() + ")");
                throw new PluginVersionException("Invalid SDK version (" + desc.getSDKVersion() + ")");
            }
        }
        return desc;
    }

    /**
     * Copy the given file to the plugins directory, add it, and load it.
     * @param selectedFile
     */
    public void installPlugin(File selectedFile) throws Throwable {
        File pluginFile = new File(DirectorySettings.getPluginsDirectory(), selectedFile.getName());
        ClientIOUtils.copyFile(selectedFile, pluginFile);
        PluginDescriptor desc = addPlugin(pluginFile);
        if (desc != null) {
            if (pluginLoader == null) {
                pluginLoader = new PluginLoader(new URL[] { pluginFile.toURI().toURL() }, getClass().getClassLoader());
            }
            pluginLoader.addJar(pluginFile);
            loadPlugin(desc);
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