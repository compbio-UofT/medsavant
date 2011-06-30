/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.view.filter.pathways;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


import org.bridgedb.bio.Organism;
import org.pathvisio.wikipathways.WikiPathwaysClient;
//import org.ut.biolab.medsavant.view.genetics.filter.FilterView;



/**
 *
 * @author AndrewBrook
 */
public class PathwaysTab extends JFrame {

    private JPanel parent;
    private PathwaysBrowser browser;
    private WikiPathwaysClient wpclient;
    private JMenuBar menubar;
    private JMenu fileButton;
    private JMenuItem browseButton;
    private Viewer svgPanel;
    private JPanel mainPanel;
    private JMenuItem findByTextButton;
    private JMenuItem previousSearchButton;
    private JMenuItem getByIdButton;
    private Loader loader;

    private boolean started = false;

    
    public PathwaysTab(){
        super();
        this.setPreferredSize(new Dimension(800,500));
        this.setMinimumSize(new Dimension(400,250));
        this.setSize(new Dimension(800,500));
        this.setLocationRelativeTo(null);
        this.getContentPane().setLayout(new BorderLayout());
        JPanel pane = new JPanel();
        this.getContentPane().add(pane, BorderLayout.CENTER);
        init(pane);
    }
    
    //public static FilterView getWikiPathwaysFilterView(){
    //    return new FilterView("WikiPathways", new PathwaysTab());
    //}
    

    interface ReturnFunction { void run(); }

    //@Override
    public void init(JPanel parent) {

        this.parent = parent;
        parent.setLayout(new GridBagLayout());
        
        //create menubar
        menubar = new JMenuBar();
        fileButton = new JMenu("Find Pathways");

        getByIdButton = new JMenuItem("Get by ID");
        getByIdButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                beginGetById();
            }
        });

        browseButton = new JMenuItem("Browse Pathways");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browse();
            }
        });
        
        findByTextButton = new JMenuItem("Search");
        findByTextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                beginFindByText();
            }
        });

        previousSearchButton = new JMenuItem("Previous Search");
        previousSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                previousSearch();
            }
        });
        previousSearchButton.setEnabled(false);

        fileButton.add(getByIdButton);
        fileButton.add(browseButton);
        fileButton.add(findByTextButton);
        fileButton.add(previousSearchButton);

        menubar.add(fileButton);
        menubar.setMinimumSize(new Dimension(50,21));

        //add menubar
        GridBagConstraints gbc = new GridBagConstraints(); 
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        parent.add(menubar, gbc, 0);

        //add mainPanel
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        //mainPanel.setBackground(Color.red);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 1;
        parent.add(mainPanel, gbc, 0);

        //create loader
        loader = new Loader();
        gbc.gridy = 0;
        mainPanel.add(loader, gbc, 0);
        loader.setVisible(false);

    }

    private void start(final ReturnFunction r){

        loader.setVisible(true);
        loader.setMessage("Initializing connection...");
        parent.validate();

        Thread thread = new Thread() {
            public void run() {

                //create client for WP
                try {
                    wpclient = new WikiPathwaysClient(new URL("http://www.wikipathways.org/wpi/webservice/webservice.php"));
                } catch(Exception e) {
                    System.err.println("COULDNT CREATE CLIENT"); //TODO
                }

                //create panel for viewing svgs
                svgPanel = new Viewer(loader);
                svgPanel.setVisible(false);

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.fill = GridBagConstraints.BOTH;
                gbc.weightx = 1.0;
                gbc.weighty = 1.0;
                gbc.gridx = 0;
                gbc.gridy = 0;
                mainPanel.add(svgPanel, gbc, 0);

                //enable previous search button
                previousSearchButton.setEnabled(true);

                //create pathways browser
                browser = new PathwaysBrowser(wpclient, svgPanel, loader);
                browser.setVisible(false);
                gbc.fill = GridBagConstraints.BOTH;
                gbc.weightx = 1.0;
                gbc.weighty = 1.0;
                gbc.gridx = 0;
                gbc.gridy = 0;
                mainPanel.add(browser, gbc, 0);
                mainPanel.repaint();
                svgPanel.setBrowser(browser);

                //init complete
                started = true;

                //run last called function
                r.run();
            }
        };
        thread.start();
    }

    private void browse(){
        if(!started){
            start(new ReturnFunction() { public void run() { browse(); }});
            return;
        }
        browser.startBrowse();
    }
    
    private void beginFindByText(){
        String message = "<HTML> Find pathways using a textual search on the description and text labels of the pathway objects. The query syntax offers several options:<BR>" +
            " - Combine terms with AND and OR. Combining terms with a space is equal to using OR ('p53 OR apoptosis' gives the same result as 'p53 apoptosis').<BR>" +
            " - Group terms with parentheses, e.g. '(apoptosis OR mapk) AND p53'<BR>" +
            " - You can use wildcards * and ?. * searches for one or more characters, ? searchers for only one character.<BR>" +
            " - Use quotes to escape special characters. E.g. '\"apoptosis*\"' will include the * in the search and not use it as wildcard.</HTML>";

        String[] organisms = Organism.latinNamesArray();
        String[] list = new String[organisms.length+1];
        list[0] = "All";
        System.arraycopy(organisms, 0, list, 1, organisms.length);

        String fields[] = ExtJOptionPane.showInputDialog(message, "Search", list);
        if(fields == null || fields[1] == null || fields[1].equals("")) return;

        findByText(fields);
    }

    private void findByText(final String[] fields){
        if(!started){
            start(new ReturnFunction() { public void run() { findByText(fields); }});
            return;
        }
        browser.startText(fields[1], fields[0]);
    }

    private void previousSearch(){
        if(!browser.hasBeenUsed()){
            browser.startBrowse();
        } else {
            browser.setVisible(true);
            svgPanel.setVisible(false);
            loader.setVisible(false);
        }
    }

    private void beginGetById(){
        String input = JOptionPane.showInputDialog(parent, "<HTML>Enter a WikiPathways ID. <BR>ex. WP100 </HTML>", "TITLE", JOptionPane.QUESTION_MESSAGE);
        if(input != null)
            getById(input);
    }

    private void getById(final String id){
        
        if(!started){
            start(new ReturnFunction() { public void run() { getById(id); }});
            return;
        }
        browser.loadPathway(id.toUpperCase());
    }

    @Override
    public String getTitle() {
        return "WikiPathways";
    }

    
}

class ExtJOptionPane extends JOptionPane {

    public static String[] showInputDialog(final String message, final String title, final String[] list) {
        String[] data = null;

        class GetData extends JDialog implements ActionListener {
            //JTextArea ta = new JTextArea(5,10);
            //JTextField tf1 = new JTextField("");

            JComboBox dropDown = new JComboBox(list);
            JTextField tf2 = new JTextField("");
            JButton btnOK = new JButton("Search");
            JButton btnCancel = new JButton("Cancel");
            String str1 = null;
            String str2 = null;

            public GetData() {
                setModal(true);
                getContentPane().setLayout(new GridBagLayout());
                setDefaultCloseOperation(DISPOSE_ON_CLOSE);              
                setSize(new Dimension(750,300));
                setPreferredSize(new Dimension(750,300));
                setLocationRelativeTo(null);
                this.setTitle(title);

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.fill = GridBagConstraints.BOTH;
                gbc.weightx = 1.0;
                gbc.weighty = 1.0;
                gbc.gridx = 0;
                gbc.gridy = 0;
                JPanel spacer4 = new JPanel();
                spacer4.setPreferredSize(new Dimension(10,10));
                getContentPane().add(spacer4, gbc);

                gbc.gridx = 1;
                gbc.gridy = 1;
                gbc.gridwidth = 3;
                getContentPane().add(new JLabel(message), gbc);

                gbc.gridy = 2;
                JPanel spacer1 = new JPanel();
                spacer1.setPreferredSize(new Dimension(10,10));
                getContentPane().add(spacer1, gbc);

                gbc.gridy = 3;
                gbc.weighty = 0.0;
                gbc.gridwidth = 1;
                gbc.weightx = 0.0;
                getContentPane().add(new JLabel("Organism: "), gbc);
                gbc.gridx = 2;
                gbc.gridwidth = 2;
                gbc.weightx = 1.0;
                getContentPane().add(dropDown, gbc);

                gbc.gridy = 4;
                gbc.weighty = 1.0;
                JPanel spacer2 = new JPanel();
                spacer2.setPreferredSize(new Dimension(5,5));
                getContentPane().add(spacer2, gbc);
                
                gbc.gridy = 5;
                gbc.gridx = 1;
                gbc.gridwidth = 1;
                gbc.weightx = 0.0;
                gbc.weighty = 0.0;
                getContentPane().add(new JLabel("Query: "), gbc);
                gbc.gridx = 2;
                gbc.gridwidth = 2;
                gbc.weightx = 1.0;
                getContentPane().add(tf2, gbc);

                gbc.gridy = 6;
                gbc.weighty = 1.0;
                JPanel spacer3 = new JPanel();
                spacer3.setPreferredSize(new Dimension(10,10));
                getContentPane().add(spacer3, gbc);
                
                btnOK.addActionListener(this);
                btnCancel.addActionListener(this);
                gbc.gridwidth = 1;
                gbc.weighty = 1.0;
                gbc.gridy = 7;
                gbc.gridx = 2;
                getContentPane().add(btnOK, gbc);
                gbc.gridx = 3;
                getContentPane().add(btnCancel, gbc);

                gbc.gridy = 8;
                gbc.gridx = 4;
                gbc.weighty = 1.0;
                JPanel spacer5 = new JPanel();
                spacer5.setPreferredSize(new Dimension(10,10));
                getContentPane().add(spacer5, gbc);

                pack();
                setVisible(true);
            }

            @Override
            public void actionPerformed(ActionEvent ae){
                if(ae.getSource() == btnOK){
                    str1 = (String)dropDown.getSelectedItem();
                    str2 = tf2.getText();
                }
                dispose();
            }

            public String[] getData(){
                return new String[]{str1, str2};
            }
        }
        
        data = new GetData().getData();
        return data;
    }
}