/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.api.variantstorage.impl;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.OrderObject;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Function;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.medsavant.api.common.MedSavantDatabaseException;
import org.medsavant.api.common.MedSavantServerContext;
import org.medsavant.api.common.MedSavantSession;
import org.medsavant.api.common.MedSavantUpdate;
import org.medsavant.api.common.storage.ColumnType;
import org.medsavant.api.database.CustomTableUtils;
import org.medsavant.api.database.MedSavantJDBCPooledConnection;
import org.medsavant.api.variantstorage.impl.schemas.AnnotationFormatTableSchema;
import org.medsavant.api.variantstorage.impl.schemas.DBSchemas;
import static org.medsavant.api.variantstorage.impl.schemas.DBSchemas.VariantFileTableSchema;
import org.medsavant.api.variantstorage.impl.schemas.VariantFormatTableSchema;
import org.medsavant.api.variantstorage.impl.schemas.VariantTablemapTableSchema;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.CustomField;

/**
 * variantTableSchemaFactory.get
 *
 * @author jim
 */
public class VariantTableSchemaBuilder {

    private static final Log LOG = LogFactory.getLog(VariantTableSchemaBuilder.class);
    private final String database;
    private final MedSavantUpdate update;

    private static final String ANNOTATION_ID_DELIMITER = ",";
    //approximate maximium size of the subset table, in # of variants.    
    private static final int APPROX_MAX_SUBSET_SIZE = 1000000;

    private List<CustomField> optionalFields;

    //fixed fields.
    private static int INDEX_OF_UPLOAD_ID = 0;
    private static int INDEX_OF_FILE_ID = 1;
    private static int INDEX_OF_VARIANT_ID = 2;
    private static int INDEX_OF_DNA_ID = 3;
    private static int INDEX_OF_CHROM = 4;
    private static int INDEX_OF_START_POSITION = 5;
    private static int INDEX_OF_END_POSITION = 6;
    private static int INDEX_OF_DBSNP_ID = 7;
    private static int INDEX_OF_REF = 8;
    private static int INDEX_OF_ALT = 9;
    private static int INDEX_OF_ALT_NUMBER = 10;
    private static int INDEX_OF_QUAL = 11;
    private static int INDEX_OF_FILTER = 12;
    private static int INDEX_OF_VARIANT_TYPE = 13;
    private static int INDEX_OF_ZYGOSITY = 14;
    private static int INDEX_OF_GT = 15;
    private static int INDEX_OF_CUSTOM_INFO = 16;

    static final CustomField UPLOAD_ID = new CustomField("upload_id", ColumnType.INTEGER, 11, false, true, false, null, true, "Upload ID", "");
    static final CustomField FILE_ID = new CustomField("file_id", ColumnType.INTEGER, 11, false, true, false, null, true, "File ID", "");
    static final CustomField VARIANT_ID = new CustomField("variant_id", ColumnType.INTEGER, 11, false, true, false, null, true, "Variant ID", "");
    static final CustomField DNA_ID = new CustomField("dna_id", ColumnType.VARCHAR, 100, false, true, false, null, true, "DNA ID", "");
    static final CustomField CHROM = new CustomField("chrom", ColumnType.VARCHAR, 5, false, true, false, "", true, "Chromosome", "");
    static final CustomField START_POSITION = new CustomField("position", ColumnType.INTEGER, 11, false, true, false, null, true, "Position", "");
    static final CustomField END_POSITION = new CustomField("end", ColumnType.INTEGER, 11, false, true, false, null, true, "End Position", "");
    static final CustomField DBSNP_ID = new CustomField("dbsnp_id", ColumnType.VARCHAR, 200, false, false, false, null, true, "dbSNP ID", "");
    static final CustomField REF = new CustomField("ref", ColumnType.VARCHAR, 10000, false, false, false, null, true, "Reference", "");
    static final CustomField ALT = new CustomField("alt", ColumnType.VARCHAR, 10000, false, false, false, null, true, "Alternate", "");
    static final CustomField ALT_NUMBER = new CustomField("alt_number", ColumnType.INTEGER, 11, false, true, false, null, true, "Alternate Number", "");
    static final CustomField QUAL = new CustomField("qual", ColumnType.FLOAT, 10, false, false, false, null, true, "Quality", "");
    static final CustomField FILTER = new CustomField("filter", ColumnType.VARCHAR, 500, false, false, false, null, true, "Filter", "");
    static final CustomField VARIANT_TYPE = new CustomField("variant_type", ColumnType.VARCHAR, 10, false, false, false, null, true, "Variant Type", "");
    static final CustomField ZYGOSITY = new CustomField("zygosity", ColumnType.VARCHAR, 20, false, false, false, null, true, "Zygosity", "");
    static final CustomField GT = new CustomField("gt", ColumnType.VARCHAR, 10, false, false, false, null, true, "Genotype", "");
    static final CustomField CUSTOM_INFO = new CustomField("custom_info", ColumnType.VARCHAR, 10000, false, false, false, null, false, "Custom Info", "");

    static final CustomField[] FIXED_VARIANT_FIELDS = new CustomField[]{//end position?
        UPLOAD_ID, FILE_ID, VARIANT_ID, DNA_ID, CHROM, START_POSITION, END_POSITION, DBSNP_ID, REF, ALT, ALT_NUMBER, QUAL, FILTER, VARIANT_TYPE, ZYGOSITY, GT, CUSTOM_INFO
    };
 
    /**
     * 
     * @param database The database where the variant table resides.
     * @param update The update corresponding to the variant table.
     */
    public VariantTableSchemaBuilder(String database, MedSavantUpdate update) {
        this.database = database;
        this.update = update;        
    }
    
    public VariantTableSchemaBuilder(String database){
        this.database = database;
        this.update = null;
    }
        
    
    ////// PRIVATE
    
    //VCF fields (e.g. AA, AF, AN, etc.) can be added from the client.  We can assume they all exist in the variant_format table.
    //public static final CustomField AC = new CustomField("ac", ColumnType.INTEGER, 11, false, false, false, null, true, "Allele Count", "");
    
    //Returns the tableschema corresponding to the given name.
    private TableSchema getTableSchema(MedSavantJDBCPooledConnection conn, String tableName) throws SQLException {
        return CustomTableUtils.getInstance().getCustomTableSchema(conn, database, tableName);
    }

    /*public void addFixedOptionalField(CustomField f) {
        
    }*/

       
    //UNUSED???
    /* 
    private TableSchema load(MedSavantJDBCPooledConnection conn, String columnName) throws SQLException {
        TableSchema tableSchema = DBSchemas.VarianttablemapTableSchema;
        SelectQuery sq = new SelectQuery();
        sq.addFromTable(tableSchema.getTable());
        sq.addColumns(tableSchema.getDBColumn(columnName));
        sq.addCondition(BinaryCondition.equalTo(tableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), update.getProject().getProjectId()));
        sq.addCondition(BinaryCondition.equalTo(tableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), update.getReference().getID()));
        sq.addCondition(BinaryCondition.equalTo(tableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), update.getUpdateID()));
        //if(pubStatus == PUBLISHED){            
        // sq.addCondition(BinaryCondition.equalTo(tableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), 1));
        // }else if(pubStatus == PENDING){            
        // sq.addCondition(BinaryCondition.equalTo(tableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), 0));
        // }else if(pubStatus == DELETED){
        // sq.addCondition(ComboCondition.and(
        // sq.addCondition(BinaryCondition.notEqualTo(tableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), 1)),
        // sq.addCondition(BinaryCondition.notEqualTo(tableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), 0))
        // ));
        // } 
        ResultSet rs = null;
        try {
            rs = conn.executeQuery(sq.toString());
            if (rs.next()) {
                String tableName = rs.getString(1);
                return getTableSchema(conn, tableName);                
            } else {
                throw new SQLException("No variant table was registered for " + update);
            }
        } finally {
            if (rs != null) {
                try{                    
                    rs.close();
                }catch(SQLException sqe){
                    LOG.error("Couldn't close result set, continuing anyway. ", sqe);
                }                    
            }
        }
    }
    
    //need PublicationStatus as parameter?
    public TableSchema load(MedSavantJDBCPooledConnection conn) throws SQLException {
        return load(conn, VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME);

    }

    public TableSchema loadSubset(MedSavantJDBCPooledConnection conn, int updateId) throws SQLException {
        //String tableName = DBSettings.getVariantSubsetTableName(projectId, referenceId, updateId);
        return load(conn, VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME);
    }
    */
    
    //Returns the list of variable VCF fields associated with the given update (i.e. those extracted from info fields)
    //in the order that they're stored in the variant table.
    private List<CustomField> getVCFFields(MedSavantJDBCPooledConnection conn) throws SQLException {
        TableSchema tableSchema = DBSchemas.VariantformatTableSchema;
        SelectQuery sq = new SelectQuery();
        sq.addFromTable(tableSchema.getTable());
        sq.addAllColumns(); //projectid, referenceid, updateid, order by position        
        sq.addCondition(BinaryCondition.equalTo(tableSchema.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), update.getProject().getProjectId()));
        sq.addCondition(BinaryCondition.equalTo(tableSchema.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_REFERENCE_ID), update.getReference().getID()));
        sq.addCondition(BinaryCondition.equalTo(tableSchema.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_UPDATE_ID), update.getUpdateID()));
        sq.addOrdering(tableSchema.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_POSITION), OrderObject.Dir.ASCENDING);

        List<CustomField> fields = new ArrayList<CustomField>();
        ResultSet rs = null;
        try {
            rs = conn.executeQuery(sq.toString());
            while (rs.next()) {
                /*
                 //public CustomField(String name, String typeStr, boolean filterable, String alias, String description, String tagStr, boolean nonNull) {
                 //name, ColumnType, length, autoInc?, notNull?, indexed?, String dflt, filterable?, alias, description
                 */
                //ustomField(String name, String typeStr, boolean filterable, String alias, String description) {
                String name = rs.getString(VariantFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME);
                String typeStr = rs.getString(VariantFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE);
                boolean filterable = (rs.getInt(VariantFormatTableSchema.COLUMNNAME_OF_FILTERABLE) > 0);
                String alias = rs.getString(VariantFormatTableSchema.COLUMNNAME_OF_ALIAS);
                String description = rs.getString(VariantFormatTableSchema.COLUMNNAME_OF_DESCRIPTION);
                fields.add(new CustomField(name, typeStr, filterable, alias, description));
            }
            return fields;
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    //Could move to another annotation manager?
    //Returns the field corresponding to the given annotationId.  This field has a name shared across the tables of a database, but
    //not across databases.  (Note that customField does not say anything about field ordering, or provide a column index) 
    private CustomField getCustomFieldForAnnotation(MedSavantJDBCPooledConnection conn, int annotationId) throws SQLException {
        TableSchema tableSchema = DBSchemas.AnnotationFormatTableSchema;
        SelectQuery sq = new SelectQuery();
        sq.addFromTable(tableSchema.getTable());
        sq.addCondition(BinaryCondition.equalTo(tableSchema.getDBColumn(AnnotationFormatTableSchema.COLUMN_NAME_OF_ANNOTATION_ID), annotationId));
        sq.addOrdering(tableSchema.getDBColumn(AnnotationFormatTableSchema.COLUMN_NAME_OF_POSITION), OrderObject.Dir.ASCENDING);
        sq.addAllColumns();
        ResultSet rs = null;
        try {
            rs = conn.executeQuery(sq.toString());

            String name = rs.getString(AnnotationFormatTableSchema.COLUMN_NAME_OF_COLUMN_NAME);
            String typeStr = rs.getString(AnnotationFormatTableSchema.COLUMN_NAME_OF_COLUMN_TYPE);
            boolean filterable = (rs.getInt(AnnotationFormatTableSchema.COLUMN_NAME_OF_FILTERABLE) > 0);
            String alias = rs.getString(AnnotationFormatTableSchema.COLUMN_NAME_OF_ALIAS);
            String description = rs.getString(AnnotationFormatTableSchema.COLUMN_NAME_OF_DESCRIPTION);
            return new CustomField(name, typeStr, filterable, alias, description);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    //Returns the list of annotation fields associated with the given update, in the order that they're stored in 
    //the variant table. 
    private List<CustomField> getAnnotationFields(MedSavantJDBCPooledConnection conn) throws SQLException {
        TableSchema tableSchema = DBSchemas.VarianttablemapTableSchema;
        SelectQuery sq = new SelectQuery();
        sq.addFromTable(tableSchema.getTable());
        sq.addColumns(tableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_ANNOTATION_IDS));
        sq.addCondition(BinaryCondition.equalTo(tableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), update.getProject().getProjectId()));
        sq.addCondition(BinaryCondition.equalTo(tableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), update.getReference().getID()));
        sq.addCondition(BinaryCondition.equalTo(tableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), update.getUpdateID()));

        ResultSet rs = null;
        try {
            rs = conn.executeQuery(sq.toString());
            if (rs.next()) {
                String astr = rs.getString(1);
                String[] annotationIds = astr.split(ANNOTATION_ID_DELIMITER);
                for (String annotationId : annotationIds) {
                    getCustomFieldForAnnotation(conn, Integer.parseInt(annotationId));
                }
            } else {
                throw new SQLException("Couldn't locate annotations for update with id " + update.getUpdateID() + " in database " + database);
            }
        } catch (NumberFormatException nfe) {
            LOG.error("Invalid number format for annotation ids fetched from " + tableSchema.getTableName() + " for updateId " + updateId + " in databse " + database);
            throw new SQLException("Annotation identifier format bad in " + tableSchema.getTableName() + " in database " + database + " for updateId " + updateId);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }

        List<CustomField> fields = new ArrayList<CustomField>();
        rs = null;
        try {
            rs = conn.executeQuery(sq.toString());
            while (rs.next()) {
                /*
                 //public CustomField(String name, String typeStr, boolean filterable, String alias, String description, String tagStr, boolean nonNull) {
                 //name, ColumnType, length, autoInc?, notNull?, indexed?, String dflt, filterable?, alias, description
                 */
                //ustomField(String name, String typeStr, boolean filterable, String alias, String description) {
                String name = rs.getString(VariantFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME);
                String typeStr = rs.getString(VariantFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE);
                boolean filterable = (rs.getInt(VariantFormatTableSchema.COLUMNNAME_OF_FILTERABLE) > 0);
                String alias = rs.getString(VariantFormatTableSchema.COLUMNNAME_OF_ALIAS);
                String description = rs.getString(VariantFormatTableSchema.COLUMNNAME_OF_DESCRIPTION);
                fields.add(new CustomField(name, typeStr, filterable, alias, description));
            }
            return fields;
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    //Returns the name of the last published variant table.
    private String getLastPublishedTableName(MedSavantJDBCPooledConnection conn) throws SQLException {
        TableSchema tableSchema = DBSchemas.VarianttablemapTableSchema;
        SelectQuery sq = new SelectQuery();
        sq.addColumns(
                tableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME)
        );
        sq.addCondition(BinaryCondition.equalTo(tableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), update.getProject().getProjectId()));
        sq.addCondition(BinaryCondition.equalTo(tableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), update.getReference().getID()));
        sq.addCondition(BinaryCondition.equalTo(tableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), 1));
        sq.addOrdering(tableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), OrderObject.Dir.DESCENDING);
        ResultSet rs = null;
        try {
            rs = conn.executeQuery(sq.toString());
            if (rs.next()) {
                String latestVariantTable = rs.getString(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME);
                //String latestVariantTableSubset = rs.getString(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME);
                return latestVariantTable;
            } else {
                return null;
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    //Saves the given subsetTable into the variant_tablemap.
    private synchronized void registerNewSubsetTable(MedSavantJDBCPooledConnection conn, String subsetTableName, double subMultiplier) throws SQLException {
        String latestTableName = getLastPublishedTableName(conn);

        TableSchema vmTableSchema = DBSchemas.VarianttablemapTableSchema;
        InsertQuery query = new InsertQuery(vmTableSchema);

        query.addColumn(vmTableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), update.getProject().getProjectId());
        query.addColumn(vmTableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), update.getReference().getID());
        query.addColumn(vmTableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), update.getReference().getID());
        query.addColumn(vmTableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), 0); //new tables are unpublished by default.
        query.addColumn(vmTableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME), latestTableName);
        query.addColumn(vmTableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME), subsetTableName);
        query.addColumn(vmTableSchema.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_SUBSET_MULTIPLIER), subMultiplier);

        conn.executeUpdate(query.toString());
    }

    
    /**
     * Creates a new, empty subset table based on an update.
     *
     * @param conn
     * @param csvFile
     * @return The table schema of the created table.
     * @throws SQLException
     * @throws IOException
     */
    private TableSchema createNewSubsetTable(MedSavantJDBCPooledConnection conn, double subsetMultiplier) throws SQLException, IOException {

        //Register the subset table corresponding to this update in the variant tablemap.
        String newSubsetTableName = DBSettings.getVariantSubsetTableName(update.getProject().getProjectId(), update.getReference().getID(), update.getUpdateID());
        registerNewSubsetTable(conn, newSubsetTableName, subsetMultiplier);

        //load variable VCF fields from variant_format
        List<CustomField> VCFFields = getVCFFields(conn);

        //load all annotation fields.
        List<CustomField> annotationFields = getAnnotationFields(conn);

        //Merge FIXED_VARIANT_FIELDS with VCF_Fields and annotationFields
        CustomField[] tmp = ArrayUtils.addAll(FIXED_VARIANT_FIELDS,
                VCFFields.toArray(new CustomField[VCFFields.size()]));
        CustomField[] allFields = ArrayUtils.addAll(tmp, VCFFields.toArray(new CustomField[annotationFields.size()]));

        //Create a new schema with the all of hte assembled fields.
        TableSchema newSchema = new TableSchema(DBSchemas.schema, newSubsetTableName, allFields);

        //Finally, create the table corresponding to this query.
        String updateString = newSchema.getCreateQuery() + " ENGINE=BRIGHTHOUSE DEFAULT CHARSET=latin1 COLLATE=latin1_bin;";
        conn.executeUpdate(updateString);
        return newSchema;
    }
    
    //undoes any intermediate work done by the refreshSubset method.   
    private void reverseRefreshSubset(MedSavantJDBCPooledConnection conn, TableSchema tmpTable, TableSchema subsetTable) {
        try {
            InfobrightUtils.dropTable(conn, tmpTable.getTableName());
            InfobrightUtils.dropTable(conn, subsetTable.getTableName());
        } catch (SQLException sqe) {
            LOG.error("Unable to drop tables " + tmpTable.getTableName() + " and " + subsetTable.getTableName() + " in database " + database + " -- can't clean up after failed refreshSubset");
        }
    }
    
    //Returns the *table* schema corresponding to the latest *published* update.  This table contains both published
    //and unpublished data -- the difference being that published data will be exposed in a view.  This method is 
    //private as generally most methods in the storage engine will use the view.  Direct access to the underlying table
    //is only needed for updates, all of which are mediated by this class.
    private TableSchema getLatestPublishedSchema(MedSavantJDBCPooledConnection conn) throws SQLException {
        String latestTableName = getLastPublishedTableName(conn);
        return getTableSchema(conn, latestTableName);
    }
    
    ////// PUBLIC          
    
    /**
     * Gets the latest view corresponding to the given project and reference, if it exists.  
     * 
     * @param conn The JDBC connection to use.
     * @param projectId The identifier of the project.
     * @param refId The identifier of the refernece.
     * @return The schema corresponding to the view for project projectId and reference refId.  If the view does not exist, throws an SQL exception.
     * @throws SQLException If the view does not exist, or if there's another error.
     */
    public TableSchema getLatestViewSchema(MedSavantJDBCPooledConnection conn, int projectId, int refId) throws SQLException {
        String viewName = DBSettings.getVariantViewName(projectId, refId);
        return getTableSchema(conn, viewName);        
    }
    
    /**
     * Returns the size of the subset table as a fraction of the main variant
     * view, (@see DBSettings.getVariantViewName).
     *
     * @param nv The number of variants in the main variant view.
     */
    public static double getSubsetFraction(int nv) {
        
        if (nv > APPROX_MAX_SUBSET_SIZE) {
            double fractionOfOriginalTable = APPROX_MAX_SUBSET_SIZE / (double) nv;
            return fractionOfOriginalTable;
        } else {
            return 1;
        }
    }
      
    /**
     * Refreshes the subset table.
     * 
     * @param conn
     * @param serverContext
     * @param approxTotalNumVariants
     * @throws MedSavantDatabaseException 
     */
    public void refreshSubset(MedSavantJDBCPooledConnection conn, MedSavantServerContext serverContext, int approxTotalNumVariants) throws MedSavantDatabaseException {
        if(update == null){ //should never happen.
            throw new IllegalArgumentException("Can't refresh subset -- no update specified");
        }
        TableSchema tmpTable = null;
        TableSchema subsetTable = null;
        try {
            tmpTable = InfobrightUtils.makeTemporaryVariantFileIBTable(conn);            

            //Copy the unpublished MyISAM file table to a temporary infobright table.
            SelectQuery query = new SelectQuery();
            TableSchema unpublishedFileTable = DBSchemas.VariantFileTableSchema;
            query.addAllTableColumns(unpublishedFileTable.getTable());
            query.addCondition(ComboCondition.and(
                    BinaryCondition.equalTo(unpublishedFileTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_PROJECT_ID), update.getProject().getProjectId()),
                    BinaryCondition.equalTo(unpublishedFileTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_REFERENCE_ID), update.getReference().getID())
            ));
            InfobrightUtils.copyQueryResultToNewTable(serverContext.getTemporaryFile(null), conn, query, tmpTable.getTableName());
            
            //unpublished data is written on top of the current published schema, so getting the latest published
            //schema will return all old data plus the data corresponding to the new update.
            TableSchema unpublishedVariantTable = getLatestPublishedSchema(conn);

            //Join the unpublished variants against the temporary file table to get all previously published
            //variants, plus the newly imported variants.                                    
            query = new SelectQuery();
            query.addAllTableColumns(unpublishedVariantTable.getTable());
            query.addJoin(
                    SelectQuery.JoinType.INNER,
                    unpublishedVariantTable.getTable(),
                    tmpTable.getTable(),
                    BinaryCondition.equalTo(
                            unpublishedVariantTable.getDBColumn(VariantTableSchemaBuilder.FILE_ID), tmpTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_ID)));

            //Restrict the results of the join using the condition RAND() < getSubsetFraction(totalNumVariants)... This
            //takes a uniformly random sample from the table of approximately
            //totalNumVariants*getSubsetFraction(totalNumVariants) variants.
            query.addCondition(BinaryCondition.lessThan(
                    new FunctionCall(
                            new Function() {
                                @Override
                                public String getFunctionNameSQL() {
                                    return "RAND";
                                }

                            }), getSubsetFraction(approxTotalNumVariants), true));
            //String qstr = query.toString();
            //LOG.info(qstr);

            //Copy the results of the restricted join to the new subset table called 'tableNameSubset'.
            //TableSchema subsetTable = InfobrightUtils.addVariantTableToDatabase(update, ..., true); //TODO
            subsetTable = createNewSubsetTable(conn, getSubsetFraction(approxTotalNumVariants));
            InfobrightUtils.copyQueryResultToNewTable(serverContext.getTemporaryFile(null), conn, query, subsetTable.getTableName());
        } catch (IOException ie) {
            reverseRefreshSubset(conn, tmpTable, subsetTable);
        } catch (SQLException sqe) {
            reverseRefreshSubset(conn, tmpTable, subsetTable);
        } finally {
            if (tmpTable != null) { //clean up the temporary table.
                try {
                    InfobrightUtils.dropTable(conn, tmpTable.getTableName());
                } catch (SQLException e) {
                    LOG.error("Unable to drop temporary table " + tmpTable.getTableName()+" after subset refresh, but subset refresh was otherwise successful", e);                   
                }
            }
        }
    }

}
