package org.ut.biolab.medsavant.shared.query.solr;

import org.junit.Before;
import org.junit.Test;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryException;
import org.ut.biolab.medsavant.shared.query.QueryManager;
import org.ut.biolab.medsavant.shared.query.ResultRow;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

import java.util.List;

public class AbstractSolrQueryTest {

    private QueryManager queryManager;

    @Before
    public void initialize() {
        queryManager = new SolrQueryManager();
    }

    @Test
    public void testAbstractSolrQueryForRows() throws QueryException {
        Query query = queryManager.createQuery("select v.id,v.chrom,v.pos from Variant v where v.dna_id = :dna_id");

        query.setParameter("dna_id","NA19785");

        List<ResultRow> solrDocumentList = query.executeForRows();

        System.out.println(solrDocumentList);
    }

    @Test
    public void testAbstractSolrQueryFields() throws QueryException {
        Query query = queryManager.createQuery("select v.id,v.chrom,v.pos from Variant v where v.dna_id = :dna_id");

        query.setParameter("dna_id","NA19785");

        List<VariantRecord> solrDocumentList = query.execute();

        System.out.println(solrDocumentList);
    }

    @Test
    public void testAbstractSolrQuery() throws QueryException {
        Query query = queryManager.createQuery("select v from Variant v where v.dna_id = :dna_id");

        query.setParameter("dna_id","NA19785");

        List<VariantRecord> solrDocumentList = query.execute();

        System.out.println(solrDocumentList);
    }

    @Test
    public void testAbstractComplexConditioanSolrQuery() throws QueryException {
        Query query = queryManager.createQuery(" select v from Variant v where (v.dna_id = :dna_id AND v.id = :id) OR v.chrom = :chrom");

        query.setParameter("dna_id","NA00001");
        query.setParameter("id","rs123456");
        query.setParameter("chrom","19");

        List<VariantRecord> variantRecordList = query.execute();

        System.out.println(variantRecordList);
    }

    @Test
    public void testAggregateTerms() {
        Query query = queryManager.createQuery("select v.dna_id, count(v), count(v.dna_id) from Variant v group by dna_id,chrom");

        List<ResultRow> variantRecordList = query.executeForRows();

        System.out.println(variantRecordList);
    }

    @Test
    public void testNoWhereParameters() throws QueryException {
        Query query = queryManager.createQuery("select v from Variant v");

        List<VariantRecord> variantRecordList = query.execute();

        System.out.print(variantRecordList);
    }



}
