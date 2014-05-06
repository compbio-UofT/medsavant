/* Source code from http://viralpatel.net/blogs/java-load-csv-file-to-database/. 
 * Modified by rammar. */
package CSVLoader;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import au.com.bytecode.opencsv.CSVReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Types;
 
/**
 * Parse CSV file using OpenCSV library and load in given database table. 
 * 
 * NOTE: I think this class treats all values as text. Does not guarantee that
 * SQL types will be maintained. So far, haven't encountered any errors.
 * 
 * @author viralpatel.net, rammar
 */
public class CSVLoader {
 
    private static final
        String SQL_INSERT = "INSERT INTO ${table}(${keys}) VALUES(${values})";
    private static final String TABLE_REGEX = "\\$\\{table\\}";
    private static final String KEYS_REGEX = "\\$\\{keys\\}";
    private static final String VALUES_REGEX = "\\$\\{values\\}";
 
    private Connection connection;
    private char seprator;
 
    /**
     * Public constructor to build CSVLoader object with
     * Connection details. The connection is closed on success
     * or failure.
     * @param connection
     */
    public CSVLoader(Connection connection) {
        this.connection = connection;
        //Set default separator
        this.seprator = ',';
    }
     
    /**
     * Parse CSV file using OpenCSV library and load in 
     * given database table. 
	 * 
	 * Modified by rammar.
	 * 
     * @param csvFile Input CSV InputStream
     * @param tableName Database table name to import data
     * @param truncateBeforeLoad Truncate the table before inserting 
     *          new records.
     * @throws Exception
     */
    public void loadCSV(InputStream csvFile, String tableName,
            boolean truncateBeforeLoad) throws Exception {
 
        CSVReader csvReader = null;
        if(null == this.connection) {
            throw new Exception("Not a valid connection.");
        }
        try {
             
			/* Modified by rammar.
			 * 
			 * I was having issues with the CSVReader using the "\" to escape characters.
			 * A MySQL CSV file contains quote-enclosed fields and non-quote-enclosed NULL
			 * values written as "\N". The CSVReader was removing the "\". To detect "\N"
			 * I must remove the escape character, and the only character you can replace
			 * it with that you are pretty much guaranteed will not be used to escape
			 * text is '\0'.
			 * I read this on:
			 * http://stackoverflow.com/questions/6008395/opencsv-in-java-ignores-backslash-in-a-field-value
			 * based on:
			 * http://sourceforge.net/p/opencsv/support-requests/5/
			 */
			// PREVIOUS VERSION: csvReader = new CSVReader(new FileReader(csvFile), this.seprator);
            csvReader = new CSVReader(new InputStreamReader(csvFile), this.seprator, '"', '\0');
 
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error occured while executing file. "
                    + e.getMessage());
        }
 
        String[] headerRow = csvReader.readNext();
 
        if (null == headerRow) {
            throw new FileNotFoundException(
                    "No columns defined in given CSV file." +
                    "Please check the CSV file format.");
        }
 
        String questionmarks = StringUtils.repeat("?,", headerRow.length);
        questionmarks = (String) questionmarks.subSequence(0, questionmarks
                .length() - 1);
 
		
		/* NOTE from Ron: Header column names must match SQL table fields */
        String query = SQL_INSERT.replaceFirst(TABLE_REGEX, tableName);
        query = query
                .replaceFirst(KEYS_REGEX, StringUtils.join(headerRow, ","));
        query = query.replaceFirst(VALUES_REGEX, questionmarks);
 
		//System.out.println("Query: " + query); // Modified by rammar to suppress output
 
        String[] nextLine;
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = this.connection;
            con.setAutoCommit(false);
            ps = con.prepareStatement(query);
 
            if(truncateBeforeLoad) {
                //delete data from table before loading csv
                con.createStatement().execute("DELETE FROM " + tableName);
            }
 
            final int batchSize = 1000;
            int count = 0;
            Date date = null;
            while ((nextLine = csvReader.readNext()) != null) {
				
                if (null != nextLine) {
                    int index = 1;
                    for (String string : nextLine) {
                        date = DateUtil.convertToDate(string);
                        if (null != date) {
                            ps.setDate(index++, new java.sql.Date(date
                                    .getTime()));
                        } else {
							
							/* Section modified by rammar to allow NULL values 
							 * to be input into the DB. */
							if (string.length() > 0 && !string.equals("\\N")) {								
								ps.setString(index++, string);
							} else {
								ps.setNull(index++, Types.VARCHAR);
								//ps.setString(index++, null); // can use this syntax also - not sure which is better
							}
						}
                    }
                    ps.addBatch();
                }
                if (++count % batchSize == 0) {
					ps.executeBatch(); 
                }
            }
            ps.executeBatch(); // insert remaining records
			System.out.println("[" + this.getClass().getSimpleName() + "]: " + 
				count + " records loaded into " + tableName + " DB table");
            con.commit();
        } catch (Exception e) {
            con.rollback();
            e.printStackTrace();
            throw new Exception(
                    "Error occured while loading data from file to database."
                            + e.getMessage());
        } finally {
            if (null != ps)
                ps.close();
            if (null != con)
                con.close();
 
            csvReader.close();
        }
    }
 
    public char getSeprator() {
        return seprator;
    }
 
    public void setSeprator(char seprator) {
        this.seprator = seprator;
    }
 
}