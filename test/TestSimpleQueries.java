/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.olddb.QueryUtil;
import org.ut.biolab.medsavant.olddb.DBUtil;
import java.util.Vector;
import java.util.ArrayList;
import org.ut.biolab.medsavant.olddb.table.TableSchema.ColumnType;
import org.ut.biolab.medsavant.olddb.table.TableSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.util.List;
import java.sql.ResultSet;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import java.sql.SQLException;
import org.ut.biolab.medsavant.olddb.MedSavantDatabase;
import org.ut.biolab.medsavant.olddb.ConnectionController;
import java.sql.Connection;
import org.ut.biolab.medsavant.exception.FatalDatabaseException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mfiume
 */
public class TestSimpleQueries {

    public TestSimpleQueries() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}


    @Test
    public void testVariantQueries() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        Connection c = ConnectionController.connect();

        TableSchema t = MedSavantDatabase.getInstance().getVariantTableSchema();
        SelectQuery q1 = new SelectQuery();
        q1.addFromTable(t.getTable());
        q1.addAllColumns();
        String q1String = q1.toString() + " LIMIT 100";

        ResultSet r1 = c.createStatement().executeQuery(q1String);

        List<DbColumn> columns = t.getColumns();
        List<Vector> results = DBUtil.parseResultSet(t.getColumnGrid(), r1);

        for (Vector row : results) {
            for (Object f : row) {
                System.out.print(f + "\t");
            }
            System.out.println();
        }

        //////////////////////////////////////////////////////////////////////

        for (DbColumn dbc : columns) {
            if (TableSchema.isNumeric(t.getColumnType(dbc))) {
                Range extremeValues = QueryUtil.getExtremeValuesForColumn(c, t, dbc);
                System.out.println(dbc.getColumnNameSQL() + ": " + extremeValues.getMin() + " " + extremeValues.getMax());
            }
        }

        if (true) {
            return;
        }

        for (DbColumn dbc : columns) {
            if (!TableSchema.isNumeric(t.getColumnType(dbc))) {
                List<String> distinctvalues = QueryUtil.getDistinctValuesForColumn(c, t, dbc);
                System.out.println(dbc.getColumnNameSQL());
                for (String v : distinctvalues) {
                    System.out.println(v);
                }
            }
        }

    }

    /*
    public static void main(String[] argv) throws SQLException {
        Connection c = ConnectionController.connect();

        int rowsInVariant = getRowsInTable(c,Database.getInstance().getVariantTableSchema().getTable());
        System.out.println("Variants: " + rowsInVariant);

        c.close();
        //int rowsInVariantAnnotations = getRowsInTable(c,DBSettings.TABLE_VARIANT_ANNOTATION);
        //System.out.println("Variant Annotations: " + rowsInVariantAnnotations);

    }
     * 
     */
}