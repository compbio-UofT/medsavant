package org.ut.biolab.medsavant.view.genetics.filter.ont;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.medsavant.view.component.Util.DataRetriever;

/**
 *
 * @author mfiume
 */
public class OntologyRetriever implements DataRetriever {

    private final List<Object[]> ontologyTerms;

    public OntologyRetriever(String path) throws FileNotFoundException, IOException {
        ontologyTerms = objectify(loadOntology(path));
    }

    @Override
    public List<Object[]> retrieve(int start, int limit) {
        return ontologyTerms.subList(start, Math.min(start + limit, getTotalNum() - 1));
    }

    //http://bioportal.bioontology.org/ontologies/46602/?p=terms&conceptid=HP%3A0001627
    @Override
    public int getTotalNum() {
        return ontologyTerms.size();
    }

    @Override
    public void retrievalComplete() {
    }

    private List<Object[]> objectify(List<OntologyTerm> terms) {
        List<Object[]> result = new ArrayList<Object[]>(terms.size());
        for (OntologyTerm t : terms) {
            String name = t.getValueForKey("name");
            result.add(new Object[]{t.getID(), name == null ? "" : name});
        }
        return result;
    }

    private static List<OntologyTerm> loadOntology(String path) throws FileNotFoundException, IOException {
        // Create a file object that points to the local copy
        //File file = new File(path);

        InputStream is = OntologyRetriever.class.getResourceAsStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        //BufferedReader br = new BufferedReader(new FileReader(file));

        String line = "";

        List<OntologyTerm> hpoterms = new ArrayList<OntologyTerm>();

        while ((line = br.readLine()) != null) {
            if (line.equals("[Term]")) {
                OntologyTerm t = parseNextTerm(br);
                hpoterms.add(t);
            }
        }

        return hpoterms;
    }

    private static OntologyTerm parseNextTerm(BufferedReader br) throws IOException {

        OntologyTerm t = new OntologyTerm();
        String line = "dummystr";
        while (true) {

            line = br.readLine();
            String[] kvp = line.split(":", 2);

            if (kvp.length != 2) {
                break;
            }

            t.addKVPair(kvp[0], kvp[1].trim());
        }

        return t;
    }

    public List<Object[]> getTerms() {
        return this.ontologyTerms;
    }
}