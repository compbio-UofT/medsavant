package org.ut.biolab.medsavant.client.app;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.mfiume.app.api.AppInfoFetcher;
import org.ut.biolab.mfiume.app.AppInfo;

/**
 *
 * @author mfiume
 */
public class MedSavantAppFetcher implements AppInfoFetcher {

    @Override
    public List<AppInfo> fetchApplicationInformation(String search) throws Exception {
        AppInfo mendelAppInfo = new AppInfo(
                "Mendel",
                "1.0.0",
                "Analysis",
                "1.0.1",
                "Scegli su www.trenitalia.com il tuo prossimo viaggio rispettando le regole sopra descritte Una volta selezionata la soluzione di viaggio, inserisci i riferimenti del viaggiatore e nel campo dedicato il codice buono sconto, facendo attenzione a riportare per intero le 19 cifre che lo compongono, zero compresi. Il sistema una volta verificata la validità del codice procederà a sottrarre il valore del buono sconto dall'importo totale. Procedi al pagamento selezionando la modalità che preferisci. Ti ricordiamo che nel caso dovessi modificare in un secondo momento il tuo viaggio, perderai il valore del buono sconto e che il valore del buono non è rimborsabile e non dà diritto ad indennità.",
                "University of Toronto Biolab",
                "http://genomesavant.com",
                new URL("http://www.genomesavant.com/medsavant/plugins/medsavant.geneontology-1.0.0.jar"));

        AppInfo mendelAppInfo2 = new AppInfo(
                "Gene Enrichment Testing",
                "1.0.0",
                "Visualization",
                "1.0.1",
                "Mendel is a great app",
                "University of Toronto Biolab",
                "http://genomesavant.com",
                new URL("http://www.genomesavant.com/medsavant/plugins/medsavant.demo-1.0.0.jar"));

        List<AppInfo> results = new ArrayList<AppInfo>();
        results.add(mendelAppInfo);
        results.add(mendelAppInfo2);
        return results;
    }
}
