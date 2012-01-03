/*
 *    Copyright 2011 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.db.util.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.SelectQuery;

import java.rmi.RemoteException;
import org.ut.biolab.medsavant.db.util.shared.BinaryConditionMS;
import org.ut.biolab.medsavant.db.model.Chromosome;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.AnnotationTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.ChromosomeTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.ReferenceTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.VariantTablemapTableSchema;
import org.ut.biolab.medsavant.db.model.Reference;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.query.api.ReferenceQueryUtilAdapter;

/**
 *
 * @author mfiume
 */
public class ReferenceQueryUtil extends java.rmi.server.UnicastRemoteObject implements ReferenceQueryUtilAdapter {

    private static ReferenceQueryUtil instance;

    public static ReferenceQueryUtil getInstance() throws RemoteException {
        if (instance == null) {
            instance = new ReferenceQueryUtil();
        }
        return instance;
    }

    public ReferenceQueryUtil() throws RemoteException {}


    public List<Reference> getReferences(String sid) throws SQLException {

        TableSchema table = MedSavantDatabase.ReferenceTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(
                table.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                table.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_NAME));

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());

        List<Reference> results = new ArrayList<Reference>();
        while(rs.next()) {
            results.add(new Reference(
                    rs.getInt(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                    rs.getString(ReferenceTableSchema.COLUMNNAME_OF_NAME)));
        }

        return results;
    }

    public List<String> getReferenceNames(String sid) throws SQLException {

        TableSchema table = MedSavantDatabase.ReferenceTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_NAME));

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());

        List<String> results = new ArrayList<String>();
        while (rs.next()) {
            results.add(rs.getString(1));
        }

        return results;
    }

    public int getReferenceId(String sid,String refName) throws SQLException {

        TableSchema table = MedSavantDatabase.ReferenceTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_NAME), refName));

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());

        if (rs.next()) {
            return rs.getInt(1);
        } else {
            return -1;
        }
    }

     public boolean containsReference(String sid,String name) throws SQLException {

        TableSchema table = MedSavantDatabase.ReferenceTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_NAME), name));

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());

        return rs.next();
    }

    public int addReference(String sid,String name, List<Chromosome> contigs) throws SQLException {
        return addReference(sid,name, contigs, null);
    }

    public int addReference(String sid,String name, List<Chromosome> contigs, String url) throws SQLException {

        TableSchema referenceTable = MedSavantDatabase.ReferenceTableSchema;
        TableSchema chromTable = MedSavantDatabase.ChromosomeTableSchema;
        Connection c = ConnectionController.connectPooled(sid);

        //add reference
        InsertQuery query1 = new InsertQuery(referenceTable.getTable());
        query1.addColumn(referenceTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_NAME), name);
        query1.addColumn(referenceTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_URL), url);

        PreparedStatement stmt = c.prepareStatement(query1.toString(), Statement.RETURN_GENERATED_KEYS);

        stmt.execute();
        ResultSet res = stmt.getGeneratedKeys();
        res.next();

        int refid = res.getInt(1);

        //add contigs
        c.setAutoCommit(false);
        for(int i = 0; i < contigs.size(); i++){
            Chromosome chrom = contigs.get(i);
            InsertQuery query = new InsertQuery(chromTable.getTable());
            query.addColumn(chromTable.getDBColumn(ChromosomeTableSchema.COLUMNNAME_OF_REFERENCE_ID), refid);
            query.addColumn(chromTable.getDBColumn(ChromosomeTableSchema.COLUMNNAME_OF_CONTIG_ID), i);
            query.addColumn(chromTable.getDBColumn(ChromosomeTableSchema.COLUMNNAME_OF_CONTIG_NAME), chrom.getName());
            query.addColumn(chromTable.getDBColumn(ChromosomeTableSchema.COLUMNNAME_OF_CONTIG_LENGTH), chrom.getLength());
            query.addColumn(chromTable.getDBColumn(ChromosomeTableSchema.COLUMNNAME_OF_CENTROMERE_POS), chrom.getCentromerepos());
            c.createStatement().executeUpdate(query.toString());
        }
        c.commit();
        c.setAutoCommit(true);

        return refid;
    }

     public boolean removeReference(String sid,int refid) throws SQLException {

         TableSchema annotationTable = MedSavantDatabase.AnnotationTableSchema;
         TableSchema variantMapTable = MedSavantDatabase.VarianttablemapTableSchema;
         TableSchema refTable = MedSavantDatabase.ReferenceTableSchema;
         TableSchema chromTable = MedSavantDatabase.ChromosomeTableSchema;

         Connection c = ConnectionController.connectPooled(sid);

         SelectQuery q1 = new SelectQuery();
         q1.addFromTable(annotationTable.getTable());
         q1.addAllColumns();
         q1.addCondition(BinaryConditionMS.equalTo(annotationTable.getDBColumn(AnnotationTableSchema.COLUMNNAME_OF_REFERENCE_ID), refid));
         ResultSet rs = c.createStatement().executeQuery(q1.toString());
         if (rs.next()) { return false; }

         SelectQuery q2 = new SelectQuery();
         q2.addFromTable(variantMapTable.getTable());
         q2.addAllColumns();
         q2.addCondition(BinaryConditionMS.equalTo(variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refid));
         rs = c.createStatement().executeQuery(q2.toString());
         if (rs.next()) { return false; }

         DeleteQuery q3 = new DeleteQuery(refTable.getTable());
         q3.addCondition(BinaryConditionMS.equalTo(refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID), refid));
         c.createStatement().execute(q3.toString());

         DeleteQuery q4 = new DeleteQuery(chromTable.getTable());
         q4.addCondition(BinaryConditionMS.equalTo(chromTable.getDBColumn(ChromosomeTableSchema.COLUMNNAME_OF_REFERENCE_ID), refid));
         c.createStatement().execute(q4.toString());

         return true;
    }

    public List<String> getReferencesForProject(String sid,int projectid) throws SQLException {

        TableSchema refTable = MedSavantDatabase.ReferenceTableSchema;
        TableSchema variantMapTable = MedSavantDatabase.VarianttablemapTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(variantMapTable.getTable());
        query.addColumns(refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_NAME));
        query.addJoin(SelectQuery.JoinType.LEFT_OUTER,
                variantMapTable.getTable(),
                refTable.getTable(),
                BinaryConditionMS.equalTo(
                        refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                        variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID)));
        query.addCondition(BinaryConditionMS.equalTo(variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());

        List<String> references = new ArrayList<String>();
        while (rs.next()) {
            references.add(rs.getString(1));
        }

        return references;
    }

    public List<Integer> getReferenceIdsForProject(String sid,int projectid) throws SQLException {

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());

        List<Integer> references = new ArrayList<Integer>();
        while (rs.next()) {
            references.add(rs.getInt(1));
        }

        return references;
    }

    public Map<Integer, String> getReferencesWithoutTablesInProject(String sid,int projectid) throws SQLException {

        Connection c = org.ut.biolab.medsavant.db.util.ConnectionController.connectPooled(sid);
        ResultSet rs = c.createStatement().executeQuery(
                "SELECT *"
                + " FROM " + ReferenceTableSchema.TABLE_NAME
                + " WHERE " + ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID + " NOT IN"
                + " (SELECT " + VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID + " FROM " + VariantTablemapTableSchema.TABLE_NAME
                + " WHERE " + VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID + "=" + projectid + ")");

        HashMap<Integer,String> result = new HashMap<Integer,String>();

        while (rs.next()) {
            result.put(rs.getInt(1), rs.getString(2));
        }

        return result;
    }

    public String getReferenceUrl(String sid,int referenceid) throws SQLException {

        Connection c = ConnectionController.connectPooled(sid);

        TableSchema refTable = MedSavantDatabase.ReferenceTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(refTable.getTable());
        query.addColumns(refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_URL));
        query.addCondition(BinaryConditionMS.equalTo(refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceid));

        ResultSet rs = c.createStatement().executeQuery(query.toString());
        rs.next();
        return rs.getString(1);
    }

    public List<Chromosome> getChromosomes(String sid,int referenceid) throws SQLException {

        Connection c = ConnectionController.connectPooled(sid);

        TableSchema table = MedSavantDatabase.ChromosomeTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ChromosomeTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceid));
        query.addOrdering(table.getDBColumn(ChromosomeTableSchema.COLUMNNAME_OF_CONTIG_ID), Dir.ASCENDING);

        ResultSet rs = c.createStatement().executeQuery(query.toString());

        List<Chromosome> result = new ArrayList<Chromosome>();
        while(rs.next()){
            result.add(new Chromosome(
                    rs.getString(ChromosomeTableSchema.COLUMNNAME_OF_CONTIG_NAME),
                    rs.getString(ChromosomeTableSchema.COLUMNNAME_OF_CONTIG_NAME),
                    rs.getLong(ChromosomeTableSchema.COLUMNNAME_OF_CENTROMERE_POS),
                    rs.getLong(ChromosomeTableSchema.COLUMNNAME_OF_CONTIG_LENGTH)));
        }
        return result;
    }

}
