package org.ut.biolab.medsavant.shared.query.parser;


import junit.framework.Assert;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.MapSolrParams;
import org.junit.Before;
import org.junit.Test;
import org.ut.biolab.medsavant.shared.query.SimpleSolrQuery;
import org.ut.biolab.medsavant.shared.query.parser.lexer.LexerException;
import org.ut.biolab.medsavant.shared.query.parser.node.Start;
import org.ut.biolab.medsavant.shared.query.parser.parser.ParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JPQLToSolrTranslatorTest {

    JQPToSolrTranslator translator;

    @Before
    public void initialize() {
        translator = new JQPToSolrTranslator();
    }

    @Test
    public void testWhereParameters() {

        SimpleSolrQuery resultedQuery = translator.translate("select v.a from Variant v where v.dna_id = \'NA000001\' and v.id = \'rs1234\'");

        SimpleSolrQuery expectedSolrQuery = new SimpleSolrQuery();
        expectedSolrQuery.addQueryTerm("dna_id", "NA000001");
        expectedSolrQuery.addQueryTerm("id", "rs1234");

        String expectedQuery = expectedSolrQuery.toSolrParamsSimple().toString();

        Assert.assertEquals(expectedQuery, resultedQuery);
    }

    @Test
    public void testNoAlias() throws ParserException, IOException, LexerException {
        SimpleSolrQuery simpleSolrQuery  = translator.translate("select v from Variant v");

        System.out.println(simpleSolrQuery.toString());
    }

}
