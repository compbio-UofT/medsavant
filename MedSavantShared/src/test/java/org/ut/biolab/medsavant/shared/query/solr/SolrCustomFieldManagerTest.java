package org.ut.biolab.medsavant.shared.query.solr;

import org.junit.Test;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.persistence.CustomFieldManager;
import org.ut.biolab.medsavant.shared.persistence.solr.SolrCustomFieldManager;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Test how new fields are added to the schema.
 */
public class SolrCustomFieldManagerTest {

    private CustomFieldManager manager = new SolrCustomFieldManager();

    @Test
    public void addNewField() throws IOException, URISyntaxException {
        CustomField newField = new CustomField("test_name", "integer", true, null,null);
        manager.addCustomField(newField);
    }
}
