/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
