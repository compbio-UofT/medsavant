package org.ut.biolab.medsavant.shared.query.parser;


import org.ut.biolab.medsavant.shared.query.parser.lexer.Lexer;
import org.ut.biolab.medsavant.shared.query.parser.lexer.LexerException;
import org.ut.biolab.medsavant.shared.query.parser.node.Start;
import org.ut.biolab.medsavant.shared.query.parser.parser.Parser;
import org.ut.biolab.medsavant.shared.query.parser.parser.ParserException;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

/**
 * Manage parsing of JPQL queries
 */
public class JPQLParser {

    public Start parse(String jqplStatement) throws ParserException, IOException, LexerException {

        Parser p = new Parser(new Lexer(new PushbackReader(new StringReader(jqplStatement))));
        Start start = p.parse();
        return start;
    }

}
