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
package org.ut.biolab.medsavant.client.query.view;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.query.SearchConditionItem;
import org.ut.biolab.medsavant.client.query.SearchConditionItem.SearchConditionListener;

/**
 *
 * @author mfiume
 */
public class SearchConditionItemView extends PillView {

    private final SearchConditionItem item;
    private final SearchConditionEditorView editor;
    //private JPopupMenu advancedMenu = new JPopupMenu();

    @Override
    public void showDialog(Point p) {
        showDialog(p, "Editing Condition: " + item.getName());
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
                final JDialog dialog = new JDialog(MedSavantFrame.getInstance());
                JPopupMenu advancedMenu = getAdvancedMenu(editor.getSearchConditionItem(), dialog);
                final SearchConditionPanel mainPanel = new SearchConditionPanel(editor, advancedMenu);
                JPanel buttonPanel = mainPanel.getButtonPanel();

                JButton OKButton = new JButton("OK");
                OKButton.setFocusable(false);
                OKButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        try {
                            if (editor.saveChanges()) {
                                setSelected(false);
                                dialog.dispose();
                            }
                        } catch (IllegalArgumentException ex) {
                            DialogUtils.displayError(ex.getMessage());
                        }
                    }
                });

                JButton delete = new JButton("Remove");
                delete.setFocusable(false);
                delete.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        setSelected(false);
                        item.getParent().removeItem(item);
                        dialog.dispose();
                    }
                });

                buttonPanel.add(delete);
                buttonPanel.add(OKButton);

                mainPanel.loadEditorViewInBackground(new Runnable() {
                    @Override
                    public void run() {
                        dialog.pack();
                        dialog.invalidate();
                    }
                });

                dialog.setModal(true);
                dialog.setContentPane(mainPanel);
                dialog.pack();
                dialog.setLocationRelativeTo(MedSavantFrame.getInstance());
                return dialog;
            }

            private JPopupMenu getAdvancedMenu(final SearchConditionItem item, final JDialog dialog) {
                final JPopupMenu advancedMenu = new JPopupMenu();
                if (item.getParent().getItems().size() > 0) {
                    
                    JMenuItem convertToGroupItem = new JMenuItem("Convert to group");
                    convertToGroupItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            item.getParent().createGroupFromItem(item);
                            dialog.dispose();
                        }
                    });                    
                    advancedMenu.add(convertToGroupItem);

                    if (item.getParent().getParent() != null) {
                        
                        JMenuItem ungroupItem = new JMenuItem("Ungroup");
                        ungroupItem.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                item.getParent().moveItemToGroup(item, item.getParent().getParent());
                                dialog.dispose();
                            }
                        });
                        advancedMenu.add(ungroupItem);                        
                    }

                }
                return advancedMenu;
            }
        });
      
        refresh();
    }

    public final void refresh() {
        if (item.getParent() == null) {
            return;
        }
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
