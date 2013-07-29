package org.ut.biolab.medsavant.shared.query.parser;


import junit.framework.Assert;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CommonParams;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class JPQLToSolrTranslatorTest {

    JPQLToSolrTranslator translator;

    @Before
    public void initialize() {
        translator = new JPQLToSolrTranslator();
    }

    @Test
    public void testSingleConditions() {
        SolrQuery resultedQuery = translator.translate("select v.a from Variant v where v.dna_id='NA000001'");

        Assert.assertEquals(resultedQuery.get(CommonParams.Q).trim(),"dna_id:'NA000001'");
    }

    @Test
    public void testSimpleOr() {
        SolrQuery resultedQuery = translator.translate("select v.a from Variant v where v.dna_id='NA000001' OR v.id ='rs1234'");

        Assert.assertEquals(resultedQuery.get(CommonParams.Q).trim(),"dna_id:'NA000001'  OR id:'rs1234'");
    }

    @Test
    public void testWhereParameters() {

        SolrQuery resultedQuery = translator.translate("select v.a from Variant v where v.dna_id='NA000001'  AND v.id ='rs1234'");

        Assert.assertEquals(resultedQuery.get(CommonParams.Q).trim(),"dna_id:'NA000001'  AND id:'rs1234'");
    }

    @Test
    public void testComplexConditions() {

        SolrQuery resultedQuery = translator.translate("select v.a, v.b from Variant v where (v.dna_id='NA000001' AND v.id='rs1234') OR v.chrom=19 ");

        Assert.assertEquals(resultedQuery.get(CommonParams.Q).trim(),"(dna_id:'NA000001'  AND id:'rs1234' ) OR chrom:19");
        Assert.assertEquals(resultedQuery.get(CommonParams.FL).trim(), "a,b");
    }

    @Test
    public void testSortParameters() {

        SolrQuery resultedQuery = translator.translate("select v.a, v.b from Variant v where v.dna_id='NA000001' AND v.id='rs1234' order by v.a, v.b desc");

        Assert.assertEquals(resultedQuery.get(CommonParams.Q).trim(),"dna_id:'NA000001'  AND id:'rs1234'");
        Assert.assertEquals(resultedQuery.get(CommonParams.FL).trim(),"a,b");
        Assert.assertEquals(resultedQuery.get(CommonParams.SORT).trim(), "a asc,b desc");
    }

    @Test
    public void testNamedParameters() {
        Map<String, Object> params = translator.getNamedParameter("select v.a, v.b from Variant v where (v.dna_id = :dna_id AND v.id = :id) OR v.chrom = :chrom ");

        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("dna_id", "");
        expected.put("id", "");
        expected.put("chrom", "");
        Assert.assertEquals(params, expected);
    }

    @Test
    public void testBetween() {
        SolrQuery resultedQuery = translator.translate("select v.a, v.b from Variant v where (pos between 10 and 10000) OR dna_id='NA*'");

        Assert.assertEquals(resultedQuery.get(CommonParams.Q).trim(), "(pos:[10 TO 10000]) OR dna_id:'NA*'");
    }

    @Test
    public void testGroupBy() {
        SolrQuery resultedQuery = translator.translate("select v.dna_id, count(v) from Variant v where v.dna_id = 'NA00002' group by v.dna_id");

        Assert.assertEquals(resultedQuery.get(CommonParams.Q).trim(), "(pos:[10 TO 10000]) OR dna_id:'NA*'");
    }

    @Test
    public void testDeleteSimple() {
        SolrQuery resultedQuery = translator.translate("delete from Variant v");

        Assert.assertEquals(resultedQuery.get(CommonParams.Q).trim(), "(pos:[10 TO 10000]) OR dna_id:'NA*'");
    }

    @Test
    public void testDeleteParameters() {
        SolrQuery resultedQuery = translator.translate("delete from Variant v where v.dna_id = 'NA00002'");

        Assert.assertEquals(resultedQuery.get(CommonParams.Q).trim(), "dna_id:NA'");
    }

}
