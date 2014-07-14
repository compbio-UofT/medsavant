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
package org.ut.biolab.medsavant.server.serverapi;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import org.medsavant.api.common.Reference;
import org.ut.biolab.medsavant.shared.model.Chromosome;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.SelectQuery;

import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.AnnotationColumns;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.ChromosomeTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.GeneSetColumns;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.ReferenceTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantTablemapTableSchema;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.PooledConnection;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.ReferenceManagerAdapter;

/**
 *
 * @author mfiume
 */
public class ReferenceManager extends MedSavantServerUnicastRemoteObject implements ReferenceManagerAdapter, GeneSetColumns {

    private static ReferenceManager instance;

    public static synchronized ReferenceManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new ReferenceManager();
        }
        return instance;
    }

    public ReferenceManager() throws RemoteException, SessionExpiredException {
        super();
    }

    @Override
    public Reference getReference(String sessID, int referenceID) throws SQLException, SessionExpiredException {
        TableSchema table = MedSavantDatabase.ReferenceTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(
                table.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                table.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_NAME));
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceID));

        ResultSet rs = null;
        try {
            rs = ConnectionController.executeQuery(sessID, query.toString());

            List<Reference> results = new ArrayList<Reference>();

            if (rs.next()) {
                return new Reference(
                        rs.getInt(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                        rs.getString(ReferenceTableSchema.COLUMNNAME_OF_NAME));
            }else{
                return null;
            }
        }finally{
            if(rs != null){
                rs.close();
            }
        }         
    }

    @Override
    public Reference[] getReferences(String sessID) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.ReferenceTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(
                table.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                table.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_NAME));

        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        List<Reference> results = new ArrayList<Reference>();
        while (rs.next()) {
            results.add(new Reference(
                    rs.getInt(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                    rs.getString(ReferenceTableSchema.COLUMNNAME_OF_NAME)));
        }

        return results.toArray(new Reference[0]);
    }

    @Override
    public String[] getReferenceNames(String sessID) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.ReferenceTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_NAME));

        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        List<String> results = new ArrayList<String>();
        while (rs.next()) {
            results.add(rs.getString(1));
        }

        return results.toArray(new String[0]);
    }

    @Override
    public int getReferenceID(String sessID, String refName) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.ReferenceTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_NAME), refName));

        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        if (rs.next()) {
            return rs.getInt(1);
        } else {
            return -1;
        }
    }

    @Override
    public boolean containsReference(String sessID, String refName) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.ReferenceTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_NAME), refName));

        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        return rs.next();
    }

    @Override
    public int addReference(String sessID, String refName, Chromosome[] chroms, String url) throws SQLException, SessionExpiredException {

        TableSchema referenceTable = MedSavantDatabase.ReferenceTableSchema;
        TableSchema chromTable = MedSavantDatabase.ChromosomeTableSchema;
        Connection c = ConnectionController.connectPooled(sessID);

        //add reference
        InsertQuery query1 = new InsertQuery(referenceTable.getTable());
        query1.addColumn(referenceTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_NAME), refName);
        query1.addColumn(referenceTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_URL), url);

        PreparedStatement stmt = c.prepareStatement(query1.toString(), Statement.RETURN_GENERATED_KEYS);

        stmt.execute();
        ResultSet rs = stmt.getGeneratedKeys();
        rs.next();

        int refID = rs.getInt(1);

        //add contigs
        c.setAutoCommit(false);
        for (int i = 0; i < chroms.length; i++) {
            Chromosome chrom = chroms[i];
            InsertQuery query = new InsertQuery(chromTable.getTable());
            query.addColumn(chromTable.getDBColumn(ChromosomeTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID);
            query.addColumn(chromTable.getDBColumn(ChromosomeTableSchema.COLUMNNAME_OF_CONTIG_ID), i);
            query.addColumn(chromTable.getDBColumn(ChromosomeTableSchema.COLUMNNAME_OF_CONTIG_NAME), chrom.getName());
            query.addColumn(chromTable.getDBColumn(ChromosomeTableSchema.COLUMNNAME_OF_CONTIG_LENGTH), chrom.getLength());
            query.addColumn(chromTable.getDBColumn(ChromosomeTableSchema.COLUMNNAME_OF_CENTROMERE_POS), chrom.getCentromerePos());
            c.createStatement().executeUpdate(query.toString());
        }
        c.commit();
        c.setAutoCommit(true);
        c.close();

        return refID;
    }

    @Override
    public boolean removeReference(String sessID, int refID) throws SQLException, SessionExpiredException {

        TableSchema annotationTable = MedSavantDatabase.AnnotationTableSchema;
        TableSchema variantMapTable = MedSavantDatabase.VarianttablemapTableSchema;
        TableSchema refTable = MedSavantDatabase.ReferenceTableSchema;
        TableSchema chromTable = MedSavantDatabase.ChromosomeTableSchema;

        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            SelectQuery q1 = new SelectQuery();
            q1.addFromTable(annotationTable.getTable());
            q1.addAllColumns();
            q1.addCondition(BinaryConditionMS.equalTo(annotationTable.getDBColumn(AnnotationColumns.REFERENCE_ID), refID));
            ResultSet rs = conn.executeQuery(q1.toString());
            if (rs.next()) {
                return false;
            }

            SelectQuery q2 = new SelectQuery();
            q2.addFromTable(variantMapTable.getTable());
            q2.addAllColumns();
            q2.addCondition(BinaryConditionMS.equalTo(variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID));
            rs = conn.executeQuery(q2.toString());
            if (rs.next()) {
                return false;
            }

            DeleteQuery q3 = new DeleteQuery(refTable.getTable());
            q3.addCondition(BinaryConditionMS.equalTo(refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID));
            conn.executeUpdate(q3.toString());

            DeleteQuery q4 = new DeleteQuery(chromTable.getTable());
            q4.addCondition(BinaryConditionMS.equalTo(chromTable.getDBColumn(ChromosomeTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID));
            conn.executeUpdate(q4.toString());
        } finally {
            conn.close();
        }

        return true;
    }

    /*    @Override
     public Map<Integer, String> getReferencesWithoutTablesInProject(String sid,int projectid) throws SQLException, SessionExpiredException {

     ResultSet rs = ConnectionController.executeQuery(sid,
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
     }*/
    @Override
    public String getReferenceUrl(String sid, int referenceid) throws SQLException, SessionExpiredException {

        TableSchema refTable = MedSavantDatabase.ReferenceTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(refTable.getTable());
        query.addColumns(refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_URL));
        query.addCondition(BinaryConditionMS.equalTo(refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceid));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());
        rs.next();
        return rs.getString(1);
    }

    @Override
    public Chromosome[] getChromosomes(String sid, int referenceid) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.ChromosomeTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ChromosomeTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceid));
        query.addOrdering(table.getDBColumn(ChromosomeTableSchema.COLUMNNAME_OF_CONTIG_ID), Dir.ASCENDING);

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<Chromosome> result = new ArrayList<Chromosome>();
        while (rs.next()) {
            result.add(new Chromosome(
                    rs.getString(ChromosomeTableSchema.COLUMNNAME_OF_CONTIG_NAME),
                    rs.getInt(ChromosomeTableSchema.COLUMNNAME_OF_CENTROMERE_POS),
                    rs.getInt(ChromosomeTableSchema.COLUMNNAME_OF_CONTIG_LENGTH)));
        }
        return result.toArray(new Chromosome[0]);
    }

    public String getReferenceName(String sid, int refID) throws SQLException, SessionExpiredException {
        TableSchema refTable = MedSavantDatabase.ReferenceTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(refTable.getTable());
        query.addColumns(refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_NAME));
        query.addCondition(BinaryConditionMS.equalTo(refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());
        rs.next();
        return rs.getString(1);
    }
}
