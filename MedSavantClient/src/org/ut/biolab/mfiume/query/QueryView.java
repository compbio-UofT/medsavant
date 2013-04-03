package org.ut.biolab.mfiume.query;

import ch.rakudave.suggest.JSuggestField;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.mfiume.query.SearchConditionItem.SearchConditionListener;
import org.ut.biolab.mfiume.query.view.RoundedBorder;
import org.ut.biolab.mfiume.query.view.ScrollableJPopupMenu;
import org.ut.biolab.mfiume.query.view.SearchConditionItemView;

/**
 *
 * @author mfiume
 */
public class QueryView extends JPanel implements SearchConditionListener {

    private final SearchConditionGroupItem model;
    private final HashMap<SearchConditionItem, SearchConditionItemView> itemToViewMap;
    private final ConditionViewGenerator conditionViewGenerator;

    public QueryView(SearchConditionGroupItem model, ConditionViewGenerator c) {
        this.model = model;
        this.conditionViewGenerator = c;
        model.addListener(this);
        this.setOpaque(false);
        this.refreshView();
        itemToViewMap = new HashMap<SearchConditionItem, SearchConditionItemView>();
    }

    public final void refreshView() {
        List<JComponent> cs;
        cs = getComponentsFromQueryModel(model);
        this.setLayout(new FlowLayout(FlowLayout.LEADING, 4, 4));
        this.removeAll();
        for (JComponent c : cs) {
            this.add(c);
        }
        this.invalidate();
        this.updateUI();
    }

    private List<JComponent> getComponentsFromQueryModel(SearchConditionGroupItem g) {
        List<JComponent> components = new ArrayList<JComponent>();
        for (SearchConditionItem item : g.getItems()) {
            if (item instanceof SearchConditionGroupItem) {
                String addition = "";
                if (item.getParent() != null) {
                    addition = item.getRelation() + "";
                }
                components.add(new JLabel(addition + " ("));
                components.addAll(getComponentsFromQueryModel((SearchConditionGroupItem) item));
                components.add(new JLabel(")"));
            } else {
                itemToViewMap.get(item).refresh();
                components.add(itemToViewMap.get(item));
            }
        }

        components.add(getInputFieldForGroup(g));
        return components;
    }

    @Override
    public void searchConditionsChanged(SearchConditionItem m) {
        this.refreshView();
    }

    public void registerViewWithItem(SearchConditionItemView editor, SearchConditionItem item) {
        itemToViewMap.put(item, editor);
    }

    @Override
    public void searchConditionItemRemoved(SearchConditionItem m) {
        itemToViewMap.remove(m);
    }

    private JComponent getInputFieldForGroup(final SearchConditionGroupItem g) {

        final QueryView instance = this;


        final Map<String, List<String>> possible = conditionViewGenerator.getAllowableItemNames();
        final List<String> allPossible = new ArrayList<String>();
        for (String key : possible.keySet()) {
            allPossible.addAll(possible.get(key));
        }

        final JTextField field = new JTextField();
        field.putClientProperty("JTextField.variant", "search");

        Dimension focusedDim = new Dimension(220, field.getPreferredSize().height);
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
                SearchConditionItem item = new SearchConditionItem(field.getText(), g);
                SearchConditionItemView view = conditionViewGenerator.generateViewForItem(item);
                instance.registerViewWithItem(view, item);
                g.addItem(item);
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

                    if (sectionHasMatch)
                    {
                        menuComponents.add(headerIndex,l);
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

                System.out.println("Moving");

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
                    newIndex = increment > 1 ? 0 : menuItems.size()-1;
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
        return field;
    }

    @Override
    public void searchConditionItemAdded(SearchConditionItem m) {
    }
}
