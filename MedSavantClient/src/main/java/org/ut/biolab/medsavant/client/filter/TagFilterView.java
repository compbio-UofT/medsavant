/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.filter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.FilterStateAdapter;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.VariantTag;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class TagFilterView extends FilterView {
    private static final Log LOG = LogFactory.getLog(TagFilterView.class);
    public static final String FILTER_NAME = "Tag Filter";
    public static final String FILTER_ID = "tag_filter";

    private List<VariantTag> variantTags;
    private List<VariantTag> appliedTags;
    private ActionListener al;
    private JTextArea ta;
    private final JButton clear;
    private final JButton applyButton;

    public TagFilterView(FilterState state, int queryID) {
        this(queryID);
        List<String> values = state.getValues("value");
        if (values != null) {
            applyFilter(unwrapValues(values));
        }
    }

    public TagFilterView(int queryID) {
        super(FILTER_NAME, queryID);

        setLayout(new BorderLayout());
        setBorder(ViewUtil.getBigBorder());
        setMaximumSize(new Dimension(200, 80));

        JPanel cp = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0; c.gridy = 0;

        cp.setLayout(gbl);

        variantTags = new ArrayList<VariantTag>();
        appliedTags = new ArrayList<VariantTag>();

        clear = new JButton("Clear");
        applyButton = new JButton("Apply");

        try {
            final JComboBox tagNameCB = new JComboBox();
            //tagNameCB.setMaximumSize(new Dimension(1000,30));

            final JComboBox tagValueCB = new JComboBox();
            //tagValueCB.setMaximumSize(new Dimension(1000,30));

            JPanel bottomContainer = new JPanel();
            ViewUtil.applyHorizontalBoxLayout(bottomContainer);

            List<String> tagNames = MedSavantClient.VariantManager.getDistinctTagNames(LoginController.getSessionID());

            for (String tag : tagNames) {
                tagNameCB.addItem(tag);
            }

            ta = new JTextArea();
            ta.setRows(10);
            ta.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            ta.setEditable(false);


            applyButton.setEnabled(false);

            JLabel addButton = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD));
            addButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {

                    if (tagNameCB.getSelectedItem() == null || tagValueCB.getSelectedItem() == null) {
                        return;
                    }

                    VariantTag tag = new VariantTag((String) tagNameCB.getSelectedItem(), (String) tagValueCB.getSelectedItem());
                    if (variantTags.isEmpty()) {
                        ta.append(tag.toString() + "\n");
                    } else {
                        ta.append("AND " + tag.toString() + "\n");
                    }
                    variantTags.add(tag);
                    applyButton.setEnabled(true);
                }
            });

            clear.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    variantTags.clear();
                    ta.setText("");
                    applyButton.setEnabled(true);
                }
            });

            int width = 150;

            ta.setPreferredSize(new Dimension(width,width));
            ta.setMaximumSize(new Dimension(width,width));
            tagNameCB.setPreferredSize(new Dimension(width,25));
            tagValueCB.setPreferredSize(new Dimension(width,25));
            tagNameCB.setMaximumSize(new Dimension(width,25));
            tagValueCB.setMaximumSize(new Dimension(width,25));

            cp.add(new JLabel("Name"), c);
            c.gridx++;
            cp.add(tagNameCB,c);
            c.gridx++;

            c.gridx = 0;
            c.gridy++;

            cp.add(new JLabel("Value"),c);
            c.gridx++;
            cp.add(tagValueCB,c);
            c.gridx++;
            cp.add(addButton,c);

            tagNameCB.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent ie) {
                    updateTagValues((String) tagNameCB.getSelectedItem(), tagValueCB);
                }
            });

            if (tagNameCB.getItemCount() > 0) {
                tagNameCB.setSelectedIndex(0);
                updateTagValues((String) tagNameCB.getSelectedItem(), tagValueCB);
            }

            al = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    applyButton.setEnabled(false);

                    appliedTags = new ArrayList<VariantTag>(variantTags);

                    Filter f = new Filter() {

                        @Override
                        public Condition[] getConditions() {
                            try {
                                List<Integer> uploadIDs = MedSavantClient.VariantManager.getUploadIDsMatchingVariantTags(LoginController.getSessionID(), TagFilterView.tagsToStringArray(variantTags));

                                Condition[] uploadIDConditions = new Condition[uploadIDs.size()];

                                TableSchema table = MedSavantClient.CustomTablesManager.getCustomTableSchema(LoginController.getSessionID(), MedSavantClient.ProjectManager.getVariantTableName(
                                         LoginController.getSessionID(),
                                         ProjectController.getInstance().getCurrentProjectID(),
                                         ReferenceController.getInstance().getCurrentReferenceID(),
                                         true));

                                for (int i = 0; i < uploadIDs.size(); i++) {
                                    uploadIDConditions[i] = BinaryCondition.equalTo(table.getDBColumn(BasicVariantColumns.UPLOAD_ID), uploadIDs.get(i));
                                }

                                return new Condition[] {ComboCondition.or(uploadIDConditions) };
                            } catch (Exception ex) {
                                ClientMiscUtils.reportError("Error getting upload IDs: %s", ex);
                            }
                            return new Condition[0];
                        }

                        @Override
                        public String getName() {
                            return FILTER_NAME;
                        }

                        @Override
                        public String getID() {
                            return FILTER_ID;
                        }
                    };
                    FilterController.getInstance().addFilter(f, TagFilterView.this.queryID);
                }
                };

            applyButton.addActionListener(al);

            bottomContainer.add(Box.createHorizontalGlue());
            bottomContainer.add(clear);
            bottomContainer.add(applyButton);


            add(ViewUtil.getClearBorderedScrollPane(ta), BorderLayout.CENTER);
            add(bottomContainer,BorderLayout.SOUTH);

        } catch (Exception ex) {
            ClientMiscUtils.checkSQLException(ex);
        }

        add(cp, BorderLayout.NORTH);

        this.showViewCard();
    }

    public final void applyFilter(List<VariantTag> tags) {
        this.variantTags = tags;
        ta.setText("");
        for (int i = 0; i < variantTags.size(); i++) {
            VariantTag tag = variantTags.get(i);
            if (i == 0) {
                ta.append(tag.toString() + "\n");
            } else {
                ta.append("AND " + tag.toString() + "\n");
            }
        }
        al.actionPerformed(null);
    }

    private static void updateTagValues(String tagName, JComboBox tagValueCB) {

        tagValueCB.removeAllItems();

        if (tagName != null) {
            List<String> values;
            try {
                values = MedSavantClient.VariantManager.getValuesForTagName(LoginController.getSessionID(), tagName);
                for (String val : values) {
                    tagValueCB.addItem(val);
                }
            } catch (Exception ex) {
                ClientMiscUtils.reportError("Error updating tag values: %s", ex);
            }
        }

        tagValueCB.updateUI();
    }

    private static String[][] tagsToStringArray(List<VariantTag> variantTags) {

        String[][] result = new String[variantTags.size()][2];

        int row = 0;
        for (VariantTag t : variantTags) {
            result[row][0] = t.key;
            result[row][1] = t.value;
            row++;
        }

        return result;
    }

    public static FilterStateAdapter wrapState(List<VariantTag> applied) {
        FilterState state = new FilterState(Filter.Type.TAG, FILTER_NAME, FILTER_ID);
        state.putValues(FilterState.VALUE_ELEMENT, wrapValues(applied));
        return state;
    }

    @Override
    public FilterStateAdapter saveState() {
        return wrapState(appliedTags);
    }

    /**
     * Take a list of strings which have been pulled in from the XML file and convert them into <c>VariantTag</c>s.
     * @param strings
     */
    private List<VariantTag> unwrapValues(List<String> strings) {
        List<VariantTag> tags = new ArrayList<VariantTag>(strings.size());
        for (String s: strings) {
            tags.add(VariantTag.fromString(s));
        }
        return tags;
    }
}
