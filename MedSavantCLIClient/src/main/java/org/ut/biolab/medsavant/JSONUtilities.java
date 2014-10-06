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
package org.ut.biolab.medsavant;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.UnaryCondition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.medsavant.shared.appdevapi.DBAnnotationColumns;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.SimpleVariantFile;
import org.ut.biolab.medsavant.shared.model.exception.LockException;
import org.ut.biolab.medsavant.shared.serverapi.AnnotationManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;
import static org.ut.biolab.medsavant.shared.util.ModificationType.VARIANT;
import org.ut.biolab.medsavant.shared.util.Modifier;

/**
 * Implementation of JSONUtilitiesAdapter
 *
 * @see org.ut.biolab.medsavant.JSONUtilitiesAdapter
 */
public class JSONUtilities implements JSONUtilitiesAdapter {

    private VariantManagerAdapter variantManager;
    private AnnotationManagerAdapter annotationManager;

    public JSONUtilities(VariantManagerAdapter vma, AnnotationManagerAdapter ama) {
        variantManager = vma;
        annotationManager = ama;
    }

    @Modifier(type = VARIANT)
    @Override
    public synchronized int replaceWithTransferredVCF(String sessID, int projID, int refID, List<SimpleVariantFile> files,
            int[] fileIDs, String[][] variantTags,
            String email) throws RemoteException, IOException, LockException, Exception {

        //always autopublish
        final boolean autoPublish = true;

        //never include homoref
        final boolean includeHomoRef = false;

        //always pre-annotatew ith jannovar
        final boolean preAnnotateWithJannovar = true;

        //never phase.
        final boolean doPhasing = false;
        
        //remove existing vcfs.
        variantManager.removeVariants(sessID, projID, refID, files, autoPublish, email);

        //import new variants.
        int updateId = variantManager.uploadTransferredVariants(sessID, fileIDs, projID, refID, variantTags, includeHomoRef, email, autoPublish, preAnnotateWithJannovar, doPhasing);
        return updateId;
    }

    private Condition collapseConditions(Condition[][] conditions) {
        int i = 0;
        Condition[] conditionsToOr = new Condition[conditions.length];
        for (Condition[] conditionsToAnd : conditions) {
            conditionsToOr[i++] = ComboCondition.and(conditionsToAnd);
        }

        ComboCondition c = ComboCondition.or(conditionsToOr);
        return c;
    }

    private String getDbColumnName(String sessID, int projID, int refID, String alias) throws SQLException, RemoteException, SessionExpiredException {
        List<String> t = new ArrayList<String>();

        AnnotationFormat[] afs = annotationManager.getAnnotationFormats(sessID, projID, refID);
        for (AnnotationFormat af : afs) {
            for (CustomField field : af.getCustomFields()) {
                if (field.getAlias().equalsIgnoreCase(alias)) {                    
                    return field.getColumnName();
                }
            }
        }

        return null;
    }

    /**
     * This method works around the lack of 'ComboCondition' support in the API,
     * which makes it difficult to construct the queries necessary to fetch
     * statistics from filtered data.
     *
     */
    @Override
    public JSONVariants getVariantsWithStatistics(String sessID, int projID, int refID, Condition[][] conditions, int start, int limit) throws SQLException, RemoteException, SessionExpiredException {
        //Get the variants to return
        List<Object[]> variants = variantManager.getVariants(sessID, projID, refID, conditions, start, limit);

        TableSchema tableSchema = variantManager.getCustomTableSchema(sessID, projID, refID);
        DbColumn refCol = tableSchema.getDBColumn(BasicVariantColumns.REF);
        DbColumn altCol = tableSchema.getDBColumn(BasicVariantColumns.ALT);

        //Get number of transitions
        ComboCondition transitionCondition = ComboCondition.or(
                ComboCondition.and(
                        ComboCondition.or(
                                BinaryCondition.equalTo(refCol, "A"),
                                BinaryCondition.equalTo(refCol, "G")
                        ),
                        ComboCondition.or(
                                BinaryCondition.equalTo(altCol, "A"),
                                BinaryCondition.equalTo(altCol, "G")
                        )
                ),
                ComboCondition.and(
                        ComboCondition.or(
                                BinaryCondition.equalTo(refCol, "C"),
                                BinaryCondition.equalTo(refCol, "T")
                        ),
                        ComboCondition.or(
                                BinaryCondition.equalTo(altCol, "C"),
                                BinaryCondition.equalTo(altCol, "T")
                        )
                )
        );

        //Get total number of variants
        int numVariants = variantManager.getFilteredVariantCount(sessID, projID, refID, conditions);

        //Get number of transitions.
        int numTi = variantManager.getFilteredVariantCount(sessID, projID, refID, new Condition[][]{{collapseConditions(conditions), transitionCondition}});

        //Get number of variants that overlap with DBSNP.
        int numDBSNP = 0;

        String dbSnpColumnName = this.getDbColumnName(sessID, projID, refID, DBAnnotationColumns.DBSNP_TEXT);
        DbColumn dbSnpColumn = tableSchema.getDBColumn(dbSnpColumnName);
        if (dbSnpColumn != null) {
            numDBSNP = variantManager.getFilteredVariantCount(sessID, projID, refID, new Condition[][]{{collapseConditions(conditions), UnaryCondition.isNotNull(dbSnpColumn)}});
        }

        JSONVariants jv = new JSONVariants(variants, numTi, numDBSNP, numVariants, this);
        return jv;
    }

}
