/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.query;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.text.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.httpclient.NameValuePair;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.view.genetics.QueryUtils;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.client.query.SearchConditionGroupItem.QueryRelation;
import org.ut.biolab.medsavant.client.query.SearchConditionItem.SearchConditionListener;
import org.ut.biolab.medsavant.client.query.medsavant.complex.ConditionUtils;
import org.ut.biolab.medsavant.client.query.view.PillView;
import org.ut.biolab.medsavant.client.query.view.ConditionPopupGenerator;
import org.ut.biolab.medsavant.client.query.view.SearchConditionItemView;
import org.ut.biolab.savant.analytics.savantanalytics.AnalyticsAgent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author mfiume
 */
public class QueryViewController extends JPanel implements SearchConditionListener {

    private static Log LOG = LogFactory.getLog(QueryViewController.class);
    //Width of the bottom (i.e. root group) search text box.
    private static final int BOTTOM_SEARCH_TEXTFIELD_WIDTH = 270;
    private final SearchConditionGroupItem rootGroup;
    private final HashMap<SearchConditionItem, SearchConditionItemView> itemToViewMap;
    private Map<SearchConditionGroupItem, Boolean> expandedItemsMap = new HashMap<SearchConditionGroupItem, Boolean>();
    private final ConditionViewGenerator conditionViewGenerator;
    private boolean didChangeSinceLastApply;
    private JButton applyButton;
    private final JLabel warningText;

    public QueryViewController(SearchConditionGroupItem model, ConditionViewGenerator c) {
        this.rootGroup = model;
        this.conditionViewGenerator = c;
        model.addListener(this);
        this.setOpaque(false);
        this.setFocusable(true);
        warningText = new JLabel("search conditions have changed");
        warningText.setFont(ViewUtil.getSmallTitleFont());
        warningText.setForeground(ViewUtil.getSubtleTitleColor());
        applyButton = new JButton("Search");
        applyButton.setFocusable(false);
        setConditionsChanged(false);
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                applySearchConditions();

                try {
                    AnalyticsAgent.log(new NameValuePair[]{
                        new NameValuePair("view-event", "SearchPerformed")
                    });
                } catch (Exception e) {
                }
            }
        });

        this.refreshView();
        itemToViewMap = new HashMap<SearchConditionItem, SearchConditionItemView>();
    }

    /**
     * Re-executes the current query with the given genomic region restrictions,
     * and returns the results. Note the search bar is not changed. gr and alt
     * should have a one-to-one correspondence, where the alt for the ith
     * genomic region is in alt[i].
     */
    public List<Object[]> restrictToRegion(List<GenomicRegion> gr, List<String> alt, int limit) {
        try {
            long st = System.currentTimeMillis();
            Condition r;
            if (rootGroup.getItems().size() > 0) {
                r = getSQLConditionsFrom(rootGroup);
                SearchConditionGroupItem rg = QueryUtils.getRegionGroup(gr.get(0), alt.get(0), false);
                r = ComboCondition.and(r, getSQLConditionsFrom(rg));
            } else {
                SearchConditionGroupItem rg = QueryUtils.getRegionGroup(gr.get(0), alt.get(0), false);
                r = getSQLConditionsFrom(rg);
            }

            for (int i = 1; i < gr.size(); ++i) {
                SearchConditionGroupItem rg = QueryUtils.getRegionGroup(gr.get(i), alt.get(i), false);
                r = ComboCondition.and(r, getSQLConditionsFrom(rg));
            }

            return MedSavantClient.VariantManager.getVariants(
                    LoginController.getSessionID(),
                    ProjectController.getInstance().getCurrentProjectID(),
                    ReferenceController.getInstance().getCurrentReferenceID(),
                    new Condition[][]{{r}},
                    0,
                    limit); //DEBUG CODE, sets limit to 10!

        } catch (Exception ex) {
            LOG.error(ex);
            ex.printStackTrace();
            DialogUtils.displayException("Error", "There was an error performing your search", ex);
        }

        return null;
    }

    public List<Object[]> restrictToRegion(GenomicRegion gr, String alt, int limit) {
        List<GenomicRegion> grl = new ArrayList<GenomicRegion>(1);
        List<String> al = new ArrayList<String>(1);
        grl.add(gr);
        al.add(alt);
        return restrictToRegion(grl, al, limit);
    }

    private void applySearchConditions() {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Condition c;
                    c = getSQLConditionsFrom(rootGroup);
                    if (c == null) {
                        c = ConditionUtils.TRUE_CONDITION;
                    }

                    //LOG.info(c.toString());
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            warningText.setVisible(false);
                            applyButton.setText("Searching...");
                            applyButton.setEnabled(false);
                            applyButton.updateUI();
                        }
                    });
                    FilterController.getInstance().setConditions(c);
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            applyButton.setText("Search");
                            applyButton.updateUI();
                        }
                    });
                } catch (final IllegalArgumentException ex) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            DialogUtils.displayError("Search Error", ex.getMessage());
                        }
                    });
                } catch (Exception ex) {
                    LOG.info(ex);
                    ex.printStackTrace();
                    DialogUtils.displayException("Error", "There was an error performing your search", ex);
                }
            }
        });
        t.start();

    }

    public void saveConditions(File file) {
        String xml = rootGroup.toXML();
        try {
            PrintWriter out = new PrintWriter(file);
            out.println(xml);
            out.close();
        } catch (Exception ex) {
            LOG.error(ex);
            DialogUtils.displayException("Error", "There was an error saving your search", ex);
        }

    }

    private SearchConditionItem getItemFromXML(Element element, SearchConditionGroupItem parentGroup) throws ParseException {

        String name = StringEscapeUtils.unescapeXml(element.getAttribute("name"));

        QueryRelation qr;
        if (element.getAttribute("queryRelation").equalsIgnoreCase("OR")) {
            qr = QueryRelation.OR;
        } else if (element.getAttribute("queryRelation").equalsIgnoreCase("AND")) {
            qr = QueryRelation.AND;
        } else {
            throw new ParseException("Malformed input file contains invalid query relation", 0);
        }

        SearchConditionItem sci = new SearchConditionItem(name, qr, parentGroup);

        String encodedConditions = StringEscapeUtils.unescapeXml(element.getAttribute("encodedConditions"));
        if (encodedConditions != null && encodedConditions.length() > 0) {
            sci.setSearchConditionEncoding(encodedConditions);
        }

        //The description does not need to be unescaped.
        String desc = element.getAttribute("description");
        if (desc != null && desc.length() > 0) {
            sci.setDescription(desc);
        }
        generateItemViewAndAddToGroup(sci, parentGroup);
        return sci;
    }

    private SearchConditionGroupItem getGroupFromXML(Element rootElement, SearchConditionGroupItem parentGroup) throws ParseException {
        if (!rootElement.getNodeName().toLowerCase().equals("group")) {
            DialogUtils.displayError("ERROR: Malformed/Invalid Input file");
        }

        QueryRelation qr;
        if (rootElement.getAttribute("queryRelation").equalsIgnoreCase("OR")) {
            qr = QueryRelation.OR;
        } else if (rootElement.getAttribute("queryRelation").equalsIgnoreCase("AND")) {
            qr = QueryRelation.AND;
        } else {
            throw new ParseException("Malformed input file contains invalid query relation", 0);
        }

        String desc = rootElement.getAttribute("description");

        SearchConditionGroupItem scg;
        if (parentGroup == null) {
            scg = this.rootGroup;
        } else {
            scg = new SearchConditionGroupItem(qr, null, parentGroup);
            if (desc != null && desc.length() > 0) {
                scg.setDescription(desc);
            }
        }

        NodeList nl = rootElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if (el.getNodeName().equalsIgnoreCase("group")) {
                    SearchConditionGroupItem childGroup = getGroupFromXML(el, scg);
                    scg.addItem(childGroup);
                } else if (el.getNodeName().equalsIgnoreCase("item")) {
                    SearchConditionItem sci = getItemFromXML(el, scg);
                    //scg.addItem(sci);
                } else {
                    throw new ParseException("Malformed input file contains invalid XML element", 0);
                }
            }
        }

        return scg;

    }

    public List<SearchConditionItem> getFirstLevelItemsByDesc(String itemDesc) {
        List<SearchConditionItem> results = new LinkedList<SearchConditionItem>();
        SearchConditionItem sci = null;
        for (SearchConditionItem i : getQueryRootGroup().getItems()) {
            if (i.getDescription() != null && i.getDescription().equals(itemDesc)) {
                results.add(i);
            }
        }
        return results;
    }

    public List<SearchConditionItem> getFirstLevelItemsByName(String itemName) {
        List<SearchConditionItem> results = new LinkedList<SearchConditionItem>();
        for (SearchConditionItem i : getQueryRootGroup().getItems()) {
            if (i.getName() != null && i.getName().equals(itemName)) {
                results.add(i);
            }
        }
        return results;
    }

    public void replaceFirstLevelItem(String name, String encodedConditions, String description) {
        List<SearchConditionItem> sciList = getFirstLevelItemsByName(name);
        for (SearchConditionItem sci : sciList) {
            getQueryRootGroup().removeItem(sci);
            if (sci instanceof SearchConditionGroupItem) {
                expandedItemsMap.remove((SearchConditionGroupItem) sci);
            }
        }

        SearchConditionItem sci = new SearchConditionItem(name, QueryRelation.AND, getQueryRootGroup());
        generateItemViewAndAddToGroup(sci, getQueryRootGroup());

        sci.setSearchConditionEncoding(encodedConditions);
        sci.setDescription(description);
    }

    /**
     * Replaces the items in the first level group (i.e. a group immediately
     * under the root node) with the description "groupDesc" with those in the
     * list 'sciList'.
     *
     * If the group does not exist, a new one is created under the root query
     * group.
     *
     * No views are created or updated by this function.
     *
     * @return The SearchConditionGroupItem matching the description, or the one
     * that was created.
     */
    public SearchConditionGroupItem replaceFirstLevelGroup(String groupDesc, List<SearchConditionItem> sciList, QueryRelation qr, boolean listIsItems) {

        List<SearchConditionItem> scgList = getFirstLevelItemsByDesc(groupDesc);
        for (SearchConditionItem sci : scgList) {
            getQueryRootGroup().removeItem(sci);
            //expandedItemsMap.remove(getQueryRootGroup());
            expandedItemsMap.remove(sci);
        }

        SearchConditionGroupItem scg = new SearchConditionGroupItem(qr, null, getQueryRootGroup());
        scg.setDescription(groupDesc);
        getQueryRootGroup().addItem(scg);

        if (sciList != null) {
            for (SearchConditionItem sci : sciList) {
                sci.setParent(scg);
                if (listIsItems) {
                    generateItemViewAndAddToGroup(sci, scg);
                } else {
                    scg.addItem(sci);
                }
            }
        }

        return scg;
    }

    //clears all search terms, and refreshes the view only if refresh is set.
    private void clearSearch(boolean refresh) {

        try {
            AnalyticsAgent.log(new NameValuePair[]{
                new NameValuePair("view-event", "SearchCleared")
            });
        } catch (Exception e) {
        }

        this.rootGroup.clearItems();
        this.itemToViewMap.clear();
        this.expandedItemsMap.clear();
        if (refresh) {
            refreshView();
        }
    }

    /**
     * Clears all search terms and refreshes the view
     */
    public void clearSearch() {
        clearSearch(true);
    }

    public void loadConditions(File f) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Element rootElement = db.parse(f).getDocumentElement();
            clearSearch(false);
            getGroupFromXML(rootElement, null);
            refreshView();
        } catch (Exception ex) {
            LOG.error(ex);
            DialogUtils.displayException("Error", "There was an error loading your search", ex);
        }
    }

    public final void refreshView() {
        List<JComponent> cs;

        cs = getComponentsFromQueryModel(rootGroup);
        this.setLayout(new BorderLayout());

        JPanel p = ViewUtil.getClearPanel();
        //  p.setBorder(ViewUtil.getBottomLineBorder());
        p.setLayout(new MigLayout("hidemode 1, nogrid, fillx, insets 12 12 12 20"));

        this.removeAll();

        JLabel l = ViewUtil.getEmphasizedLabel("SEARCH CONDITIONS");
        p.add(l, "left");

        p.add(ViewUtil.getHelpButton("How to search", "Type a search condition into the search box, e.g. \"Chromosome\". "
                + "Press Enter / Return to accept the selected condition name. "
                + "You\'ll then be prompted to specify parameters for this condition."), "left, wrap");

        for (JComponent c : cs) {
            p.add(c, "left, wrap");
        }

        p.add(warningText, "left, wrap");
        p.add(applyButton, "left, wrap, gapy n 1");

        JScrollPane jsp = ViewUtil.getClearBorderlessScrollPane(p);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.add(jsp, BorderLayout.CENTER);

        this.invalidate();
        this.updateUI();
    }

    private String getGroupTitle(SearchConditionGroupItem scg) {
        if (scg.getDescription() != null && scg.getDescription().length() > 0) {
            return (scg.getParent().isFirstItem(scg) ? "" : scg.getRelation().toString()) + " " + scg.getDescription();
        } else {
            return (scg.getParent().isFirstItem(scg) ? "" : scg.getRelation().toString()) + " " + scg.getItems().size() + " grouped condition(s)";
        }
    }

    private List<JComponent> getComponentsFromQueryModel(SearchConditionGroupItem g) {
        return getComponentsFromQueryModel(g, 0);
    }

    private List<JComponent> getComponentsFromQueryModel(SearchConditionGroupItem g, int depth) {
        List<JComponent> components = new ArrayList<JComponent>();
        int w = 0;
        for (final SearchConditionItem item : g.getItems()) {
            if (item instanceof SearchConditionGroupItem) {
                String addition = "";
                if (item.getParent() != null) {
                    addition = item.getRelation() + "";
                }

                final JPanel p = ViewUtil.getClearPanel();
                MigLayout ml = new MigLayout("wrap 1, hidemode 1, insets 2");
                p.setLayout(ml);
                Border border = ViewUtil.getThickLeftLineBorder();
                p.setBorder(border);

                final PillView pv = new PillView(true);
                pv.indent(depth);

                pv.setActivated(true);

                final ActionListener toggleGroupExpand = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        expandedItemsMap.put((SearchConditionGroupItem) item, !p.isVisible());
                        p.setVisible(!p.isVisible());
                        pv.setText(getGroupTitle((SearchConditionGroupItem) item));
                    }
                };

                pv.setExpandListener(toggleGroupExpand);

                pv.setPopupGenerator(new ConditionPopupGenerator() {
                    @Override
                    public JPopupMenu generatePopup() {
                        final SearchConditionGroupItem groupItem = (SearchConditionGroupItem) item;
                        final JPopupMenu m = new JPopupMenu();

                        if (!item.getParent().isFirstItem(item)) {
                            if (item.getRelation() == QueryRelation.AND) {
                                JMenuItem b = new JMenuItem("Change to \"or\"");
                                b.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent ae) {
                                        item.setRelation(QueryRelation.OR);
                                        pv.setText(getGroupTitle(groupItem));
                                    }
                                });
                                m.add(b);
                            } else {
                                JMenuItem b = new JMenuItem("Change to \"and\"");
                                b.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent ae) {
                                        item.setRelation(QueryRelation.AND);
                                        pv.setText(getGroupTitle(groupItem));
                                    }
                                });
                                m.add(b);
                            }
                        }

                        JMenuItem delgroup = new JMenuItem("Delete");
                        delgroup.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                groupItem.getParent().removeItem(groupItem);
                                refreshView();
                                expandedItemsMap.remove((SearchConditionGroupItem) item);
                            }
                        });
                        m.add(delgroup);

                        return m;
                    }
                });
                SearchConditionGroupItem g2 = (SearchConditionGroupItem) item;

                pv.setText(getGroupTitle(g2));
                Boolean exp = expandedItemsMap.get((SearchConditionGroupItem) item);

                if (exp == null) {
                    p.setVisible(false);
                    pv.collapse();
                } else {
                    p.setVisible(true);
                    pv.expand();
                }

                components.add(pv);
                for (JComponent c : getComponentsFromQueryModel((SearchConditionGroupItem) item, depth + 1)) {
                    p.add(c, "left");
                }
                components.add(p);

                w = pv.getMaximumSize().width;
            } else {
                SearchConditionItemView sciv = itemToViewMap.get(item);
                ((PillView) sciv).indent(depth);
                w = sciv.getMaximumSize().width;
                sciv.refresh();
                components.add(sciv);
            }
        }

        if (w == 0) {
            w = BOTTOM_SEARCH_TEXTFIELD_WIDTH;
        }
        JComponent c = getInputFieldForGroup(g, w);
        components.add(c);
        return components;
    }
    private int c = 0;

    @Override
    public void searchConditionsOrderChanged(SearchConditionItem m) {
        setConditionsChanged(true);
        this.refreshView();
    }

    public void registerViewWithItem(SearchConditionItemView editor, SearchConditionItem item) {

        itemToViewMap.put(item, editor);
    }

    @Override
    public void searchConditionItemRemoved(SearchConditionItem m) {
        itemToViewMap.remove(m);
    }

    public void generateItemViewAndAddToGroup(SearchConditionItem sci, SearchConditionGroupItem parent) {
        SearchConditionItemView view = conditionViewGenerator.generateViewForItem(sci);
        addItemToGroup(sci, view, parent);
    }

    public SearchConditionItemView generateItemViewAndAddToGroup(String fieldName, SearchConditionGroupItem parent) {
        SearchConditionItem item = new SearchConditionItem(fieldName, parent);
        SearchConditionItemView view = conditionViewGenerator.generateViewForItem(item);
        addItemToGroup(item, view, parent);
        return view;
    }

    public void addItemToGroup(SearchConditionItem item, SearchConditionItemView view, SearchConditionGroupItem parent) {               
        registerViewWithItem(view, item);
        parent.addItem(item);
    }

    public void addGroupToGroup(SearchConditionGroupItem child, SearchConditionGroupItem parent) {
        parent.addItem(child);
    }

    public SearchConditionGroupItem getQueryRootGroup() {
        return this.rootGroup;
    }

    private JComponent getInputFieldForGroup(final SearchConditionGroupItem g, int width) {

        final QueryViewController instance = this;

        final Map<String, List<String>> possible = conditionViewGenerator.getAllowableItemNames();
        final CaseInsensitiveArrayList allPossible = new CaseInsensitiveArrayList();
        for (String key : possible.keySet()) {
            allPossible.addAll(possible.get(key));
        }

        final JButton addCondition = ViewUtil.getSoftButton("Add search condition...");
        final CustomKeyListener keyListener = new CustomKeyListener(this, possible, allPossible, g);
        addCondition.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                keyListener.refreshPopup(addCondition);
            }

        });

        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(p);
        p.add(addCondition);

        return p;
    }

    @Override
    public void searchConditionItemAdded(SearchConditionItem m) {
    }

    @Override
    public void searchConditionEdited(SearchConditionItem item) {
        setConditionsChanged(true);
    }

    private void setConditionsChanged(boolean b) {
        if (applyButton != null) {
            warningText.setVisible(b);
            applyButton.setSelected(b);
            applyButton.setEnabled(b);
        }
    }

    private Condition getSQLConditionsFrom(SearchConditionItem item) throws Exception {

        if (item instanceof SearchConditionGroupItem) {

            SearchConditionGroupItem group = (SearchConditionGroupItem) item;
            Condition childrenConditions = null;
            for (SearchConditionItem child : group.getItems()) {
                if (childrenConditions == null) {
                    childrenConditions = getSQLConditionsFrom(child);
                } else {
                    if (child.getRelation() == QueryRelation.AND) {
                        childrenConditions = ComboCondition.and(childrenConditions, getSQLConditionsFrom(child));
                    } else if (child.getRelation() == QueryRelation.OR) {
                        childrenConditions = ComboCondition.or(childrenConditions, getSQLConditionsFrom(child));
                    }
                }
            }

            return childrenConditions;
        } else {
            return conditionViewGenerator.generateConditionForItem(item);
        }
    }

    private static class CustomKeyListener {

        private final Map<String, List<String>> possible;
        private final CaseInsensitiveArrayList allPossible;
        private SearchConditionGroupItem group;
        private QueryViewController qvc;

        public CustomKeyListener(QueryViewController qvc, Map<String, List<String>> p, CaseInsensitiveArrayList ap, SearchConditionGroupItem g) {
            this.qvc = qvc;
            this.possible = p;
            this.allPossible = ap;
            this.group = g;
        }

        ;
        private ArrayList<JComponent> menuComponents;
        List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
        int currentlySelectedIndex;

        public void addItemBasedOnField(String validTerm) {
            final SearchConditionItemView view = qvc.generateItemViewAndAddToGroup(validTerm, group);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    view.showDialog(view.getLocationOnScreen());
                }
            });
        }

        public void refreshPopup(JComponent parent) {

            QueryConditionDialog m = new QueryConditionDialog(possible);
            m.setVisible(true);
            String choice = m.getChosenConditionName();
            if (choice != null) {
                addItemBasedOnField(choice);
            }

            
            /*
             currentlySelectedIndex = -1;

             Dimension d = new Dimension(field.getWidth(), 23);

             menuComponents = new ArrayList<JComponent>();

             menuItems.removeAll(menuItems);

             for (String key : possible.keySet()) {
             JMenuItem l = new JMenuItem(key.toUpperCase());
             Font f = new Font(l.getFont().getFamily(), Font.PLAIN, 10);
             l.setFont(f);
             l.setEnabled(false);
             l.setBackground(new Color(179, 189, 199));
             l.setForeground(Color.white);
             Dimension d2 = new Dimension(d.width, d.height - 5);
             l.setMinimumSize(d2);
             l.setMaximumSize(d2);
             l.setPreferredSize(d2);

             int headerIndex = menuComponents.size();
             boolean sectionHasMatch = false;

             Collections.sort(possible.get(key));

             for (String s : possible.get(key)) {
             int indexOfMatch = s.toLowerCase().indexOf(field.getText().toLowerCase());
             if (indexOfMatch > -1) {
             sectionHasMatch = true;
             int to = indexOfMatch + field.getText().length();

             final JMenuItem i = new JMenuItem("<html>"
             + s.substring(0, indexOfMatch)
             + "<b>" + s.substring(indexOfMatch, to) + "</b>"
             + s.substring(to) + "</html>");
             fixSize(i, d);
             i.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
             field.setText(i.getText().replaceAll("\\<.*?>", ""));
             }
             });
             menuItems.add(i);
             menuComponents.add(i);
             }
             }

             if (sectionHasMatch) {
             menuComponents.add(headerIndex, l);
             }
             }

             int originalheight = 0;
             for (Component c : menuComponents) {
             originalheight += c.getPreferredSize().getHeight();
             }

             //int maxHeight = 100;
             int adjustedheight = originalheight;

             m = new ScrollableJPopupMenu(15);
            
             m.setFocusable(false);
             m.removeAll();

             for (JComponent c : menuComponents) {
             m.addComponent(c);
             }

             Dimension dadjusted = new Dimension(m.getPreferredSize().width, adjustedheight + 10);
             //fixSize(m, dadjusted);

             //m.pack();
             m.setPopupSize(200, 400);
             m.show(field, 0, 23);
             m.updateUI();
             */
        }
    }

    public class CaseInsensitiveArrayList extends ArrayList<String> {

        @Override
        public boolean contains(Object o) {
            String paramStr = (String) o;
            for (String s : this) {
                if (paramStr.equalsIgnoreCase(s)) {
                    return true;
                }
            }
            return false;
        }

        public String getInsensitiveMatch(String o) {
            String paramStr = (String) o;
            for (String s : this) {
                if (paramStr.equalsIgnoreCase(s)) {
                    return s;
                }
            }
            return null;
        }
    }
}
