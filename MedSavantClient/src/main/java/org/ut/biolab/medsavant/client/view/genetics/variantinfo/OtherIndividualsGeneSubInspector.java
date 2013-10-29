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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.String;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.filter.SearchBar;
import org.ut.biolab.medsavant.client.view.SplitScreenPanel;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;
import org.ut.biolab.mfiume.query.QueryViewController;

/**
 *
 * @author jim
 */
public class OtherIndividualsGeneSubInspector extends OtherIndividualsSubInspector implements Listener<Object> {

    private static final Log LOG = LogFactory.getLog(OtherIndividualsGeneSubInspector.class);    
    private static final int MIDDLE_LEVEL_INDENT = 10;    
    private static final int MAX_GENENAME_LENGTH_TODISPLAY = 15;
    private static final String[] COLUMN_NAMES_IN_AGGREGATE_TABLE = new String[]{"Position", "Ref.", "Alt.", BasicVariantColumns.ZYGOSITY.getAlias()};
    private Gene currentGene;
  
    @Override
    protected JPanel getIndividualSummaryPanel(String dnaID) {
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));

        Set<VariantRecord> variantRecords = getVariantRecords(dnaID);


        Map<Long, Set<VariantRecord>> positionVariantMap = new TreeMap<Long, Set<VariantRecord>>();
        for (VariantRecord variantRecord : variantRecords) {
            Set<VariantRecord> variantsAtPosition = positionVariantMap.get(variantRecord.getPosition());
            if (variantsAtPosition == null) {
                variantsAtPosition = new HashSet<VariantRecord>();
            }
            variantsAtPosition.add(variantRecord);
            positionVariantMap.put(variantRecord.getPosition(), variantsAtPosition);
        }


        for (Map.Entry<Long, Set<VariantRecord>> e : positionVariantMap.entrySet()) {
            Long pos = e.getKey();
            Set<VariantRecord> variantsAtPosition = e.getValue();
            for (final VariantRecord variantRecord : variantsAtPosition) {
                final JPanel rowContainer = new JPanel();
                rowContainer.setLayout(new BoxLayout(rowContainer, BoxLayout.Y_AXIS));
                final JPanel row = new JPanel();
                row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
                row.setBorder(new EmptyBorder(0, MIDDLE_LEVEL_INDENT, 0, 0));
                final JLabel showDetailsButton = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.EXPAND));
                String posStr = NumberFormat.getNumberInstance().format(pos);
                JLabel rowTitle = new JLabel(" "+variantRecord.getZygosity().name() + " " + variantRecord.getType() + " @ " + posStr);
                row.add(showDetailsButton);
                row.add(rowTitle);
                row.add(Box.createHorizontalGlue());
                rowContainer.add(row);
                outerPanel.add(rowContainer);



                showDetailsButton.addMouseListener(new MouseAdapter() {
                    private boolean expanded = true;

                    @Override
                    public void mousePressed(MouseEvent me) {
                        if (expanded) {
                            showDetailsButton.setIcon(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.COLLAPSE));
                            int last_level_indent = MIDDLE_LEVEL_INDENT
                                    + IconFactory.getInstance().getIcon(IconFactory.StandardIcon.EXPAND).getIconWidth()
                                    + rowContainer.getFontMetrics(rowContainer.getFont()).charWidth(' ');
                            JPanel p = new JPanel();
                            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
                            p.setBorder(new EmptyBorder(0, last_level_indent, 0, 0));
                            JPanel textPanel = new JPanel();
                            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
                            textPanel.add(new JLabel("Ref: " + variantRecord.getRef()));
                            textPanel.add(Box.createHorizontalGlue());
                            p.add(textPanel);

                            textPanel = new JPanel();
                            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
                            textPanel.add(new JLabel("Alt: " + variantRecord.getAlt()));
                            textPanel.add(Box.createHorizontalGlue());
                            p.add(textPanel);

                            rowContainer.add(p);
                        } else {
                            showDetailsButton.setIcon(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.EXPAND));
                            rowContainer.removeAll();
                            rowContainer.add(row);
                        }
                        rowContainer.revalidate();
                        rowContainer.repaint();
                        expanded = !expanded;
                    }
                });

            }
        }
        return outerPanel;
    }

    @Override
    public boolean setObject(Object obj) {
        if (obj instanceof Gene) {
            if(this.currentGene == (Gene) obj){
                return false;
            }else{
                this.currentGene = (Gene) obj;
                return true;
            }
        }
        return false;
    }

    //Note that this method executes in its own thread.
    @Override
    protected synchronized List<Object[]> getQueryResults() {
        QueryViewController qvc = SearchBar.getInstance().getQueryViewController();
        GenomicRegion gr = new GenomicRegion("", currentGene.getChrom(), currentGene.getStart(), currentGene.getEnd());
        List<Object[]> results = qvc.restrictToRegion(gr, null, MAXIMIUM_VARIANTS_TO_FETCH);
        //System.out.println("OtherIndividualsGeneSubInspector: Got " + results.size() + " results.");
        return results;
    }

    @Override
    public String getName() {
        return "Individuals with a variant in this gene";
    }
        
    public OtherIndividualsGeneSubInspector(SplitScreenPanel splitScreenPanel){
        super(splitScreenPanel);
        VariantFrequencyAggregatePane ap = new VariantFrequencyAggregatePane(COLUMN_NAMES_IN_AGGREGATE_TABLE) {
            @Override
            public Object[] getRow(Cohort cohort, String familyId, VariantRecord variantRecord) {
                return new Object[]{
                    variantRecord.getPosition(),
                    variantRecord.getRef(),
                    variantRecord.getAlt(),
                    variantRecord.getZygosity()
                };
            }

            @Override
            public String getTitle(String currentFirstColumn) {
                String geneName = "this gene";
                if(currentGene != null){
                    geneName = "gene "+currentGene.getName();
                }
                if(geneName.length() > MAX_GENENAME_LENGTH_TODISPLAY){
                    geneName = "this gene";
                }
                
                if (currentFirstColumn.equals(BasicPatientColumns.FAMILY_ID.getAlias())) {
                    return "Variants within "+geneName+" by Family";
                } else {
                    return "Variants within "+geneName+" by Cohort";
                }
            }

            @Override
            public void selectVariant(VariantRecord variantRecord) {
                OtherIndividualsGeneSubInspector.this.selectVariant(variantRecord);
            }
        };

        init(ap);
    }
}