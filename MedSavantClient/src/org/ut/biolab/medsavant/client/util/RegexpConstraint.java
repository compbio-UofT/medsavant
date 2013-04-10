/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.util;

import java.util.regex.Pattern;

/**
 * A constraint that can be specified with a regular expression.
 * 
 * @author jim
 */
public class RegexpConstraint extends CustomFieldConstraint{
    
    //A few regular expressions that may be useful.
    public static final String REGEXP_URL_WEB = "(?i)^((https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]\\s*)+$";
    public static final String REGEXP_URL = "(?i)^((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]\\s*)+$";
    
    public static final String REGEXP_EMAIL = "^(?i)([_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"+
        "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,}))$";
        
    public static final String REGEXP_DEFAULT_VARCHAR = "(?s)^.*$"; //allow everything.    
    public static final String REGEXP_DEFAULT_INTEGER = "^[0-9]+$";
    public static final String REGEXP_DEFAULT_FLOAT = "(\\+|-)?([0-9]*(\\.[0-9]+))";
    public static final String REGEXP_DEFAULT_DECIMAL = REGEXP_DEFAULT_FLOAT;
    public static final String REGEXP_DEFAULT_DATE = "^((19|20)\\\\d\\\\d)-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])$";
    public static final String REGEXP_DEFAULT_TEXT = REGEXP_DEFAULT_VARCHAR;
    
    private Pattern re;

    /**
     * 
     * @param regexp The regular expression that must be satisfied for this constraint.  If missing values are to be forbidden, it should
     * be indicated with the 'required' flag rather than encoded only in the regular expression.
     * @param required Whether or not a missing value is acceptable.
     * @param maxlength The maximum number of characters in the input
     * @param errorMessage An error message that can be stored for later retrieval with getErrorMessage()
     */
    public RegexpConstraint(String regexp, boolean required, int maxlength, String errorMessage){          
        super(required, maxlength, errorMessage);
        this.re = Pattern.compile(regexp);                
    }
    
    /**
     * 
     * @param regexp The regular expression that must be satisfied for this constraint.  If missing values are to be forbidden, it should
     * be indicated with the 'required' flag rather than encoded only in the regular expression.
     * @param required Whether or not a missing value is acceptable.
     * @param maxlength The maximum number of characters in the input
     */
    public RegexpConstraint(String regexp, int maxlength, String errorMessage){
        this(regexp, false, maxlength, errorMessage);
    }
    
    /**
     * Constructs a new regexp constraint where missing values are permitted.
     * 
     * @param regexp The regular expression that must be satisfied for this constraint.  If missing values are to be forbidden, it should
     * be indicated with the 'required' flag rather than encoded only in the regular expression.
     * @param maxlength The maximum number of characters in the input
     */
    public RegexpConstraint(String regexp, int maxlength){
        this(regexp, maxlength, null);
    }
    
    /**
     * Validates the given input
     * 
     * @param val The input string to validate.  Null, zero-length, or white-space only strings are considered
     * missing.
     * @return true if valid, false otherwise.
     */
    @Override
    public boolean isValid(String val){                
        return super.isValid(val) && re.matcher(val).matches();        
    }
}
