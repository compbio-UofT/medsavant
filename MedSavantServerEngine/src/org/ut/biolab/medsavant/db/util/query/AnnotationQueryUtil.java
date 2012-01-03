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

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import java.rmi.RemoteException;
import org.ut.biolab.medsavant.db.BinaryConditionMS;
import org.xml.sax.SAXException;

import org.ut.biolab.medsavant.db.format.AnnotationFormat;
import org.ut.biolab.medsavant.db.log.DBLogger;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.format.AnnotationFormat.AnnotationType;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.AnnotationTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.AnnotationFormatTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.ReferenceTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.VariantTablemapTableSchema;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.model.Annotation;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.query.api.AnnotationQueryUtilAdapter;

/**
 *
 * @author mfiume
 */
public class AnnotationQueryUtil extends java.rmi.server.UnicastRemoteObject implements AnnotationQueryUtilAdapter {

    private static AnnotationQueryUtil instance;

    public static AnnotationQueryUtil getInstance() throws RemoteException {
        if (instance == null) {
            instance = new AnnotationQueryUtil();
        }
        return instance;
    }

    public AnnotationQueryUtil() throws RemoteException {}

    public List<Annotation> getAnnotations(String sid) throws SQLException {

        TableSchema refTable = MedSavantDatabase.ReferenceTableSchema;
        TableSchema annTable = MedSavantDatabase.AnnotationTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(annTable.getTable());
        query.addAllColumns();
        query.addJoin(
                SelectQuery.JoinType.LEFT_OUTER,
                annTable.getTable(),
                refTable.getTable(),
                BinaryConditionMS.equalTo(
                    annTable.getDBColumn(AnnotationTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                    refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID)));

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());

        List<Annotation> results = new ArrayList<Annotation>();

        while (rs.next()) {
            results.add(new Annotation(
                    rs.getInt(AnnotationTableSchema.COLUMNNAME_OF_ANNOTATION_ID),
                    rs.getString(AnnotationTableSchema.COLUMNNAME_OF_PROGRAM),
                    rs.getString(AnnotationTableSchema.COLUMNNAME_OF_VERSION),
                    rs.getInt(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                    rs.getString(ReferenceTableSchema.COLUMNNAME_OF_NAME),
                    rs.getString(AnnotationTableSchema.COLUMNNAME_OF_PATH),
                    AnnotationFormat.intToAnnotationType(rs.getInt(AnnotationTableSchema.COLUMNNAME_OF_TYPE))));
        }

        return results;
    }

    public Annotation getAnnotation(String sid,int annotation_id) throws SQLException {

        TableSchema refTable = MedSavantDatabase.ReferenceTableSchema;
        TableSchema annTable = MedSavantDatabase.AnnotationTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(annTable.getTable());
        query.addAllColumns();
        query.addJoin(
                SelectQuery.JoinType.LEFT_OUTER,
                annTable.getTable(),
                refTable.getTable(),
                BinaryConditionMS.equalTo(
                    annTable.getDBColumn(AnnotationTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                    refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID)));
        query.addCondition(BinaryConditionMS.equalTo(annTable.getDBColumn(AnnotationTableSchema.COLUMNNAME_OF_ANNOTATION_ID), annotation_id));

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());

        rs.next();
        Annotation result = new Annotation(
                    rs.getInt(AnnotationTableSchema.COLUMNNAME_OF_ANNOTATION_ID),
                    rs.getString(AnnotationTableSchema.COLUMNNAME_OF_PROGRAM),
                    rs.getString(AnnotationTableSchema.COLUMNNAME_OF_VERSION),
                    rs.getInt(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                    rs.getString(ReferenceTableSchema.COLUMNNAME_OF_NAME),
                    rs.getString(AnnotationTableSchema.COLUMNNAME_OF_PATH),
                    AnnotationFormat.intToAnnotationType(rs.getInt(AnnotationTableSchema.COLUMNNAME_OF_TYPE)));

        return result;
    }

    public int[] getAnnotationIds(String sid,int projectId, int referenceId) throws SQLException {

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_ANNOTATION_IDS));
        query.addCondition(ComboCondition.and(
                BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId),
                BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId)));


        String a = query.toString();
        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());

        rs.next();
        String annotationString = rs.getString(VariantTablemapTableSchema.COLUMNNAME_OF_ANNOTATION_IDS);

        if (annotationString == null || annotationString.isEmpty()) {
            return new int[0];
        }

        String[] split = annotationString.split(",");
        int[] result = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            result[i] = Integer.parseInt(split[i]);
        }

        return result;
    }

    public AnnotationFormat getAnnotationFormat(String sid,int annotationId) throws SQLException, IOException, ParserConfigurationException, SAXException {

        TableSchema annTable = MedSavantDatabase.AnnotationTableSchema;
        SelectQuery query1 = new SelectQuery();
        query1.addFromTable(annTable.getTable());
        query1.addAllColumns();
        query1.addCondition(BinaryConditionMS.equalTo(annTable.getDBColumn(AnnotationTableSchema.COLUMNNAME_OF_ANNOTATION_ID), annotationId));

        ResultSet rs1 = ConnectionController.connectPooled(sid).createStatement().executeQuery(query1.toString());

        rs1.next();

        String program = rs1.getString(AnnotationTableSchema.COLUMNNAME_OF_PROGRAM);
        String version = rs1.getString(AnnotationTableSchema.COLUMNNAME_OF_VERSION);
        int referenceId = rs1.getInt(AnnotationTableSchema.COLUMNNAME_OF_REFERENCE_ID);
        String path = rs1.getString(AnnotationTableSchema.COLUMNNAME_OF_PATH);
        boolean hasRef = rs1.getBoolean(AnnotationTableSchema.COLUMNNAME_OF_HAS_REF);
        boolean hasAlt = rs1.getBoolean(AnnotationTableSchema.COLUMNNAME_OF_HAS_ALT);
        AnnotationType type = AnnotationFormat.intToAnnotationType(rs1.getInt(AnnotationTableSchema.COLUMNNAME_OF_TYPE));


        TableSchema annFormatTable = MedSavantDatabase.AnnotationformatTableSchema;
        SelectQuery query2 = new SelectQuery();
        query2.addFromTable(annFormatTable.getTable());
        query2.addAllColumns();
        query2.addCondition(BinaryConditionMS.equalTo(annFormatTable.getDBColumn(AnnotationFormatTableSchema.COLUMNNAME_OF_ANNOTATION_ID), annotationId));
        query2.addOrdering(annFormatTable.getDBColumn(AnnotationFormatTableSchema.COLUMNNAME_OF_POSITION), Dir.ASCENDING);

        ResultSet rs2 = ConnectionController.connectPooled(sid).createStatement().executeQuery(query2.toString());

        List<CustomField> fields = new ArrayList<CustomField>();
        while(rs2.next()){
            fields.add(new CustomField(
                    rs2.getString(AnnotationFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME),
                    rs2.getString(AnnotationFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE),
                    rs2.getBoolean(AnnotationFormatTableSchema.COLUMNNAME_OF_FILTERABLE),
                    rs2.getString(AnnotationFormatTableSchema.COLUMNNAME_OF_ALIAS),
                    rs2.getString(AnnotationFormatTableSchema.COLUMNNAME_OF_DESCRIPTION)));
        }

        return new AnnotationFormat(program, version, referenceId, path, hasRef, hasAlt, type, fields);
    }


    public int addAnnotation(String sid,String program, String version, int referenceid, String path, boolean hasRef, boolean hasAlt, int type) throws SQLException {

        DBLogger.log("Adding annotation...");

        TableSchema table = MedSavantDatabase.AnnotationTableSchema;
        InsertQuery query = new InsertQuery(table.getTable());
        query.addColumn(table.getDBColumn(AnnotationTableSchema.COLUMNNAME_OF_PROGRAM), program);
        query.addColumn(table.getDBColumn(AnnotationTableSchema.COLUMNNAME_OF_VERSION), version);
        query.addColumn(table.getDBColumn(AnnotationTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceid);
        query.addColumn(table.getDBColumn(AnnotationTableSchema.COLUMNNAME_OF_PATH), path);
        query.addColumn(table.getDBColumn(AnnotationTableSchema.COLUMNNAME_OF_HAS_REF), hasRef);
        query.addColumn(table.getDBColumn(AnnotationTableSchema.COLUMNNAME_OF_HAS_ALT), hasAlt);
        query.addColumn(table.getDBColumn(AnnotationTableSchema.COLUMNNAME_OF_TYPE), type);

        PreparedStatement stmt = (ConnectionController.connectPooled(sid)).prepareStatement(
                query.toString(),
                Statement.RETURN_GENERATED_KEYS);

        stmt.execute();
        ResultSet res = stmt.getGeneratedKeys();
        res.next();

        int annotid = res.getInt(1);

        return annotid;
    }

    public void addAnnotationFormat(String sid,int annotationId, int position, String columnName, String columnType, boolean isFilterable, String alias, String description) throws SQLException {

        TableSchema table = MedSavantDatabase.AnnotationformatTableSchema;
        InsertQuery query = new InsertQuery(table.getTable());
        query.addColumn(table.getDBColumn(AnnotationFormatTableSchema.COLUMNNAME_OF_ANNOTATION_ID), annotationId);
        query.addColumn(table.getDBColumn(AnnotationFormatTableSchema.COLUMNNAME_OF_POSITION), position);
        query.addColumn(table.getDBColumn(AnnotationFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME), columnName);
        query.addColumn(table.getDBColumn(AnnotationFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE), columnType);
        query.addColumn(table.getDBColumn(AnnotationFormatTableSchema.COLUMNNAME_OF_FILTERABLE), isFilterable);
        query.addColumn(table.getDBColumn(AnnotationFormatTableSchema.COLUMNNAME_OF_ALIAS), alias);
        query.addColumn(table.getDBColumn(AnnotationFormatTableSchema.COLUMNNAME_OF_DESCRIPTION), description);

        ConnectionController.connectPooled(sid).createStatement().executeUpdate(query.toString());
    }

}
