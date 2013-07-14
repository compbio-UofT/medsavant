package org.ut.biolab.medsavant.shared.query.parser;


import junit.framework.Assert;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CommonParams;
import org.junit.Before;
import org.junit.Test;
import org.ut.biolab.medsavant.shared.query.SimpleSolrQuery;
import org.ut.biolab.medsavant.shared.query.parser.lexer.LexerException;
import org.ut.biolab.medsavant.shared.query.parser.parser.ParserException;

import java.io.IOException;

public class JPQLToSolrTranslatorTest {

    JQPLToSolrTranslator translator;

    @Before
    public void initialize() {
        translator = new JQPLToSolrTranslator();
    }

    @Test
    public void testSingleConditions() {
        SolrQuery resultedQuery = translator.translate("select v.a from Variant v where v.dna_id='NA000001'");

        Assert.assertEquals(resultedQuery.get(CommonParams.Q).trim(),"dna_id:'NA000001'");
    }

    @Test
    public void testSimpleOr() {
        SolrQuery resultedQuery = translator.translate("select v.a from Variant v where v.dna_id='NA000001' OR v.id ='rs1234'");

        Assert.assertEquals(resultedQuery.get(CommonParams.Q).trim(),"dna_id:'NA000001' OR id:'rs1234'");
    }

    @Test
    public void testWhereParameters() {

        SolrQuery resultedQuery = translator.translate("select v.a from Variant v where v.dna_id='NA000001' AND v.id ='rs1234'");

        Assert.assertEquals(resultedQuery.get(CommonParams.Q).trim(),"dna_id:'NA000001' AND id:'rs1234'");
    }

    @Test                                                         	 Issue #33: Add JQPL to Solr query translator
    public void testComplexConditions() {

        SolrQuery resultedQuery = translator.translate("select v.a, v.b from Variant v where (v.dna_id='NA000001' AND v.id='rs1234') OR v.chrom=19 ");

        Assert.assertEquals(resultedQuery.get(CommonParams.Q).trim(),"(dna_id:'NA000001' AND id:'rs1234') OR chrom:19");
        Assert.assertEquals(resultedQuery.get(CommonParams.FL).trim(), "a,b");
    }

    @Test
    public void testSortParameters() {

        SolrQuery resultedQuery = translator.translate("select v.a, v.b from Variant v where v.dna_id='NA000001' AND v.id='rs1234' order by v.a, v.b desc");

        Assert.assertEquals(resultedQuery.get(CommonParams.Q).trim(),"dna_id:'NA000001' AND id:'rs1234'");
        Assert.assertEquals(resultedQuery.get(CommonParams.FL).trim(),"a,b");
        Assert.assertEquals(resultedQuery.get(CommonParams.SORT).trim(), "a asc,b desc");
    }

}
