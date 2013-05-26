package org.ut.biolab.mfiume.query;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.swingx.JXSearchField;
import org.jdesktop.swingx.prompt.PromptSupport;
import org.jdesktop.swingx.prompt.PromptSupport.FocusBehavior;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.mfiume.query.SearchConditionGroupItem.QueryRelation;
import org.ut.biolab.mfiume.query.SearchConditionItem.SearchConditionListener;
import org.ut.biolab.mfiume.query.medsavant.complex.ConditionUtils;
import org.ut.biolab.mfiume.query.view.PillView;
import org.ut.biolab.mfiume.query.view.PopupGenerator;
import org.ut.biolab.mfiume.query.view.ScrollableJPopupMenu;
import org.ut.biolab.mfiume.query.view.SearchConditionItemView;

/**
 *
 * @author mfiume
 */
public class QueryViewController extends JPanel implements SearchConditionListener {

    private static Log LOG = LogFactory.getLog(QueryViewController.class);
    private final SearchConditionGroupItem rootGroup;
    private final HashMap<SearchConditionItem, SearchConditionItemView> itemToViewMap;
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
        applyButton = new JButton("Search");
        applyButton.setFocusable(false);
        setConditionsChanged(false);
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                applySearchConditions();
            }
        });
        this.refreshView();
        itemToViewMap = new HashMap<SearchConditionItem, SearchConditionItemView>();
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

                    System.out.println(c.toString());

                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            warningText.setVisible(false);
                            applyButton.setEnabled(false);
                            applyButton.setText("Searching...");
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
                } catch (Exception ex) {
                    LOG.error(ex);
                    ex.printStackTrace();
                    DialogUtils.displayException("Error", "There was an error performing your search", ex);
                }
            }
        });
        t.start();


    }

    public final void refreshView() {
        List<JComponent> cs;
        cs = getComponentsFromQueryModel(rootGroup);
        this.setLayout(new MigLayout("wrap 1"));

        JPanel p = ViewUtil.getClearPanel();
        p.setBorder(ViewUtil.getBottomLineBorder());
        p.setLayout(new MigLayout("wrap 1, hidemode 1"));

        this.removeAll();
        for (JComponent c : cs) {
            p.add(c, "center");
        }

        add(p);

        add(warningText, "center");
        add(applyButton, "center");

        this.invalidate();
        this.updateUI();
    }

    private List<JComponent> getComponentsFromQueryModel(SearchConditionGroupItem g) {
        List<JComponent> components = new ArrayList<JComponent>();
        for (final SearchConditionItem item : g.getItems()) {
            if (item instanceof SearchConditionGroupItem) {
                String addition = "";
                if (item.getParent() != null) {
                    addition = item.getRelation() + "";
                }

                final JPanel p = ViewUtil.getClearPanel();
                MigLayout ml = new MigLayout("wrap 1, hidemode 1, insets 2");
                p.setLayout(ml);
                p.setBorder(ViewUtil.getThickLeftLineBorder());

                final PillView pv = new PillView();
                pv.setActivated(true);
                pv.setPopupGenerator(new PopupGenerator() {
                    @Override
                    public JPopupMenu generatePopup() {
                        JPopupMenu m = new JPopupMenu();
                        JMenuItem visibility = new JMenuItem(p.isVisible() ? "Hide group conditions" : "Show group conditions");
                        final SearchConditionGroupItem g = (SearchConditionGroupItem) item;

                        visibility.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent ae) {

                                p.setVisible(!p.isVisible());
                                pv.setText((item.getParent().isFirstItem(item) ? "" : item.getRelation().toString()) + " " + g.getItems().size() + " grouped condition(s)");

                            }
                        });
                        m.add(visibility);
                        if (!item.getParent().isFirstItem(item)) {
                            if (item.getRelation() == QueryRelation.AND) {
                                JMenuItem b = new JMenuItem("Change to \"or\"");
                                b.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent ae) {
                                        item.setRelation(QueryRelation.OR);
                                        pv.setText((item.getParent().isFirstItem(item) ? "" : item.getRelation().toString()) + " " + g.getItems().size() + " grouped condition(s)");
                                    }
                                });
                                m.add(b);
                            } else {
                                JMenuItem b = new JMenuItem("Change to \"and\"");
                                b.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent ae) {
                                        item.setRelation(QueryRelation.AND);
                                        pv.setText((item.getParent().isFirstItem(item) ? "" : item.getRelation().toString()) + " " + g.getItems().size() + " grouped condition(s)");
                                    }
                                });
                                m.add(b);
                            }
                        }

                        return m;
                    }
                });
                SearchConditionGroupItem g2 = (SearchConditionGroupItem) item;
                pv.setText((item.getParent().isFirstItem(item) ? "" : item.getRelation().toString()) + " " + g2.getItems().size() + " grouped condition(s)");
                p.setVisible(false);

                components.add(pv);
                for (JComponent c : getComponentsFromQueryModel((SearchConditionGroupItem) item)) {
                    p.add(c, "center");
                }
                components.add(p);

            } else {
                itemToViewMap.get(item).refresh();
                components.add(itemToViewMap.get(item));
            }
        }

        components.add(getInputFieldForGroup(g));
        return components;
    }

    @Override
    public void searchConditionsOrderChanged(SearchConditionItem m) {
        System.out.println("Order changed");
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

    public void generateItemViewAndAddToGroup(String fieldName, SearchConditionGroupItem parent) {
        SearchConditionItem item = new SearchConditionItem(fieldName, parent);
        SearchConditionItemView view = conditionViewGenerator.generateViewForItem(item);
        addItemToGroup(item, view, parent);
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

    private JComponent getInputFieldForGroup(final SearchConditionGroupItem g) {

        final QueryViewController instance = this;

        final Map<String, List<String>> possible = conditionViewGenerator.getAllowableItemNames();
        final List<String> allPossible = new ArrayList<String>();
        for (String key : possible.keySet()) {
            allPossible.addAll(possible.get(key));
        }

        final JButton placeHolder = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.SEARCH_PH));

        final JXSearchField field = new JXSearchField();
        PromptSupport.setPrompt("Type search condition", field);
        PromptSupport.setFocusBehavior(FocusBehavior.SHOW_PROMPT, field);

        final Dimension focusedDim = new Dimension(220, field.getPreferredSize().height);
        field.setPreferredSize(focusedDim);

        field.addKeyListener(new KeyListener() {
            ScrollableJPopupMenu m;
            private ArrayList<JComponent> menuComponents;
            List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
            int currentlySelectedIndex;

            @Override
            public void keyTyped(KeyEvent ke) {
            }

            public void addItemBasedOnField() {
                generateItemViewAndAddToGroup(field.getText(), g);
                m.setVisible(false);
                field.setText("");
            }

            private void refreshPopup() {

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

                m.pack();
                m.show(field, 0, 23);
                m.updateUI();
            }

            private void moveUpOrDown(int increment) {

                // the new, non-header index to be selected
                int newIndex;

                // disarm previous
                if (currentlySelectedIndex != -1) {

                    menuItems.get(currentlySelectedIndex).setArmed(false);
                    newIndex = (currentlySelectedIndex + increment) % menuItems.size();

                    if (newIndex < 0) {
                        newIndex = menuItems.size() - 1;
                    }

                    // always start at the first position
                } else {
                    newIndex = increment > 1 ? 0 : menuItems.size() - 1;
                }

                // arm the index
                menuItems.get(newIndex).setArmed(true);

                // adjust the text
                field.setText(menuItems.get(newIndex).getText().replaceAll("\\<.*?>", ""));

                // set the index
                currentlySelectedIndex = newIndex;

                // scroll if necessary
                int scrollToIndex = menuComponents.indexOf(menuItems.get(newIndex));

                // include headers
                if (scrollToIndex > 0) {
                    int aboveIndex = scrollToIndex - 1;
                    if (!menuComponents.get(aboveIndex).isEnabled()) {
                        scrollToIndex = aboveIndex;
                    }
                }

                m.scrollToItem(scrollToIndex);
            }

            @Override
            public void keyPressed(KeyEvent ke) {

                // accept item
                if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (allPossible.contains(field.getText())) {
                        addItemBasedOnField();
                    }

                    // scroll
                } else if (ke.getKeyCode() == KeyEvent.VK_DOWN || ke.getKeyCode() == KeyEvent.VK_UP) {

                    if (m == null || !m.isVisible()) {
                        refreshPopup();
                        return;
                    }

                    // if things to scroll through
                    if (m.getComponentCount() > 0 && m.isVisible()) {

                        // which direction
                        int increment = ke.getKeyCode() == KeyEvent.VK_DOWN ? 1 : -1;
                        moveUpOrDown(increment);
                    }


                }
            }

            @Override
            public void keyReleased(KeyEvent ke) {

                // handled above
                if (ke.getKeyCode() == KeyEvent.VK_DOWN || ke.getKeyCode() == KeyEvent.VK_UP) {
                    return;
                }

                // hide the popup
                if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (m != null) {
                        m.setVisible(false);
                    }
                    return;
                }

                refreshPopup();


            }

            private void fixSize(JComponent m, Dimension d3) {
                m.setMinimumSize(d3);
                m.setMaximumSize(d3);
                m.setPreferredSize(d3);
            }
        });

        /*JPanel p = ViewUtil.getClearPanel();
         p.add(placeHolder);
         p.add(field);

         field.setVisible(false);

         placeHolder.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent ae) {
         placeHolder.setVisible(false);
         field.setVisible(true);
         field.requestFocus();
         }
         });

         field.addFocusListener(new FocusListener() {
         @Override
         public void focusGained(FocusEvent fe) {
         }

         @Override
         public void focusLost(FocusEvent fe) {
         placeHolder.setVisible(true);
         field.setVisible(false);
         }
         });*/


        return field;
    }

    @Override
    public void searchConditionItemAdded(SearchConditionItem m) {
    }

    @Override
    public void searchConditionEdited(SearchConditionItem item) {
        //this.invalidate();
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
}