package org.ut.biolab.mfiume.query.medsavant.complex;

import com.healthmarketscience.sqlbuilder.Condition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.geneset.GeneSetController;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.ontology.OntologyFilter;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.model.OntologyTerm;
import org.ut.biolab.medsavant.shared.model.OntologyType;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.medsavant.MedSavantConditionViewGenerator;
import org.ut.biolab.mfiume.query.value.StringConditionValueGenerator;
import org.ut.biolab.mfiume.query.value.encode.StringConditionEncoder;
import org.ut.biolab.mfiume.query.view.SearchConditionItemView;
import org.ut.biolab.mfiume.query.view.StringSearchConditionEditorView;

/**
 *
 * @author mfiume
 */
public class OntologyConditionGenerator implements ComprehensiveConditionGenerator {

    private static final Log LOG = LogFactory.getLog(OntologyConditionGenerator.class);
    private boolean alreadyInitialized;
    private HashMap<String, OntologyTerm> termNameToTermObjectMap;
    private List<String> acceptableValues;
    private final OntologyType ontology;

    public OntologyConditionGenerator(OntologyType ont) {
        this.ontology = ont;
    }

    @Override
    public String getName() {
        return ontology.name();
    }

    @Override
    public String category() {
        return MedSavantConditionViewGenerator.REGIONBASED_CONDITIONS;
    }

    @Override
    public Condition getConditionsFromEncoding(String encoding) throws Exception {

        init();

        List<String> termNames = StringConditionEncoder.unencodeConditions(encoding);
        List<OntologyTerm> appliedTerms = new ArrayList<OntologyTerm>(termNames.size());
        for (String termName : termNames) {
            appliedTerms.add(termNameToTermObjectMap.get(termName));
        }

        Set<Gene> genes = new HashSet<Gene>();
        Map<OntologyTerm, String[]> allTermsGenes = MedSavantClient.OntologyManager.getGenesForTerms(LoginController.getInstance().getSessionID(), appliedTerms.toArray(new OntologyTerm[0]), ReferenceController.getInstance().getCurrentReferenceName());
        for (String[] termGenes : allTermsGenes.values()) {
            for (String geneName : termGenes) {
                Gene g = GeneSetController.getInstance().getGene(geneName);
                if (g != null) {
                    genes.add(g);
                } else {
                    LOG.info("Non-existent gene " + geneName + " referenced by " + ontology);
                }
            }
        }
        List<GenomicRegion> regions = new ArrayList<GenomicRegion>(genes.size());
        int i = 0;
        for (Gene g : genes) {
            regions.add(new GenomicRegion(g.getName(), g.getChrom(), g.getStart(), g.getEnd()));
        }
        return ConditionUtils.getConditionsMatchingGenomicRegions(regions);
    }

    @Override
    public SearchConditionItemView generateViewFromItem(SearchConditionItem item) {
        StringSearchConditionEditorView editor = new StringSearchConditionEditorView(item, new StringConditionValueGenerator() {
            private HashMap<String, OntologyTerm> termNameToTermObjectMap;

            @Override
            public List<String> getStringValues() {
                init();
                return acceptableValues;

            }
        });
        SearchConditionItemView view = new SearchConditionItemView(item, editor);
        return view;

    }

    private void init() {
        if (alreadyInitialized) {
            return;
        }

        List<String> vals = new ArrayList<String>();

        try {

            OntologyTerm[] terms = MedSavantClient.OntologyManager.getAllTerms(LoginController.getInstance().getSessionID(), ontology);
            vals = new ArrayList<String>(terms.length);
            termNameToTermObjectMap = new HashMap<String, OntologyTerm>();
            for (OntologyTerm t : terms) {
                String termName = t.getOntology() + ":" + t.getName();
                termNameToTermObjectMap.put(termName, t);
                vals.add(termName);
            }

        } catch (Exception ex) {
            LOG.error(ex);
        }
        acceptableValues = vals;

        alreadyInitialized = true;
    }
}
