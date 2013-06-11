package org.ut.biolab.mfiume.query.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.mfiume.query.SearchConditionGroupItem;
import org.ut.biolab.mfiume.query.SearchConditionGroupItem.QueryRelation;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.SearchConditionItem.SearchConditionListener;
import org.ut.biolab.mfiume.query.img.ImagePanel;

/**
 *
 * @author mfiume
 */
public class SearchConditionItemView extends PillView {

    private final SearchConditionItem item;
    private final SearchConditionEditorView editor;

    public SearchConditionItemView(SearchConditionItem i, final SearchConditionEditorView editor) {
        this.item = i;
        this.editor = editor;

        i.addListener(new SearchConditionListener() {
            @Override
            public void searchConditionsOrderChanged(SearchConditionItem m) {
            }

            @Override
            public void searchConditionItemRemoved(SearchConditionItem m) {
            }

            @Override
            public void searchConditionItemAdded(SearchConditionItem m) {
            }

            @Override
            public void searchConditionEdited(SearchConditionItem m) {
                refresh();
            }
        });

        this.setPopupGenerator(new PopupGenerator() {
            @Override
            public JPopupMenu generatePopup() {
                final JPopupMenu m = new JPopupMenu();

                final JPanel conditionsEditor = ViewUtil.getClearPanel();
                ViewUtil.applyVerticalBoxLayout(conditionsEditor);
                JProgressBar waitForConditions = new JProgressBar();
                waitForConditions.setIndeterminate(true);
                if (ClientMiscUtils.MAC) {
                    waitForConditions.putClientProperty("JProgressBar.style", "circular");
                }
                conditionsEditor.add(ViewUtil.centerHorizontally(new JLabel("Preparing condition,")));
                conditionsEditor.add(Box.createVerticalStrut(5));
                conditionsEditor.add(ViewUtil.centerHorizontally(new JLabel("please wait...")));
                conditionsEditor.add(ViewUtil.centerHorizontally(waitForConditions));

                JPanel closePanel = ViewUtil.getClearPanel();
                closePanel.setLayout(new BoxLayout(closePanel, BoxLayout.X_AXIS));
                closePanel.add(Box.createHorizontalGlue());
                JButton closeButton = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CLOSE));
                closePanel.add(closeButton);
                closePanel.add(Box.createHorizontalStrut(5));
                closeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        m.setVisible(false);
                    }
                });

                m.add(closePanel);

                m.add(conditionsEditor);
                m.add(new JSeparator());

                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            editor.loadViewFromExistingSearchConditionParameters();
                            SwingUtilities.invokeAndWait(new Runnable() {
                                @Override
                                public void run() {
                                    conditionsEditor.removeAll();

                                    JPanel p = ViewUtil.getClearPanel();
                                    p.setBorder(ViewUtil.getMediumBorder());
                                    p.add(editor);
                                    conditionsEditor.add(p);
                                    m.pack();
                                    m.invalidate();
                                    m.updateUI();
                                }
                            });
                        } catch (Exception ex) {
                            Logger.getLogger(SearchConditionItemView.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                };
                t.start();

                if (item.getParent().getItems().size() > 1) {
                    m.add(new JMenuItem(new AbstractAction("Convert to group") {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            item.getParent().createGroupFromItem(item);
                        }
                    }));
                }

                if (!item.getParent().isFirstItem(item)) {
                    if (item.getRelation() == QueryRelation.OR) {
                        JMenuItem toggle = new JMenuItem(new AbstractAction("Change to \"and\"") {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                item.setRelation(QueryRelation.AND);
                            }
                        });
                        m.add(toggle);
                    } else {
                        JMenuItem toggle = new JMenuItem(new AbstractAction("Change to \"or\"") {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                item.setRelation(QueryRelation.OR);
                            }
                        });
                        m.add(toggle);
                    }
                }
                JMenuItem delete = new JMenuItem(new AbstractAction("Remove condition") {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        item.getParent().removeItem(item);
                    }
                });
                m.add(delete);


                return m;
            }
        });

        refresh();
    }

    public final void refresh() {

        this.setActivated(item.getSearchConditionEncoding() != null);
        this.setText(
                "<html>"
                + (!item.getParent().isFirstItem(item) ? item.getRelation() + " " : "")
                + "<b>" + item.getName() + "</b>"
                + (item.getDescription() != null ? " is " + item.getDescription() + "" : " is <i>unset</i>") + "</html>");
    }

}
