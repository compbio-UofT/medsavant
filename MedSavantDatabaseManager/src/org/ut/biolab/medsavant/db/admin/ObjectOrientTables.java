package org.ut.biolab.medsavant.db.admin;

import java.io.IOException;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.DBSettings;
import org.ut.biolab.medsavant.db.util.DBUtil;

/**
 *
 * @author mfiume
 */
public class ObjectOrientTables {

    public static String outputClassName = "MedSavantDatabase";

    public static void main(String argv[]) throws SQLException, IOException, Exception {
        ooTables("localhost",5029,"agp",TableSchema.class);
    }

    private static void ooTables(String dbhost, int port, String dbname,Class tableSchemaClass) throws SQLException, IOException, Exception {

        File outfile = new File(outputClassName + ".java");

        System.out.println("Writing result to " + outfile.getAbsoluteFile());
        System.out.println("Copy to  result to " + tableSchemaClass.getPackage().getName() + " when complete");


        if (outfile.exists()) {
            outfile.delete();
        }

        if (outfile.exists()) {
            throw new Exception("Output file exists and can't be removed");
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));

        ConnectionController.setPort(port);
        ConnectionController.setHost(dbhost);
        ConnectionController.setDBName(dbname);
        ConnectionController.setCredentials("root", "");

        Connection conn = ConnectionController.connectPooled();

        ResultSet rs = conn.createStatement().executeQuery("SHOW TABLES in " + dbname);

        bw.write("package " + tableSchemaClass.getPackage().getName() + ";\n\n");
        bw.write("import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;\n");
        bw.write("import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;\n");
        bw.write("import " + tableSchemaClass.getPackage().getName() + "." + tableSchemaClass.getSimpleName() + ";"+ "\n");
        bw.write("\n");
        bw.write("public class " + outputClassName + " {"+ "\n\n");

        List<String> classNames = new ArrayList<String>();

        while (rs.next()) {
            DbTable t = DBUtil.importTable(rs.getString(1));

            String tableName = t.getTableNameSQL();

            if (tableName.startsWith("z")) { continue; }

            String tableNameSentenceCase = (tableName.charAt(0) + "").toUpperCase() + tableName.substring(1);
            String className = tableNameSentenceCase.replace("_", "") + tableSchemaClass.getSimpleName();
            classNames.add(className);

            bw.write("");
            bw.write("\tpublic static class " + className + " extends " + tableSchemaClass.getSimpleName() + " {"+ "\n");

            bw.write("\t\tpublic static final String TABLE_NAME = \"" + t.getTableNameSQL() + "\";"+ "\n");
            bw.write("");
            bw.write("\t\tpublic " + className + "(DbSchema s) {\n"
                    + "\t\t\tsuper(s.addTable(TABLE_NAME));\n"
                    + "\t\t\taddColumns();\n"
                    + "\t\t}"+ "\n");
            bw.write(""+ "\n");
            if(className.toLowerCase().contains("default")){
                bw.write("\t\tpublic " + className + "(DbSchema s, String tablename) {\n"
                        + "\t\t\tsuper(s.addTable(tablename));\n"
                        + "\t\t\taddColumns();\n"
                        + "\t\t}"+ "\n");
                bw.write(""+ "\n");
            }

            int index = 0;

            for (DbColumn c : t.getColumns()) {
                String cnamelower = c.getColumnNameSQL();
                String cnameupper = c.getColumnNameSQL().toUpperCase();
                bw.write("\t\t// " + c.getAbsoluteName()+ "\n");
                bw.write("\t\tpublic static final int " + indexname(cnameupper) + " = " + (index++)+ ";"+ "\n");
                bw.write("\t\tpublic static final ColumnType " + typename(cnameupper) + " = " + tableSchemaClass.getSimpleName() + ".ColumnType." + TableSchema.convertStringToColumnType(c.getTypeNameSQL()) + ";"+ "\n");
                bw.write("\t\tpublic static final int " + lengthname(cnameupper) + " = " + c.getTypeLength()+ ";"+ "\n");
                bw.write("\t\tpublic static final String " + colname(cnameupper) + " = \"" + cnamelower + "\";"+ "\n");
                bw.write("");
            }

            bw.write("\t\tprivate void addColumns() {"+ "\n");

            for (DbColumn c : t.getColumns()) {


                String cname = c.getColumnNameSQL().toUpperCase();
                //bw.write("\t\t\t// " + cname);
                bw.write("\t\t\taddColumn(" + colname(cname) + "," + colname(cname) + "," + tableSchemaClass.getSimpleName() + ".ColumnType." + TableSchema.convertStringToColumnType(c.getTypeNameSQL()) + "," + c.getTypeLength() + ");"+ "\n");
                //bw.write("");
            }

            bw.write("\t\t}"+ "\n");
            bw.write(""+ "\n");
            bw.write("\t}"+ "\n");
            bw.write("\n");
        }

        bw.write("\tpublic static final DbSchema schema = (new DbSpec()).addDefaultSchema();\n\n");

        for (String className : classNames) {
            bw.write("\t//" + className + "\n");
            bw.write("\tpublic static final " + className + " " + className + " = new " + className + "(schema);\n\n");
        }


        bw.write("}"+ "\n");

        bw.flush();
        bw.close();
    }

    private static String colname(String cname) {
        return "COLUMNNAME_OF_" + cname;
    }

    private static String indexname(String cname) {
        return "INDEX_OF_" + cname;
    }

    private static String typename(String cname) {
        return "TYPE_OF_" + cname;
    }

    private static String lengthname(String cname) {
        return "LENGTH_OF_" + cname;
    }
}
