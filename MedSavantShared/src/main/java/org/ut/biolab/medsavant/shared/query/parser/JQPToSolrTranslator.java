package org.ut.biolab.medsavant.shared.query.parser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.shared.query.SimpleSolrQuery;
import org.ut.biolab.medsavant.shared.query.parser.lexer.LexerException;
import org.ut.biolab.medsavant.shared.query.parser.node.Start;
import org.ut.biolab.medsavant.shared.query.parser.parser.ParserException;

import java.io.IOException;

/**
 * Translate JPQL queries.
 */
public class JQPToSolrTranslator {

    private QueryAnalyzer analyzer;

    private static final Log LOG = LogFactory.getLog(JQPToSolrTranslator.class);

    public JQPToSolrTranslator() {
        this.analyzer = new QueryAnalyzer(new QueryContext());
    }

    public JQPToSolrTranslator(QueryAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public SimpleSolrQuery translate(String input) {

        JPQLParser parser = new JPQLParser();
        SimpleSolrQuery solrQuery = null;
        try {
            Start tree = parser.parse(input);
            tree.apply(analyzer);

            solrQuery = analyzer.getSimpleSolrQuery();

        } catch (ParserException e) {
            LOG.error("Unable to parse string " + input, e);
        } catch (IOException e) {
            LOG.error("Unable to parse string " + input, e);
        } catch (LexerException e) {
            LOG.error("Unable to parse string " + input, e);
        }

        return solrQuery;
    }

    public QueryAnalyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(QueryAnalyzer analyzer) {
        this.analyzer = analyzer;
    }
}
