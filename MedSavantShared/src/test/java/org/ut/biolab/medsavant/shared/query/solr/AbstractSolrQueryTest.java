package org.ut.biolab.medsavant.shared.query.solr;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ut.biolab.medsavant.shared.model.ProjectDetails;
import org.ut.biolab.medsavant.shared.model.VariantComment;
import org.ut.biolab.medsavant.shared.persistence.EntityManager;
import org.ut.biolab.medsavant.shared.persistence.EntityManagerFactory;
import org.ut.biolab.medsavant.shared.persistence.solr.SolrEntityManager;
import org.ut.biolab.medsavant.shared.query.*;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

import java.util.List;

public class AbstractSolrQueryTest {

    private QueryManager queryManager;

    private EntityManager entityManager;

    @Before
    public void initialize() {
        queryManager = QueryManagerFactory.getQueryManager();
        entityManager = EntityManagerFactory.getEntityManager();
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

    @Test
    public void testUpdate() throws QueryException {
        Query updateQuery = queryManager.createQuery("Update Project p set p.reference_id = :referenceId, " +
                "p.reference_name = :referenceName, p.update_id = :updateId, p.annotation_ids = :annotationIds " +
                "where p.project_id = :projectId");
        updateQuery.setParameter("projectId", 1);
        updateQuery.setParameter("referenceId", 3);
        updateQuery.setParameter("referenceName", "hg19");
        updateQuery.setParameter("updateId", 2);
        updateQuery.setParameter("annotationIds", new Integer[] { 1, 2, 3, 4});
        updateQuery.executeUpdate();
    }

    @Test
    public void testAddProject() throws InitializationException {

        //construct a project
        String projectName = "test_project_name";
        int[] annotationIds = new int[] { 1, 2, 3};
        ProjectDetails projectDetails = new ProjectDetails(1, 1, 1, true, projectName, "test_ref_name", annotationIds);
        entityManager.persist(projectDetails);

        //retrieve it
        Query query = queryManager.createQuery("Select p from Project p where p.name = :projectName");
        query.setParameter("projectName", projectName);
        ProjectDetails retrievedProjectDetails = query.getFirst();

        //compare
        Assert.assertEquals("Objects not equal", retrievedProjectDetails, projectDetails);

    }

    public void testRetrieveProject() {

        String projectName = "test_project_name";
        Query query = queryManager.createQuery("Select p from Project p where p.name = :projectName");
        query.setParameter("projectName", projectName);
        ProjectDetails retrievedProjectDetails = query.getFirst();
        Assert.assertEquals(projectName, retrievedProjectDetails.getProjectName());
    }


}