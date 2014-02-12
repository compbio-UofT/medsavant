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
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.List;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.ut.biolab.medsavant.client.filter.SearchBar;
import org.ut.biolab.medsavant.client.view.SplitScreenPanel;
import static org.ut.biolab.medsavant.client.view.genetics.variantinfo.OtherIndividualsSubInspector.MAXIMIUM_VARIANTS_TO_FETCH;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;
import org.ut.biolab.mfiume.query.QueryViewController;

/**
 *
 * @author jim
 */
public class OtherIndividualsVariantSubInspector extends OtherIndividualsSubInspector {

    private static final String[] COLUMN_NAMES_IN_AGGREGATE_TABLE = new String[]{"Ref.", "Alt.", BasicVariantColumns.ZYGOSITY.getAlias()};
    private static final int MAX_POSITION_STRLENGTH_TODISPLAY = 30;
    private SimpleVariant currentVariant;
    public OtherIndividualsVariantSubInspector(SplitScreenPanel splitScreenPanel) {
        super(splitScreenPanel);
        VariantFrequencyAggregatePane ap = new VariantFrequencyAggregatePane(COLUMN_NAMES_IN_AGGREGATE_TABLE) {
            @Override
            public Object[] getRow(Cohort cohort, String familyId, VariantRecord variantRecord) {
                return new Object[]{
                    variantRecord.getRef(),
                    variantRecord.getAlt(),
                    variantRecord.getZygosity()
                };
            }

            @Override
            public String getTitle(String currentFirstColumn) {
                String position = "this position";
                if(currentVariant != null){
                    position = "Position "+NumberFormat.getNumberInstance().format(currentVariant.getGenomicRegion().getStart());
                }
                if(position.length() > MAX_POSITION_STRLENGTH_TODISPLAY){
                    position = "this position";
                }

                if (currentFirstColumn.equals(BasicPatientColumns.FAMILY_ID.getAlias())) {
                    return "Variants at "+position+" by Family";
                } else {
                    return "Variants at "+position+" by Cohort";
                }
            }

            @Override
            public void selectVariant(VariantRecord variantRecord) {
                OtherIndividualsVariantSubInspector.this.selectVariant(variantRecord);
            }
        };
        
        init(ap);
    }

    @Override
    public String getName() {
        return "Individuals with a variant at this position";
    }

    @Override
    protected JPanel getIndividualSummaryPanel(String dnaID){
        return new VariantSummaryPanel(dnaID);
    }

    @Override
    public boolean setObject(Object obj) {
        if (obj instanceof SimpleVariant) {
            if(this.currentVariant == (SimpleVariant)obj){
                return false;
            }else{
                this.currentVariant = (SimpleVariant) obj;
                return true;
            }
        }
        return false;
    }

    @Override
    protected synchronized List<Object[]> getQueryResults(){
        QueryViewController qvc = SearchBar.getInstance().getQueryViewController();
        List<Object[]> results = qvc.restrictToRegion(currentVariant.getGenomicRegion(), currentVariant.getAlternate(), MAXIMIUM_VARIANTS_TO_FETCH);
        return results;
    }

    private class VariantSummaryPanel extends JPanel {

        public VariantSummaryPanel(String dnaID) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            //Map<String, Map<String, VariantRecord>> dnaIDVariantMap

            //Map<String, VariantRecord> typeVariantMap = dnaIDVariantMap.get(dnaID);
            Set<VariantRecord> variantRecords = dnaIDVariantMap.get(dnaID);

            for (final VariantRecord variantRecord : variantRecords) {
                int initialIndent = getFontMetrics(getFont()).charWidth('T');
                int subsequentIndent = getFontMetrics(getFont()).stringWidth("Typ");
                add(getRow("Type: " + variantRecord.getType(), initialIndent));
                add(getRow("Zygosity: " + variantRecord.getZygosity().toString(), subsequentIndent));
                add(getRow("Ref.: " + variantRecord.getRef(), subsequentIndent));
                add(getRow("Alt.: " + variantRecord.getAlt(), subsequentIndent));

                //add(getRow("Zygosity: ", e.getValue().getZygosity().toString(), "Alt.: ", e.getValue().getAlt()));
                JButton showButton = new JButton("Show");
                showButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        selectVariant(variantRecord);
                    }
                });

                JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
                buttonPanel.add(Box.createHorizontalGlue());
                buttonPanel.add(showButton);
                add(buttonPanel);
            }
        }

        private JPanel getRow(String txt, int indent) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.setBorder(new EmptyBorder(0, indent, 0, 0));
            p.add(getLabel(txt));
            p.add(Box.createHorizontalGlue());
            return p;
        }

        private JPanel getRow(String lbl1, String value1, String lbl2, String value2) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(getLabel(lbl1));
            p.add(getLabel(value1));
            p.add(Box.createHorizontalGlue());
            p.add(getLabel(lbl2));
            p.add(getLabel(value2));
            return p;
        }

        private JLabel getLabel(String txt) {
            JLabel lbl = new JLabel(txt);
            //lbl.setFont(lbl.getFont().deriveFont(Font.ITALIC));
            lbl.setToolTipText("Foo");
            lbl.setAlignmentX(1.0f);
            lbl.setAlignmentY(1.0f);
            return lbl;
        }

        private JLabel getBoldLabel(String txt) {
            JLabel lbl = new JLabel(txt);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
            return lbl;
        }
    }
}
