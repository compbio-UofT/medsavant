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
package org.ut.biolab.medsavant.client.query.medsavant.complex;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.geneset.GeneSetController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.util.MiscUtils;
import org.ut.biolab.medsavant.client.query.SearchConditionItem;
import org.ut.biolab.medsavant.client.query.medsavant.MedSavantConditionViewGenerator;
import org.ut.biolab.medsavant.client.query.value.encode.StringConditionEncoder;
import org.ut.biolab.medsavant.client.query.view.GeneSearchConditionEditorView;
import org.ut.biolab.medsavant.client.query.view.SearchConditionEditorView;
import savant.api.util.DialogUtils;

/**
 *
 * @author jim
 */
public class GenesConditionGenerator implements ComprehensiveConditionGenerator {

    private static final Log LOG = LogFactory.getLog(GenesConditionGenerator.class);
    private static final String NAME = "Genes";
    private static int[] REFRESH_KEY_CODES = {KeyEvent.VK_ENTER, KeyEvent.VK_SPACE, KeyEvent.VK_TAB, KeyEvent.VK_PASTE};
    private static String GENE_DELIMITER = "\\s+|\\t+|,|\\n|\\r\\n";
    private static final Color GENE_INVALID_COLOR = Color.RED;
    private static final Color GENE_VALID_COLOR = Color.BLACK;
    private static final Dimension PREFERRED_SIZE = new Dimension(80, 150);

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String category() {
        return MedSavantConditionViewGenerator.REGIONBASED_CONDITIONS;
    }

    @Override
    public Condition getConditionsFromEncoding(String encoding) throws Exception {
        List<String> geneNames = StringConditionEncoder.unencodeConditions(encoding);
        Condition[] conditions = new Condition[geneNames.size()];
        int i = 0;
        for (String geneName : geneNames) {
            Gene gene = GeneSetController.getInstance().getGene(geneName);
            if (gene == null) {
                LOG.error("Search error, the gene " + geneName + " is no longer valid");
                DialogUtils.displayError("Search Error", "The gene " + geneName + " is no longer valid, returning unfiltered variants.");
                return null;
            } else {
                //System.out.println("checking " + BasicVariantColumns.CHROM.getColumnName() + " = " + gene.getChrom() + ", " + BasicVariantColumns.POSITION.getColumnName() + " >= " + gene.getStart() + ", " + BasicVariantColumns.POSITION.getColumnName() + " <= " + gene.getEnd());
                TableSchema ts = ProjectController.getInstance().getCurrentVariantTableSchema();
                conditions[i] = ComboCondition.and(
                        BinaryCondition.equalTo(ts.getDBColumn(BasicVariantColumns.CHROM), gene.getChrom()),
                        MiscUtils.getIntersectCondition(
                            gene.getStart(), 
                            gene.getEnd(), 
                            ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(BasicVariantColumns.START_POSITION), 
                            ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(BasicVariantColumns.END_POSITION)));                                        
            }
            i++;
        }
        return ComboCondition.or(conditions);
    }

    @Override
    public SearchConditionEditorView getViewGeneratorForItem(SearchConditionItem item) {
        return new GeneSearchConditionEditorView(item);
    }
}
