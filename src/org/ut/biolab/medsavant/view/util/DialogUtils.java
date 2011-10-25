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

package org.ut.biolab.medsavant.view.util;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import com.jidesoft.dialog.JideOptionPane;

import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.MainFrame;


/**
 * Some utility methods for displaying message dialogs to the user.
 *
 * @author vwilliams, tarkvara
 */
public class DialogUtils {
    /**
     * Same as JOptionPane.YES_OPTION.
     */
    public static final int YES = 0;

    /**
     * Same as JOptionPane.OK_OPTION.
     */
    public static final int OK = 0;

    /**
     * Same as JOptionPane.NO_OPTION.
     */
    public static final int NO = 1;

    /**
     * Same as JOptionPane.CANCEL_OPTION.
     */
    public static final int CANCEL = 2;


    public static int askYesNo(String title, String prompt) {
        return JOptionPane.showConfirmDialog(MainFrame.getInstance(), prompt, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    public static int askYesNoCancel(String title, String prompt) {
        return JOptionPane.showConfirmDialog(MainFrame.getInstance(), prompt, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    public static void displayError(String title, String message) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(), message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void displayMessage(String title, String message) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(), message, title, JOptionPane.PLAIN_MESSAGE);
    }

    public static String displayInputMessage(String title, String message, String defaultInput) {
        String result = JOptionPane.showInputDialog(MainFrame.getInstance(), message, title, JOptionPane.QUESTION_MESSAGE);
        if (result != null && result.length() > 0) {
            return result;
        }
        return null;
    }

    public static void displayException(final String title, final String message, final Throwable t) {
        MiscUtils.invokeLaterIfNecessary(new Runnable() {
            @Override
            public void run() {
                String msg = message;
                if (t.getCause() != null) {
                    msg += "\r\nCause: " + MiscUtils.getMessage(t.getCause()) + ".";
                }
                JideOptionPane optionPane = new JideOptionPane(msg, JOptionPane.ERROR_MESSAGE, JideOptionPane.CLOSE_OPTION);
                optionPane.setTitle(title);
                optionPane.setOptions(new String[] {});
                JButton reportButton = new JButton("Report Issue");
                ((JComponent) optionPane.getComponent(optionPane.getComponentCount()-1)).add(reportButton);
                final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(),"Error encountered");
                dialog.setResizable(true);
                String details = t.getMessage() + "\r\n" + MiscUtils.getStackTrace(t);
                optionPane.setDetails(details);
                dialog.pack();

                reportButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e2) {
                    String issue = "Hey MedSavant Developers,\n\n";
                    issue += "I am encountering an error in MedSavant. I have provided additional diagnostic information below.\n\n";

                    issue += "=== DESCRIBE THE ISSUE BELOW ===\n\n\n";


                    issue += "=== ERROR DETAILS ===\n";
                    issue += MiscUtils.getStackTrace(t);

                    dialog.dispose();
                    (new BugReportDialog(MainFrame.getInstance(), issue, null)).setVisible(true);
                }

                });

                dialog.setVisible(true);
            }
        });
    }
    
       /**
     * Display a Savant message dialog with the given message and the title "Savant".
     *
     * @param message the message to be displayed
     */
    public static void displayMessage(String message) {
        displayMessage("MedSavant", message);
    }

    /**
     * Open-dialog variant which lets user select a single file.
     *
     * @param parent the parent frame (typically the Savant main frame)
     * @param title title for the dialog
     * @param filter controls which files to display (null for no filtering)
     * @param initialDir initial directory for the dialog
     * @return the selected file, or null if nothing was selected
     */
    public static File chooseFileForOpen(Frame parent, String title, FileFilter filter, File initialDir) {
        if (MiscUtils.MAC) {
            FileDialog fd = new FileDialog(parent, title, FileDialog.LOAD);
            if (filter != null) {
                fd.setFilenameFilter(new FilenameFilterAdapter(filter));
            }
            if (initialDir != null) {
                fd.setDirectory(initialDir.getAbsolutePath());
            }
            fd.setVisible(true);
            fd.setAlwaysOnTop(true);
            String selectedFileName = fd.getFile();
            if (selectedFileName != null) {
                return new File(fd.getDirectory(), selectedFileName);
            }
        } else {
            JFileChooser fd = new JFileChooser();
            fd.setDialogTitle(title);
            fd.setDialogType(JFileChooser.OPEN_DIALOG);
            if (filter != null) {
                fd.setFileFilter(filter);
            }
            if (initialDir != null) {
                fd.setCurrentDirectory(initialDir);
            }
            int result = fd.showOpenDialog(parent);
            if (result == JFileChooser.APPROVE_OPTION) {
                return fd.getSelectedFile();
            }
        }
        return null;
    }

    /**
     * Open-file dialog variant which lets user select multiple files on Windows and
     * Linux.
     * 
     * @param parent the parent frame (typically the Savant main frame)
     * @param title title for the dialog
     * @param filter controls which files to display (null for no filtering)
     * @param initialDir initial directory for the dialog
     * @return an array of selected files; an empty array if nothing is selected
     */
    public static File[] chooseFilesForOpen(Frame parent, String title, FileFilter filter, File initialDir) {
        
        
        // unfortunately, we need function over aesthetics... 
        /*
        if (MiscUtils.MAC) {
            // Mac AWT FileDialog doesn't support multiple selection.
            File[] files = chooseFilesForOpen( parent,  title,  filter,  initialDir);
            if (files != null) {
                return files;
            }
        } else {
         * 
         */
            JFileChooser fd = new JFileChooser();
            fd.setDialogTitle(title);
            fd.setSelectedFile(initialDir);
            fd.setDialogType(JFileChooser.OPEN_DIALOG);
            if (filter != null) {
                fd.setFileFilter(filter);
            }
            fd.setMultiSelectionEnabled(true);
            int result = fd.showOpenDialog(parent);
            if (result == JFileChooser.APPROVE_OPTION) {
                return fd.getSelectedFiles();
            }
        //}
        return new File[] {};
    }

    /**
     * Prompt the user to save a file.
     *
     * @param parent window which will serve as the parent for this dialog
     * @param title title of the dialog
     * @param defaultName default file-name to appear in the dialog
     * @return a File, or null if cancelled
     */
    public static File chooseFileForSave(Frame parent, String title, String defaultName) {
        return chooseFileForSave(parent, title, defaultName, null, null);
    }

    /**
     * Prompt the user to save a file.
     *
     * @param parent window which will serve as the parent for this dialog
     * @param title title of the dialog
     * @param defaultName default file-name to appear in the dialog
     * @param filter file-filter for controlling what appears in the dialog
     * @param initialDir initial directory for the dialog
     * @return a File, or null if cancelled
     */
    public static File chooseFileForSave(Frame parent, String title, String defaultName, FileFilter filter, File initialDir) {
        FileDialog fd = new FileDialog(parent, title, FileDialog.SAVE);
        if (filter != null) {
            fd.setFilenameFilter(new FilenameFilterAdapter(filter));
        }
        if (initialDir != null) {
            fd.setDirectory(initialDir.getAbsolutePath());
        }
        fd.setFile(defaultName);
        fd.setAlwaysOnTop(true);
        fd.setVisible(true);
        String selectedFile = fd.getFile();
        if (selectedFile != null) {
            return new File(fd.getDirectory(), selectedFile);
        }
        return null;
    }

    /**
     * Display a Savant error dialog with the given message and the title "Savant Error".
     *
     * @param message the message to be displayed
     */
    public static void displayError(String message) {
        displayError("MedSavant Error", message);
    }
    /**
     * Little class so that caller can pass us a FileFilter and will still be able
     * to use it with a Mac FileDialog.
     */
    static class FilenameFilterAdapter implements FilenameFilter {
        FileFilter filter;

        FilenameFilterAdapter(FileFilter f) {
            filter = f;
        }

        @Override
        public boolean accept(File dir, String name) {
            return filter.accept(new File(dir, name));
        }
    }
    
        public static boolean confirmChangeReference(boolean isChangingProject){
        int result = JOptionPane.showConfirmDialog(
                null, 
                "<HTML>Changing the " + (isChangingProject ? "project" : "reference") + " will remove current filters.<BR>Are you sure you want to do this?</HTML>", 
                "Confirm", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
        
    public static void displayErrorMessage(String msg, Exception ex) {
        JOptionPane p = new JOptionPane(msg, JOptionPane.ERROR_MESSAGE);
        p.setVisible(true);
    }
    
    /**
     * For purposes of parenting dialogs, here's the MedSavant main window.
     */
    public static Window getMainWindow() {
        return MainFrame.getInstance();
    }
}
