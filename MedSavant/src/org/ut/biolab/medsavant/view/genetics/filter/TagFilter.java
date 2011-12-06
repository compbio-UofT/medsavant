package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.model.structure.CustomTables;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.query.ProjectQueryUtil;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.VariantTag;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class TagFilter extends FilterView {

    public static final String FILTER_NAME = "Tag Filter";
    public static final String FILTER_ID = "tag_filter";
    //private static final String COHORT_ALL = "All Individuals";

    static FilterView getTagFilterView(int queryId) {
        return new TagFilter(queryId, new JPanel());
    }

    public TagFilter(int queryId, JPanel container) {
        super(FILTER_NAME, container);
        createContentPanel(container, queryId);
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

    private void createContentPanel(JComponent p, final int queryId) {

        p.setLayout(new BorderLayout());
        p.setBorder(ViewUtil.getBigBorder());
        p.setMaximumSize(new Dimension(1000, 80));

        JPanel content = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(content);

        final List<VariantTag> variantTags = new ArrayList<VariantTag>();

        try {
            final JComboBox tagNameCB = new JComboBox();
            //tagNameCB.setMaximumSize(new Dimension(1000,30));

            final JComboBox tagValueCB = new JComboBox();
            //tagValueCB.setMaximumSize(new Dimension(1000,30));

            JPanel bottomContainer = new JPanel();
            ViewUtil.applyHorizontalBoxLayout(bottomContainer);

            List<String> tagNames = VariantQueryUtil.getDistinctTagNames();

            for (String tag : tagNames) {
                tagNameCB.addItem(tag);
            }

            final JTextArea ta = new JTextArea();
            ta.setRows(10);
            ta.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            ta.setEditable(false);

              final JButton applyButton = new JButton("Apply");
             applyButton.setEnabled(false);

            JButton addButton = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD));
            addButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
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

            JButton clear = new JButton("Clear");
            clear.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    variantTags.clear();
                    ta.setText("");
                    applyButton.setEnabled(true);
                }
            });

            content.add(new JLabel("Name"));
            content.add(ViewUtil.getMediumSeparator());
            content.add(tagNameCB);
            content.add(ViewUtil.getMediumSeparator());
            content.add(new JLabel("Value"));
            content.add(ViewUtil.getMediumSeparator());
            content.add(tagValueCB);
            content.add(ViewUtil.getMediumSeparator());
            content.add(addButton);

            tagNameCB.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent ie) {
                    updateTagValues((String) tagNameCB.getSelectedItem(), tagValueCB);
                }
            });

            tagNameCB.setSelectedIndex(0);
            updateTagValues((String) tagNameCB.getSelectedItem(), tagValueCB);

             ActionListener al = new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    applyButton.setEnabled(false);

                    Filter f = new QueryFilter() {

                        @Override
                        public Condition[] getConditions() {
                                try {
                                    List<Integer> uploadIDs = VariantQueryUtil.getUploadIDsMatchingVariantTags(TagFilter.tagsToStringArray(variantTags));

                                    Condition[] uploadIDConditions = new Condition[uploadIDs.size()];

                                     TableSchema table = CustomTables.getCustomTableSchema(ProjectQueryUtil.getVariantTablename(
                                             ProjectController.getInstance().getCurrentProjectId(),
                                             ReferenceController.getInstance().getCurrentReferenceId()));


                                    for (int i = 0; i < uploadIDs.size(); i++) {
                                        uploadIDConditions[i] = BinaryCondition.equalTo(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_UPLOAD_ID), uploadIDs.get(i));
                                    }

                                    return new Condition[] {ComboCondition.or(uploadIDConditions) };
                                } catch (SQLException ex) {
                                    return new Condition[0];
                                }

                        }

                        @Override
                        public String getName() {
                            return FILTER_NAME;
                        }

                        @Override
                        public String getId() {
                            return FILTER_ID;
                        }
                    };
                    FilterController.addFilter(f, queryId);
                }
                };

            applyButton.addActionListener(al);

            bottomContainer.add(Box.createHorizontalGlue());
            bottomContainer.add(clear);
            bottomContainer.add(applyButton);


            p.add(ViewUtil.getClearBorderedJSP(ta), BorderLayout.CENTER);
            p.add(bottomContainer,BorderLayout.SOUTH);

        } catch (SQLException ex) {
            content.add(new JLabel("Problem getting tag information"));
        }

        p.add(content, BorderLayout.NORTH);


    }

    private static void updateTagValues(String tagName, JComboBox tagValueCB) {

        tagValueCB.removeAllItems();

        if (tagName != null) {
            List<String> values;
            try {
                values = VariantQueryUtil.getValuesForTagName(tagName);
                for (String val : values) {
                    tagValueCB.addItem(val);
                }
            } catch (SQLException ex) {
                Logger.getLogger(TagFilter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        tagValueCB.updateUI();
    }

    @Override
    public FilterState saveState() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
