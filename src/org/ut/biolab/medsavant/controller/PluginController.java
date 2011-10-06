/*
 *    Copyright 2010-2011 University of Toronto
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
package org.ut.biolab.medsavant.controller;

import java.awt.BorderLayout;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.plugin.PluginDescriptor;
import org.ut.biolab.medsavant.plugin.PluginIndex;
import org.ut.biolab.medsavant.plugin.PluginVersionException;
import org.ut.biolab.medsavant.plugin.MedSavantSectionPlugin;
import org.ut.biolab.medsavant.plugin.MedSavantPlugin;
import org.ut.biolab.medsavant.settings.BrowserSettings;
import org.ut.biolab.medsavant.settings.DirectorySettings;
import org.ut.biolab.medsavant.settings.NetworkUtils;
import org.ut.biolab.medsavant.util.Controller;
import org.ut.biolab.medsavant.util.IOUtils;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.util.DialogUtils;

/*
import savant.api.util.DialogUtils;
import savant.controller.event.PluginEvent;
import savant.experimental.PluginTool;
import savant.settings.BrowserSettings;
import savant.settings.DirectorySettings;
import savant.util.Controller;
import savant.util.IOUtils;
import savant.util.MiscUtils;
import savant.util.NetworkUtils;
import savant.view.tools.ToolsModule;
 * 
 */

/**
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
            uninstallFile = new File(DirectorySettings.getSavantDirectory(), UNINSTALL_FILENAME);

            LOG.info("Uninstall list " + UNINSTALL_FILENAME);
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
                LOG.warn("No compatible plugins found in " + f);
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
                DialogUtils.displayMessage("Plugins Updated", String.format("<html>The following plugins were updated to be compatible with Savant %s:<br><br><i>%s</i></html>", BrowserSettings.VERSION, MiscUtils.join(updated, ", ")));
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
                    DialogUtils.displayMessage("Plugins Not Loaded", String.format("<html>The following plugins could not be loaded:<br><br><i>%s</i><br><br>They will not be available to Savant.</html>", errorStr));
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
                                LOG.error("Unable to load " + desc.getName(), x);
                                pluginErrors.put(desc.getID(), x.getClass().getName());
                                DialogUtils.displayMessage("Plugin Not Loaded", String.format("<html>The following plugin could not be loaded:<br><br><i>%s – %s</i><br><br>It will not be available to Savant.</html>", desc.getID(), x));
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

    public MedSavantPlugin getPlugin(String id) {
        return loadedPlugins.get(id);
    }

    public void queuePluginForRemoval(String id) {
        FileWriter fstream = null;
        try {
            PluginDescriptor info = knownPlugins.get(id);
            LOG.info("Adding plugin " + info.getFile().getAbsolutePath() + " to uninstall list " + uninstallFile.getPath());

            if (!uninstallFile.exists()) {
                uninstallFile.createNewFile();
            }
            fstream = new FileWriter(uninstallFile, true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(info.getFile().getAbsolutePath() + "\n");
            out.close();

            DialogUtils.displayMessage("Uninstallation Complete", "Please restart Savant for changes to take effect.");
            pluginsToRemove.add(id);

            fireEvent(new PluginEvent(PluginEvent.Type.QUEUED_FOR_REMOVAL, id, null));

        } catch (IOException ex) {
            LOG.error("Error uninstalling plugin: " + uninstallFile, ex);
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

    /**
     * Give a panel plugin an opportunity to initialise itself.
     *
     * @param plugin
     */
    private SectionView initSectionPlugin(MedSavantSectionPlugin plugin) {
        
        JPanel canvas = new JPanel();
        canvas.setLayout(new BorderLayout());
        SectionView view = plugin.getView();
        return view;
    }


    private void deleteFileList(File fileListFile) {
        BufferedReader br = null;
        String line = "";
        try {
            br = new BufferedReader(new FileReader(fileListFile));

            while ((line = br.readLine()) != null) {
                LOG.info("Uninstalling " + line);
                if (!new File(line).delete()) {
                    throw new IOException("Delete of " + line + " failed");
                }
            }
        } catch (IOException ex) {
            LOG.error("Problem uninstalling " + line, ex);
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
        if (MiscUtils.MAC) {
            srcDir = new File(com.apple.eio.FileManager.getPathToApplicationBundle() + "/Contents/Plugins");
            if (srcDir.exists()) {
                try {
                    IOUtils.copyDir(srcDir, destDir);
                    return;
                } catch (Exception ignored) {
                    // We should expect to see this when running in the debugger.
                }
            }
        }
        try {
            srcDir = new File("plugins");
            IOUtils.copyDir(srcDir, destDir);
        } catch (Exception x) {
            LOG.error("Unable to copy builtin plugins from " + srcDir.getAbsolutePath() + " to " + destDir, x);
        }
    }


    private void loadPlugin(PluginDescriptor desc) throws Throwable {
        Class pluginClass = pluginLoader.loadClass(desc.getClassName());
        MedSavantPlugin plugin = (MedSavantPlugin)pluginClass.newInstance();
        plugin.setDescriptor(desc);

        // Init the plugin based on its type
        SectionView view = null;
        if (plugin instanceof MedSavantSectionPlugin) {
            view = initSectionPlugin((MedSavantSectionPlugin)plugin);
        }
        loadedPlugins.put(desc.getID(), plugin);
        fireEvent(new PluginEvent(PluginEvent.Type.LOADED, desc.getID(), view));
    }

    /**
     * Try to add a plugin from the given file.  It is inserted into our internal
     * data structures, but not yet loaded.
     */
    public PluginDescriptor addPlugin(File f) throws PluginVersionException {
        PluginDescriptor desc = PluginDescriptor.fromFile(f);
        if (desc != null) {
            LOG.info("Found usable " + desc + " in " + f.getName());
            PluginDescriptor existingDesc = knownPlugins.get(desc.getID());
            if (existingDesc != null && existingDesc.getVersion().compareTo(desc.getVersion()) >= 0) {
                LOG.info("   Ignored " + desc + " due to presence of existing " + existingDesc);
                return null;
            }
            knownPlugins.put(desc.getID(), desc);
            if (desc.isCompatible()) {
                if (existingDesc != null) {
                    LOG.info("   Replaced " + existingDesc);
                    pluginErrors.remove(desc.getID());
                }
            } else {
                LOG.info("Found incompatible " + desc + " (SDK version " + desc.getSDKVersion() + ") in " + f.getName());
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
        IOUtils.copyFile(selectedFile, pluginFile);
        PluginDescriptor desc = addPlugin(pluginFile);
        pluginLoader.addJar(pluginFile);
        loadPlugin(desc);
    }

    private boolean checkForPluginUpdate(String id) {
        try {
            if (repositoryIndex == null) {
                repositoryIndex = new PluginIndex(BrowserSettings.PLUGIN_URL);
            }
            URL updateURL = repositoryIndex.getPluginURL(id);
            if (updateURL != null) {
                LOG.info("Downloading updated version of " + id + " from " + updateURL);
                addPlugin(NetworkUtils.downloadFile(updateURL, DirectorySettings.getPluginsDirectory(), null));
                return true;
            }
        } catch (IOException x) {
            LOG.error("Unable to install update for " + id, x);
        } catch (PluginVersionException x) {
            LOG.error("Update for " + id + " not loaded.");
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


