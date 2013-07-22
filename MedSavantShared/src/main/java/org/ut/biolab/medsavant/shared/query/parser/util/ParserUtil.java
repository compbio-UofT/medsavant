package org.ut.biolab.medsavant.shared.query.parser.util;

/**
 * Misc util functionality for parsing JPQL
 */
public class ParserUtil {

    public static boolean greaterThan(String token) {
        token = token.trim();
        return (">".equals(token) || ">=".equals(token)) ? true : false;
    }

    public static boolean lessThan(String token) {
        token = token.trim();
        return ("<".equals(token) || "<=".equals(token)) ? true : false;
    }

    public static boolean equal(String token) {
        token = token.trim();
        return ("=".equals(token)) ? true : false;
    }
}
