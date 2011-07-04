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

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.table.TableSchema;
import org.ut.biolab.medsavant.db.table.VariantTableSchema;
//import savant.controller.LocationController;
//import savant.util.Range;

/**
 *
 * @author AndrewBrook
 */
public class Viewer extends JSplitPane {

    private Loader loader;
    private PathwaysBrowser browser;

    private JScrollPane scrollPane;
    private JScrollPane infoScroll;
    private JPanel infoPanel;
    private JLabel infoLabel;
    private JScrollPane treeScroll;
    private JSplitPane rightPanel;
    private JTree dataTree;
    //private JLabel jumpLocationButton;
    private JLabel linkOutButton;
    private JLabel jumpPathwayButton;
    private ExtendedJSVGCanvas svgCanvas;
    private Document gpmlDoc;
    private Node pathway;
    //private NodeList comments;
    //private NodeList dataNodes;
    //private NodeList lines;
    private ArrayList<DataNode> dataNodes = new ArrayList<DataNode>();
    private ArrayList<Rectangle> recs = new ArrayList<Rectangle>();
    private String version;

    private Gene jumpGene;
    private String jumpPathway;
    private String linkOutUrl;

    private Point start;
    private int initialVerticalScroll = 0;
    private int initialHorizontalScroll = 0;
    
    private boolean hasPathway = false;
    private PathwaysPanel pathwaysPanel;
    private String pathwayString;

    Viewer(Loader loader, PathwaysPanel pp) {

        this.pathwaysPanel = pp;
        
        this.loader = loader;

        this.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        this.setBackground(Color.white);

        //scrollPane
        scrollPane = new JScrollPane();
        scrollPane.setMinimumSize(new Dimension(200,50));
        scrollPane.setPreferredSize(new Dimension(10000,10000));
        scrollPane.getViewport().setBackground(Color.white);
        this.setLeftComponent(scrollPane);

        //svgCanvas
        svgCanvas = new ExtendedJSVGCanvas();
        svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
        scrollPane.getViewport().add(svgCanvas);

        //treeScroll
        treeScroll = new JScrollPane();
        treeScroll.getViewport().setLayout(new FlowLayout(FlowLayout.LEFT));
        treeScroll.getViewport().setBackground(Color.white);
        treeScroll.setMinimumSize(new Dimension(200,200));

        //infoScroll
        infoScroll = new JScrollPane();
        infoScroll.setBackground(Color.white);
        infoScroll.getViewport().setBackground(Color.white);
        infoScroll.setMaximumSize(new Dimension(100,100));
        infoScroll.setPreferredSize(new Dimension(100,100));

        //infoPanel
        infoPanel = new JPanel();
        infoPanel.setLayout(new GridBagLayout());
        infoPanel.setBackground(Color.white);
        infoScroll.getViewport().add(infoPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        Border buttonBorder = BorderFactory.createEmptyBorder(3,5,0,5);

        //jumpLocationButton
        /*jumpLocationButton = new JLabel("<HTML><B>Jump to Gene Location</B></HTML>");
        jumpLocationButton.setForeground(Color.BLUE);
        jumpLocationButton.setBackground(Color.WHITE);
        jumpLocationButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        jumpLocationButton.setMaximumSize(new Dimension(25, 200));    
        jumpLocationButton.setBorder(BorderFactory.createCompoundBorder(buttonBorder,buttonBorder));
        jumpLocationButton.setVisible(false);
        jumpLocationButton.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                jumpToGene();
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });
        infoPanel.add(jumpLocationButton, gbc);*/

        //jumpPathwayButton
        jumpPathwayButton = new JLabel("<HTML><B>Jump to Pathway</B></HTML>");
        jumpPathwayButton.setForeground(Color.BLUE);
        jumpPathwayButton.setBackground(Color.WHITE);
        jumpPathwayButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        jumpPathwayButton.setMaximumSize(new Dimension(25, 200));
        jumpPathwayButton.setBorder(BorderFactory.createCompoundBorder(buttonBorder,buttonBorder));
        jumpPathwayButton.setVisible(false);
        jumpPathwayButton.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                jumpToPathway();
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });
        gbc.gridy = 1;
        infoPanel.add(jumpPathwayButton, gbc);
        
        //linkOutButton
        linkOutButton = new JLabel("<HTML><B>Link to Web Page</B></HTML>");
        linkOutButton.setForeground(Color.BLUE);
        linkOutButton.setBackground(Color.WHITE);
        linkOutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkOutButton.setMaximumSize(new Dimension(25, 200));
        linkOutButton.setBorder(BorderFactory.createCompoundBorder(buttonBorder,buttonBorder));
        linkOutButton.setVisible(false);
        linkOutButton.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                linkOut();
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });
        gbc.gridy = 2;
        infoPanel.add(linkOutButton, gbc);
    
        //infoLabel
        infoLabel = new JLabel();
        infoLabel.setBackground(Color.white);
        Border paddingBorder = BorderFactory.createEmptyBorder(5,5,5,5);
        infoLabel.setBorder(BorderFactory.createCompoundBorder(paddingBorder,paddingBorder));
        gbc.gridy = 3;
        infoPanel.add(infoLabel, gbc);

        //filler
        JPanel filler = new JPanel();
        filler.setBackground(Color.white);
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        infoPanel.add(filler, gbc);
        
        //rightPanel
        rightPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treeScroll, infoScroll);
        rightPanel.setMinimumSize(new Dimension(200,20));
        this.setRightComponent(rightPanel);
        
        this.setDividerLocation(0.8);

        svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
            @Override
            public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
                revalidate();
                setDividerLocation(0.8);
            }
        });

        svgCanvas.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tryClick(e.getPoint());
            }
            public void mousePressed(MouseEvent e) {
                start = e.getLocationOnScreen();
                initialVerticalScroll = scrollPane.getVerticalScrollBar().getValue();
                initialHorizontalScroll = scrollPane.getHorizontalScrollBar().getValue();
            }
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });

        svgCanvas.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = (int)(e.getLocationOnScreen().getX() - start.getX());
                int y = (int)(e.getLocationOnScreen().getY() - start.getY());
                int newVert = Math.max(Math.min(scrollPane.getVerticalScrollBar().getMaximum(), initialVerticalScroll - y), 0);
                int newHor = Math.max(Math.min(scrollPane.getHorizontalScrollBar().getMaximum(), initialHorizontalScroll - x), 0);
                scrollPane.getVerticalScrollBar().setValue(newVert);
                scrollPane.getHorizontalScrollBar().setValue(newHor);
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                svgCanvas.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        });
    }

    //must always be set
    public void setBrowser(PathwaysBrowser browser){
        this.browser = browser;
    }

    public void setPathway(URI svgUri, URI gpmlUri, String pathway) {
        
        this.pathwayString = pathway;
        
        hasPathway = false;
        pathwaysPanel.enableApply(false);
        
        jumpGene = null;
        jumpPathway = null;
        linkOutUrl = null;
        clearInfo();

        svgCanvas.setURI(svgUri.toString());      
        getGPML(gpmlUri);
        getGeneInfo();
        //applyFilter();
        
        hasPathway = true;
        pathwaysPanel.enableApply(true);      
        pathwaysPanel.setCurrentFilter(pathwayString);
    }
    
    public void applyFilter(){
        
        if(!hasPathway) return;
        
        loader.setVisible(true);
        loader.setMessage("Applying Filter");
        
        final ArrayList<Gene> genes = new ArrayList<Gene>();      
        for(DataNode n : dataNodes){
            if(n.hasGene()){
                genes.add(n.getGene());
            }
        }
        
        TableSchema table = MedSavantDatabase.getInstance().getVariantTableSchema();
        final DbColumn positionCol = table.getDBColumn(VariantTableSchema.ALIAS_POSITION);
        final DbColumn chromCol = table.getDBColumn(VariantTableSchema.ALIAS_CHROM);
    
        Filter f = new QueryFilter() {

            @Override
            public Condition[] getConditions() {               
                Condition[] results = new Condition[genes.size()];
                int i = 0;               
                for(Gene g : genes){                    
                    Condition[] current = new Condition[3];
                    current[0] = BinaryCondition.equalTo(chromCol, "chr" + g.getChromosome());
                    current[1] = BinaryCondition.greaterThan(positionCol, Math.min(g.getStart(), g.getEnd()), true);
                    current[2] = BinaryCondition.lessThan(positionCol, Math.max(g.getStart(), g.getEnd()), true);                                   
                    results[i++] = ComboCondition.and(current);
                }
                return results;
            }

            @Override
            public String getName() {
                return "WikiPathways";
            }
        };
        System.out.println("Adding filter: " + f.getName());
        FilterController.addFilter(f);
        
        
        pathwaysPanel.setAppliedFilter(pathwayString);
        loader.setVisible(false);
        
    }

    private void getGeneInfo(){

        loader.setMessage("Getting gene information");

        ArrayList<DataNode> entrezNodes = new ArrayList<DataNode>();

        //determine url
        String urlString = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=gene&id=";
        for(DataNode n : dataNodes){
            String db = n.getAttribute("Xref", "Database");
            String id = n.getAttribute("Xref", "ID");
            if(db == null || id == null) continue;
            if(db.equals("Entrez Gene")){
                urlString += id + ",";
                entrezNodes.add(n);
            } else if (db.equals("Ensembl") || db.equals("Ensembl Human")){


                //FIXME: this is a massive hack...is there a better way?
                //START ENSEMBL LOOKUP//////////////////////////////////////////

                boolean success = false;
                int retries = 5;
                String ensemblUrlString = "http://www.ensembl.org/Gene/Summary?g=" + id;
                String rangeString = "";

                while(!success && retries > 0){
                    try {
                        URL url = new URL(ensemblUrlString);
                        HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
                        httpConnection.setInstanceFollowRedirects(false);
                        httpConnection.connect();
                        int responseCode = httpConnection.getResponseCode();
                        String header = httpConnection.getHeaderField("Location");
                        if(header.contains(";r=")){
                            success = true;
                            rangeString = header.substring(header.indexOf(";r=")+3);
                            if(rangeString.contains(";")){
                                rangeString = rangeString.substring(0, rangeString.indexOf(";"));
                            }
                        } else {
                            if(header.startsWith("http://")){
                                ensemblUrlString = header;
                            } else if (header.startsWith("/")){
                                ensemblUrlString = url.getProtocol() + "://" + url.getHost() + header;
                            } else {
                                retries = 0;
                            }
                            
                        }

                    } catch (MalformedURLException ex) {
                        Logger.getLogger(Viewer.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex){
                        Logger.getLogger(Viewer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    retries--;
                }

                if(success){
                    String chrom = rangeString.substring(0, rangeString.indexOf(":"));
                    String startRange = rangeString.substring(rangeString.indexOf(":")+1, rangeString.indexOf("-"));
                    String endRange = rangeString.substring(rangeString.indexOf("-")+1, rangeString.length());
                    n.setEnsemblGeneInfo(id, chrom, startRange, endRange);
                }

                //END ENSEMBL LOOKUP////////////////////////////////////////////


            }
        }
        urlString += "&retmode=xml";
        if(entrezNodes.isEmpty()) return;
        
        //get xml string
        BufferedReader reader;
        String xmlString = "";
        try {
            reader = new BufferedReader(new InputStreamReader(new URL(urlString).openStream()));
            String line = reader.readLine();
            while (line != null) {
                if(!line.startsWith("<?xml") && !line.startsWith("<!DOCTYPE")){
                    xmlString += line;
                }
                line = reader.readLine();
            }
        } catch (MalformedURLException e) {
            //todo
            return;
        } catch (IOException e) {
             //todo
            return;
        }
	
        //create document
        Element root = null;
        try {
            root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xmlString.getBytes())).getDocumentElement();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Viewer.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (SAXException ex) {
            Logger.getLogger(Viewer.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (IOException ex) {
            Logger.getLogger(Viewer.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        //TODO: can we really make assumptions about ordering of xml?
        NodeList docSumList = root.getElementsByTagName("DocSum");
        for(int i = 0; i < docSumList.getLength(); i++){
            entrezNodes.get(i).setEntrezGeneInfo((Element)docSumList.item(i));
        }

    }

    private void getGPML(URI uri) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            gpmlDoc = db.parse(uri.toString());
        } catch (ParserConfigurationException pce) {
            System.out.println("ERROR");
            //TODO
        } catch (SAXException se) {
            System.out.println("ERROR");
            //TODO
        } catch (IOException ioe) {
            System.out.println("ERROR");
            //TODO
        }

        parseGPML();
        generateLinks();
        createTree();
    }

    private void parseGPML() {

        gpmlDoc.getDocumentElement().normalize();

        NodeList nodes = gpmlDoc.getElementsByTagName("Pathway");
        pathway = nodes.item(0);

        version = ((Element) pathway).getAttribute("xmlns");
        if(version == null || version.equals("")){
            version = "unknown";
        } else if (version.indexOf("2008") != -1){
            version = "2008";
        } else if (version.indexOf("2010") != -1){
            version = "2010";
        } else {
            version = "unknown";
        }

        //comments = ((Element) pathway).getElementsByTagName("Comment");
        dataNodes.clear();
        NodeList dataNodeList = ((Element) pathway).getElementsByTagName("DataNode");
        for(int i = 0; i < dataNodeList.getLength(); i++){
            dataNodes.add(new DataNode((Element) (dataNodeList.item(i))));
        }
        //lines = ((Element) pathway).getElementsByTagName("Line");
    }

    private void generateLinks() {

        double scale = 1.0;
        if(version.equals("2008")){
            scale = 0.0667;
        }

        recs.clear();

        for(int i = 0; i < dataNodes.size(); i++){
            
            DataNode node = dataNodes.get(i);
            if(!node.hasSubNode("Graphics")){
                recs.add(null);
                continue;
            }

            float centerX = Float.valueOf(node.getAttribute("Graphics", "CenterX"));
            float centerY = Float.valueOf(node.getAttribute("Graphics", "CenterY"));
            float width = Float.valueOf(node.getAttribute("Graphics", "Width"));
            float height = Float.valueOf(node.getAttribute("Graphics", "Height"));
            recs.add(new Rectangle((int) (scale *(centerX - width / 2.0)),
                    (int) (scale *(centerY - height / 2.0)),
                    (int) (scale * width),
                    (int) (scale * height)));
        }
    }

    private void tryClick(Point p) {
        int i = searchShapes(p);
        if (i == -1) return;
        fillInfo(dataNodes.get(i));     
    }

    private void clearInfo(){
        //jumpLocationButton.setVisible(false);
        jumpPathwayButton.setVisible(false);
        linkOutButton.setVisible(false);
        infoLabel.setText("");
        rightPanel.revalidate();
    }

    private void fillInfo(DataNode dataNode){

        /*if(dataNode.hasGene()){
            jumpLocationButton.setVisible(true);
            jumpGene = dataNode.getGene();
        } else {
            jumpLocationButton.setVisible(false);
            jumpGene = null;
        }*/

        if(dataNode.hasWikiPathway()){
            jumpPathwayButton.setVisible(true);
            jumpPathway = dataNode.getWikiPathway();
        } else {
            jumpPathwayButton.setVisible(false);
            jumpPathway = null;
        }

        linkOutUrl = dataNode.getLinkOut();
        linkOutButton.setVisible(linkOutUrl != null);
              
        infoLabel.setText(dataNode.getInfoString());
        rightPanel.revalidate();
    }

    private void createTree(){

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("dataTree");

        Map<String, ArrayList<DataNode>> treeMap = new HashMap<String, ArrayList<DataNode>>();
        for(DataNode n : dataNodes){
            String type = n.getType();
            if(treeMap.get(type) == null){
                treeMap.put(type, new ArrayList<DataNode>());
            }
            treeMap.get(type).add(n);
        }
        Iterator it = treeMap.keySet().iterator();
        while(it.hasNext()){
            String key = (String)it.next();
            ArrayList<DataNode> list = treeMap.get(key);
            Collections.sort(list);

            DefaultMutableTreeNode node = new DefaultMutableTreeNode(key);
            for(DataNode n : list){
                node.add(new DefaultMutableTreeNode(n));
            }
            root.add(node);
        }

        dataTree = new JTree(root);
        dataTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        dataTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int selRow = dataTree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = dataTree.getPathForLocation(e.getX(), e.getY());
                if(selRow != -1) {
                    if(e.getClickCount() == 2) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode)selPath.getLastPathComponent();
                        if (node == null) return;
                        if (node.isLeaf()) {
                            DataNode dataNode = (DataNode)node.getUserObject();
                            fillInfo(dataNode);
                        }
                    }
                }
            }
        });

        dataTree.setRootVisible(false);
        treeScroll.getViewport().removeAll();
        treeScroll.getViewport().add(dataTree);

       // rightPanel.setDividerLocation(0.5);
    }

    private void jumpToGene(){
        if(jumpGene == null) return;
        int startGene = jumpGene.getStart();
        int endGene = jumpGene.getEnd();
        if(startGene > endGene){
            int temp = startGene;
            startGene = endGene;
            endGene = temp;
        }
  
        //TODO: what if references don't start with "chr"?
        //TODO: change to plugin api
        //if(LocationController.getInstance().isGenomeLoaded()){
        //    LocationController.getInstance().setLocation("chr" + jumpGene.getChromosome(), new Range(startGene, endGene));
        //}
    }

    private void jumpToPathway(){
        if(jumpPathway == null) return;
        browser.loadPathway(jumpPathway);       
    }

    private void linkOut(){

        if(linkOutUrl == null) return;

        if(!java.awt.Desktop.isDesktopSupported()){
            JOptionPane.showMessageDialog(this, "<HTML>This operation is not supported by your computer.<BR>Web page: " + linkOutUrl + "</HTML>");
            return;
        }

        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        try {
            java.net.URI uri = new java.net.URI(linkOutUrl);
            desktop.browse(uri);
        }
        catch ( Exception e ) {
            //TODO: something
        }
    }

    private int searchShapes(Point p) {
        for (int i = 0; i < recs.size(); i++) {
            if (recs.get(i) != null && recs.get(i).contains(p)) {
                return i;
            }
        }
        return -1;
    }

    private class ExtendedJSVGCanvas extends JSVGCanvas {

        ExtendedJSVGCanvas() {
            super();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.red);
            for (Rectangle rec : recs) {
                if (rec == null) {
                    continue;
                }
                g.drawRect((int) rec.getX(), (int) rec.getY(), (int) rec.getWidth(), (int) rec.getHeight());
            }
            
        }       
    }
}
