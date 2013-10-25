package org.ut.biolab.mfiume.query.view;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.mfiume.query.SearchConditionGroupItem.QueryRelation;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.SearchConditionItem.SearchConditionListener;

/**
 *
 * @author mfiume
 */
public class SearchConditionItemView extends PillView {

    private final SearchConditionItem item;
    private final SearchConditionEditorView editor;
    private static final int BORDER_PADDING = 5;
    private JPopupMenu advancedMenu = new JPopupMenu();
    
    @Override
    public void showDialog(Point p) {
        showDialog(p, "Editing Condition: "+item.getName());
    }

    
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

        this.setDialogGenerator(new ConditionEditorDialogGenerator() {
            @Override
            public JDialog generateDialog() {
                final JPanel mainPanel = new JPanel();
                final JDialog dialog = new JDialog(MedSavantFrame.getInstance());

                mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

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


                mainPanel.add(conditionsEditor);
                
                mainPanel.setBorder(new EmptyBorder(BORDER_PADDING, BORDER_PADDING, BORDER_PADDING, BORDER_PADDING));
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            editor.loadViewFromExistingSearchConditionParameters();
                            SwingUtilities.invokeAndWait(new Runnable() {
                                @Override
                                public void run() {
                                    conditionsEditor.removeAll();
                                    conditionsEditor.add(editor);                                    
                                    dialog.pack();
                                    dialog.invalidate();
                                    mainPanel.updateUI();

                                }
                            });
                        } catch (Exception ex) {
                            Logger.getLogger(SearchConditionItemView.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                };
                t.start();

                JPanel horizButtonPanel = new JPanel();
                horizButtonPanel.setLayout(new BoxLayout(horizButtonPanel, BoxLayout.X_AXIS));                
                if (item.getParent().getItems().size() > 0) {
                    
                   // JButton convertToGroupButton = ViewUtil.getSoftButton("Convert to group");
                    
                    JMenuItem convertToGroupItem = new JMenuItem("Convert to group");
                    convertToGroupItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            item.getParent().createGroupFromItem(item);
                            dialog.dispose();
                        }
                    });
                    //horizButtonPanel.add(button);
                    advancedMenu.add(convertToGroupItem);

                    if (item.getParent().getParent() != null) {
                        //JButton ungroupButton = ViewUtil.getSoftButton("Ungroup");
                        JMenuItem ungroupItem = new JMenuItem("Ungroup");
                        ungroupItem.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                item.getParent().moveItemToGroup(item, item.getParent().getParent());
                                dialog.dispose();
                            }
                        });
                        advancedMenu.add(ungroupItem);
                        //horizButtonPanel.add(button);
                    }

                }


                if (!item.getParent().isFirstItem(item)) {
                    if (item.getRelation() == QueryRelation.OR) {
                        //JButton toggle = ViewUtil.getSoftButton("Change to \"and\"");
                        JMenuItem toggle = new JMenuItem("Change to \"and\"");
                        toggle.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                item.setRelation(QueryRelation.AND);
                                dialog.dispose();
                            }
                        });
                        advancedMenu.add(toggle);
                        //horizButtonPanel.add(toggle);
                    } else {
                        //JButton toggle = ViewUtil.getSoftButton("Change to \"or\"");
                        JMenuItem toggle = new JMenuItem("Change to \"or\"");
                        toggle.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                item.setRelation(QueryRelation.OR);
                                dialog.dispose();
                            }
                        });
                        advancedMenu.add(toggle);
                        //horizButtonPanel.add(toggle);
                    }
                }
                
                //JButton delete = ViewUtil.getSoftButton("Remove Condition");
                JButton delete = new JButton("Remove");
                delete.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        item.getParent().removeItem(item);
                        dialog.dispose();
                    }
                });
                
                //JButton OKButton = ViewUtil.getSoftButton("OK");
                JButton OKButton = new JButton("OK");
                OKButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {                      
                        dialog.dispose();
                    }
                });
                
                if(advancedMenu.getComponentCount() > 0){
                    final JButton gearButton = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CONFIGURE));
                    gearButton.setToolTipText("More Options");
                    gearButton.addActionListener(new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            advancedMenu.show(gearButton, 0, gearButton.getHeight());
                        }
                        
                    });
                    horizButtonPanel.add(gearButton);
                }
                                                     
                horizButtonPanel.add(Box.createHorizontalGlue());
                horizButtonPanel.add(delete);
                horizButtonPanel.add(OKButton);
                horizButtonPanel.setMaximumSize(new Dimension(horizButtonPanel.getMaximumSize().width, 22));
                mainPanel.add(horizButtonPanel);

                dialog.setModal(true);
                dialog.setContentPane(mainPanel);
                dialog.pack();
                dialog.setLocationRelativeTo(MedSavantFrame.getInstance());
                return dialog;
            }
        });

        refresh();
    }

    public final void refresh() {

        this.setActivated(item.getSearchConditionEncoding() != null);

        if (item.getExplanation() != null) {
            setInfo(item.getExplanation());
        }

        String name = item.getName(); // e.g. "frequency - thousand genomes"

        int index = name.indexOf("-");
        if (index != -1) {
            // remove the program name, e.g. frequency
            name = name.substring(0, index);
        }

        this.setText(
                "<html>"
                + (!item.getParent().isFirstItem(item) ? item.getRelation() + " " : "")
                + "<b>" + name + "</b>"
                + (item.getDescription() != null ? " is " + item.getDescription() + "" : " is <i>unset</i>") + "</html>");
    }
}
