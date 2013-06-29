package org.ut.biolab.medsavant.shared.query;

/**
 *  Provide exception handling for query exceptions
 */
public class QueryException extends Exception {


    private Query query;

    public QueryException(String message, Query query, Throwable throwable) {
        super(message, throwable);

        this.query = query;
    }

    public String getMesage() {
        return String.format("%s\n%s", super.getMessage(), query.toString());
    }


}
