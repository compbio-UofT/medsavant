package medsavant.pgx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;

/**
 * Thrown to indicate that an error has occurred in the PGx app.
 * @author rammar
 */
public class PGXException extends Exception {
	
	private static Log log= LogFactory.getLog(MedSavantClient.class);
	
	public PGXException(String message) {
		super(message);
		
		printErrorToLog();
	}
	
	/**
	 * Prints the error message to the MedSavant log.
	 */
	private void printErrorToLog() {
		log.error("[" + this.getClass().getSimpleName() + "]: " + this.getMessage());
	}
	
}
