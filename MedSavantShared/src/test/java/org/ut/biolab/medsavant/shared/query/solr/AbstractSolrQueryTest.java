package org.ut.biolab.medsavant.shared.query.solr;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ut.biolab.medsavant.shared.model.VariantComment;
import org.ut.biolab.medsavant.shared.persistence.EntityManager;
import org.ut.biolab.medsavant.shared.persistence.solr.SolrEntityManager;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryException;
import org.ut.biolab.medsavant.shared.query.QueryManager;
import org.ut.biolab.medsavant.shared.query.ResultRow;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

import java.util.List;

public class AbstractSolrQueryTest {

    private QueryManager queryManager;

    private EntityManager entityManager;

    @Before
    public void initialize() {
        queryManager = new SolrQueryManager();
        entityManager = new SolrEntityManager();
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
        Query query = queryManager.createQuery("select v.dna_id, count(v.dna_id), v.upload_id from Variant v group by dna_id,upload_id");

        List<ResultRow> variantRecordList = query.executeForRows();

        System.out.println(variantRecordList);
    }

    @Test
    public void testOneAggregateTerm() {
        Query query = queryManager.createQuery("select v.dna_id, count(v), count(v.dna_id) from Variant v group by dna_id");

        List<ResultRow> variantRecordList = query.executeForRows();

        System.out.println(variantRecordList);
    }

    @Test
    public void testNoWhereParameters() throws QueryException {
        Query query = queryManager.createQuery("select v from Variant v");

        List<VariantRecord> variantRecordList = query.execute();

        System.out.print(variantRecordList);
    }

    @Test
    public void testMinMaxParameters() throws QueryException {
        Query query = queryManager.createQuery("select v.position, min(v.position), max(v.position) from Variant v");

        List<ResultRow> variantRecordList = query.executeForRows();

        System.out.print(variantRecordList);
    }

    @Test
    public void testDelete() throws InitializationException {

        Query query = queryManager.createQuery("select c from Comment c");
        List<VariantComment> comments = query.execute();

        query = queryManager.createQuery("delete from Comment c");
        query.executeDelete();
        List<VariantComment> comments2 = query.execute();

        entityManager.persistAll(comments);
        Assert.assertEquals(comments2.size(), 0);
    }

    @Test
    public void testComments() throws QueryException {
        String statement = "Select c from Comment c where " +
                "c.project_id = :projectId AND " +
                "c.reference_id = :referenceId AND " +
                "c.upload_id = :uploadId AND " +
                "c.file_id = :fileId AND " +
                "c.variant_id = :variantId";
        Query query = queryManager.createQuery(statement);
        query.setParameter("projectId", 1);
        query.setParameter("referenceId", 3);
        query.setParameter("uploadId", 18);
        query.setParameter("fileId", 1);
        query.setParameter("variantId", 7);

        List<VariantComment> comments =  query.execute();
    }
}