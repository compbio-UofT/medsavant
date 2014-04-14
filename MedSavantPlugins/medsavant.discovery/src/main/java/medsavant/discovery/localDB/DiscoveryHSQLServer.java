package medsavant.discovery.localDB;

import org.hsqldb.HsqlException;
import org.hsqldb.server.Server;


/** 
 * Starts and stops the HyperSQL (HSQL) server in memory to be used by the
 * MedSavant Incidentalome app.
 * 
 * @author rammar
 */
public class DiscoveryHSQLServer {
	
	private Server hsqlServer;
	
	private String DB_USER= "hsqldbUser";
	private String DB_PASSWORD= "lsk519dj122fl307sakf"; // randomly chosen...
	private String db_path_prefix= "mem:medsavantincidental";
	private String DB_PATH= db_path_prefix + ";ifexists=false" + ";user=" +	DB_USER + ";password=" + DB_PASSWORD;
	private String DB_URL= "jdbc:hsqldb:" + db_path_prefix;
	
	private int DB_INDEX= 0; // leave == 0, not sure why but registers server type "mem:" when 0 only.
	private String DB_NAME= "incidental";
	private int HSQLDB_PORT= 9001;
	
	
	/**
	 * Get a HyperSQL server instance with default username and password.
	 */
	public DiscoveryHSQLServer() {
	}
	
	
	/**
	 * Get a HyperSQL server instance with user-specified username and password.
	 * @param	user	Specified username
	 * @param	passw	Specified password
	 */
	public DiscoveryHSQLServer(String user, String passw) {
		DB_USER= user;
		DB_PASSWORD= passw;
		DB_PATH= db_path_prefix + ";user=" + DB_USER + ";password=" + DB_PASSWORD;
		DB_URL= "jdbc:hsqldb:" + db_path_prefix;
		
		hsqlServer= new Server();
		hsqlServer.signalCloseAllServerConnections(); // Not sure if this is needed, but keeping for now.
		
		hsqlServer.setDatabaseName(DB_INDEX, DB_NAME);
		hsqlServer.setDatabasePath(DB_INDEX, DB_PATH);
		
		// Suppress DB-based output
		hsqlServer.setLogWriter(null);
		hsqlServer.setErrWriter(null);
	}
	
	
	/** 
	 * Start the HyperSQL server.
	 * Needs further error checking.
	 * @param	port	Desired port to run HyperSQL server.
	 */
	public void startServer(int port) {
		hsqlServer.setPort(port);
		hsqlServer.start();
	}
	
	
	/**
	 * Start the HyperSQL server on the default port.
	 */
	public void startServer() {
		startServer(HSQLDB_PORT);
	}
	
	
	/** 
	 * Stops the server.
	 */
	public void stopServer() {
		hsqlServer.stop();
	}
	
	
	/**
	 * Returns the server URL.
	 */
	public String getURL() {
		return DB_URL;
	}
	
	
	/**
	 * Check the status of the server.
	 */
	public boolean isRunning() {
		boolean result= true;
		
		try {
			hsqlServer.checkRunning(true);
		} catch (HsqlException e) {
			result= false;
		}
		
		return result;
	}
	
}
