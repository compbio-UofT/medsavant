package org.ut.biolab.medsavant.client.util;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.shared.model.GeneSet;


public class GeneTable {

    private final static Log LOG = LogFactory.getLog(GeneTable.class);
    private GeneSet gs;
    private String[] geneNames;
    private String[] chromosomes;
    private int[] starts;
    private int[] ends;
    private static GeneTable instance;

    private GeneTable(Object[][] data) {
        this.geneNames = new String[data.length];
        this.chromosomes = new String[data.length];
        this.starts = new int[data.length];
        this.ends = new int[data.length];
        for (int i = 0; i < data.length; ++i) {
            this.geneNames[i] = (String) data[i][0];
            this.chromosomes[i] = (String) data[i][1];
            this.starts[i] = (Integer) data[i][2];
            this.ends[i] = (Integer) data[i][3];
        }
        Arrays.sort(this.chromosomes);
        Arrays.sort(this.starts);
        Arrays.sort(this.ends);
        
    }

    public static GeneTable getGeneTable(GeneSet gs) {
        if (instance != null && instance.gs.equals(gs)) {
            return instance;
        }
        final Semaphore sem = new Semaphore(1);
        try {
            sem.acquire();
            GeneFetcher gf = new GeneFetcher(gs, "GeneFetcher") {
                @Override
                public void setData(Object[][] data) {
                    instance = new GeneTable(data);
                    sem.release();
                }
              
                @Override
                public void showProgress(double prog) {
                }
            };


            gf.execute();

            sem.acquire();
        } catch (Exception ex) {
            LOG.error(ex);
        }
        return instance;
    }
}
