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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.genetics.filter.FilterState.FilterType;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class TagFilterView extends FilterView {

    public static final String FILTER_NAME = "Tag Filter";
    public static final String FILTER_ID = "tag_filter";
    //private static final String COHORT_ALL = "All Individuals";

    static FilterView getTagFilterView(int queryId) {
        return new TagFilterView(queryId, new JPanel());
    }


    private List<VariantTag> variantTags;
    private List<VariantTag> appliedTags;
    private ActionListener al;
    private JTextArea ta;

    public TagFilterView(FilterState state, int queryId){
        this(queryId, new JPanel());
        if(state.getValues().get("variantTags") != null){
            applyFilter(stringToVariantTags(state.getValues().get("variantTags")));
        }
    }

    public TagFilterView(int queryId, JPanel container) {
        super(FILTER_NAME, container, queryId);
        createContentPanel(container);
    }

    public void applyFilter(List<VariantTag> tags){
        this.variantTags = tags;
        ta.setText("");
        for(int i = 0; i < variantTags.size(); i++){
            VariantTag tag = variantTags.get(i);
            if(i == 0){
                ta.append(tag.toString() + "\n");
            } else {
                ta.append("AND " + tag.toString() + "\n");
            }
        }
        al.actionPerformed(null);
    }

    private void createContentPanel(JComponent p) {

        p.setLayout(new BorderLayout());
        p.setBorder(ViewUtil.getBigBorder());
        p.setMaximumSize(new Dimension(1000, 80));

        JPanel content = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(content);

        variantTags = new ArrayList<VariantTag>();
        appliedTags = new ArrayList<VariantTag>();

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

            ta = new JTextArea();
            ta.setRows(10);
            ta.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            ta.setEditable(false);

            final JButton applyButton = new JButton("Apply");
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

            if (tagNameCB.getItemCount() > 0) {
                tagNameCB.setSelectedIndex(0);
                updateTagValues((String) tagNameCB.getSelectedItem(), tagValueCB);
            }

            al = new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    applyButton.setEnabled(false);

                    appliedTags = new ArrayList<VariantTag>(variantTags);

                    Filter f = new QueryFilter() {

                        @Override
                        public Condition[] getConditions() {
                            try {
                                List<Integer> uploadIDs = VariantQueryUtil.getUploadIDsMatchingVariantTags(TagFilterView.tagsToStringArray(variantTags));

                                Condition[] uploadIDConditions = new Condition[uploadIDs.size()];

                                TableSchema table = CustomTables.getCustomTableSchema(ProjectQueryUtil.getVariantTablename(
                                         ProjectController.getInstance().getCurrentProjectId(),
                                         ReferenceController.getInstance().getCurrentReferenceId()));

                                for (int i = 0; i < uploadIDs.size(); i++) {
                                    uploadIDConditions[i] = BinaryCondition.equalTo(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_UPLOAD_ID), uploadIDs.get(i));
                                }

                                return new Condition[] {ComboCondition.or(uploadIDConditions) };
                            } catch (SQLException ex) {
                                MiscUtils.checkSQLException(ex);
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
                    FilterController.addFilter(f, getQueryId());
                }
                };

            applyButton.addActionListener(al);

            bottomContainer.add(Box.createHorizontalGlue());
            bottomContainer.add(clear);
            bottomContainer.add(applyButton);


            p.add(ViewUtil.getClearBorderedJSP(ta), BorderLayout.CENTER);
            p.add(bottomContainer,BorderLayout.SOUTH);

        } catch (SQLException ex) {
            MiscUtils.checkSQLException(ex);
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
                MiscUtils.checkSQLException(ex);
                Logger.getLogger(TagFilterView.class.getName()).log(Level.SEVERE, null, ex);
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

    @Override
    public FilterState saveState() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("variantTags", variantTagsToString(appliedTags));
        return new FilterState(FilterType.TAG, FILTER_NAME, FILTER_ID, map);
    }

    private String variantTagsToString(List<VariantTag> tags){
        String s = "";
        for(VariantTag tag : tags){
            s += tag.key + ":::" + tag.value + ";;;";
        }
        return s;
    }

    private List<VariantTag> stringToVariantTags(String s){
        List<VariantTag> list = new ArrayList<VariantTag>();
        String[] pairs = s.split(";;;");
        for(String x : pairs){
            String[] pair = x.split(":::");
            if(pair.length == 2){
                list.add(new VariantTag(pair[0], pair[1]));
            }
        }
        return list;
    }
}
