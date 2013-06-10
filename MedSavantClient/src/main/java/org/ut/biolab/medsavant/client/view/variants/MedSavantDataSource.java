/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.variants;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.filter.FilterEvent;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.login.LoginEvent;
import org.ut.biolab.medsavant.shared.model.RangeCondition;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.reference.ReferenceEvent;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.SelectableListView.SelectionEvent;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import savant.api.adapter.BookmarkAdapter;
import savant.api.adapter.DataSourceAdapter;
import savant.api.adapter.RangeAdapter;
import savant.api.adapter.RecordFilterAdapter;
import savant.api.adapter.VariantDataSourceAdapter;
import savant.api.data.DataFormat;
import savant.api.data.VariantRecord;
import savant.api.util.Resolution;
import savant.view.tracks.Track;
import savant.controller.TrackController;
import savant.exception.RenderingException;
import savant.util.DrawingInstruction;
import savant.util.MiscUtils;

/**
 *
 * @author Andrew
 */
public class MedSavantDataSource implements DataSourceAdapter<VariantRecord>, VariantDataSourceAdapter, Listener<SelectionEvent> {

    /*static void setActive(boolean b) {
     active = b;
     }*/
    //private static boolean active = true;
    private Set<String> chromosomes = new HashSet<String>();
    private String[] participants = new String[0];
    private static final int LIMIT = 100000;
    private List<String> restrictToTheseDNAIDs;

    public MedSavantDataSource() {
        try {
            updateSource();
        } catch (Exception ex) {
            Logger.getLogger(MedSavantDataSource.class.getName()).log(Level.SEVERE, null, ex);
        }

        FilterController.getInstance().addListener(new Listener<FilterEvent>() {
            @Override
            public void handleEvent(FilterEvent event) {
                refresh();
            }
        });

        ReferenceController.getInstance().addListener(new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent event) {
                try {
                    updateSource();
                } catch (Exception ex) {
                    Logger.getLogger(MedSavantDataSource.class.getName()).log(Level.SEVERE, null, ex);
                }
                refresh();
            }
        });
    }

    private void updateSource() throws SQLException, RemoteException, InterruptedException {

        //update chroms
        List<String> chroms;
        try {
            chroms = MedSavantClient.DBUtils.getDistinctValuesForColumn(
                    LoginController.getInstance().getSessionID(),
                    ProjectController.getInstance().getCurrentVariantTableName(),
                    BasicVariantColumns.CHROM.getColumnName(),
                    false);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return;
        }
        chromosomes.clear();
        for (String c : chroms) {
            chromosomes.add(c);
        }

        //update participants
        List<String> dnaIds;
        try {
            dnaIds = MedSavantClient.DBUtils.getDistinctValuesForColumn(
                    LoginController.getInstance().getSessionID(),
                    ProjectController.getInstance().getCurrentVariantTableName(),
                    BasicVariantColumns.DNA_ID.getColumnName(),
                    false);
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return;
        }
        participants = new String[dnaIds.size()];
        for (int i = 0; i < dnaIds.size(); i++) {
            participants[i] = dnaIds.get(i);
        }

    }

    public void refresh() {
        Track t = getTrack();
        if (t != null) {
            t.getFrame().forceRedraw();
        }
    }

    @Override
    public Set<String> getReferenceNames() {
        return chromosomes;
    }
    List<String> dnaIds;

    @Override
    public List<VariantRecord> getRecords(String ref, RangeAdapter range, Resolution resolution, RecordFilterAdapter<VariantRecord> filter) throws IOException, InterruptedException {

        if (dnaIds == null) {
            try {
                dnaIds = MedSavantClient.DBUtils.getDistinctValuesForColumn(
                        LoginController.getInstance().getSessionID(),
                        ProjectController.getInstance().getCurrentVariantTableName(),
                        BasicVariantColumns.DNA_ID.getColumnName(),
                        false);
            } catch (Exception ex) {
            }
        }
        if (this.restrictToTheseDNAIDs == null && (dnaIds != null && dnaIds.size() > 10)) {
            throw new IOException("too many samples, restrict DNA IDs");
        }

        //System.err.println("Getting records " + active);

        if (true) { // used to check if active

            try {

                String savantChrom = MiscUtils.homogenizeSequence(ref);
                String chrom = savantChrom;
                for (String c : chromosomes) {
                    if (MiscUtils.homogenizeSequence(c).equals(savantChrom)) {
                        chrom = c;
                    }
                }

                Condition[][] filterConditions = FilterController.getInstance().getAllFilterConditions();
                TableSchema table = ProjectController.getInstance().getCurrentVariantTableSchema();

                // add filter by DNA ids condition
                if (this.restrictToTheseDNAIDs != null) {
                    Condition[] restrictToDNAIDsCondition = new Condition[restrictToTheseDNAIDs.size()];
                    int i = 0;
                    for (String dnaID : this.restrictToTheseDNAIDs) {
                        restrictToDNAIDsCondition[i++] = BinaryCondition.equalTo(table.getDBColumn(BasicVariantColumns.CHROM.getColumnName()), dnaID);
                    }
                    Condition[][] newFilterConditions = new Condition[filterConditions.length + 1][];
                    for (i = 0; i < filterConditions.length; i++) {
                        newFilterConditions[i] = filterConditions[i];
                    }
                    newFilterConditions[filterConditions.length] = restrictToDNAIDsCondition;
                    filterConditions = newFilterConditions;
                }

                Condition rangeCondition = ComboCondition.and(
                        new Condition[]{
                            BinaryCondition.equalTo(table.getDBColumn(BasicVariantColumns.CHROM.getColumnName()), chrom),
                            new RangeCondition(table.getDBColumn(BasicVariantColumns.POSITION.getColumnName()), range.getFrom(), range.getTo())});
                Condition[][] conditions;
                if (filterConditions.length == 0) {
                    conditions = new Condition[][]{new Condition[]{rangeCondition}};
                } else {
                    conditions = new Condition[filterConditions.length][];
                    for (int i = 0; i < filterConditions.length; i++) {
                        conditions[i] = new Condition[2];
                        conditions[i][0] = rangeCondition;
                        conditions[i][1] = ComboCondition.and(filterConditions[i]);
                    }
                }

                List<Object[]> filteredVariants = MedSavantClient.VariantManager.getVariants(
                        LoginController.getInstance().getSessionID(),
                        ProjectController.getInstance().getCurrentProjectID(),
                        ReferenceController.getInstance().getCurrentReferenceID(),
                        conditions,
                        0,
                        LIMIT);

                Map<String, MergedMedSavantVariantRecord> recordMap = new HashMap<String, MergedMedSavantVariantRecord>();

                if (filteredVariants.size() == LIMIT) {
                    Track t = getTrack();
                    if (t != null) {
                        t.getRenderer().addInstruction(DrawingInstruction.ERROR, new RenderingException("Too many variants to display", RenderingException.INFO_PRIORITY));
                    }
                    return new ArrayList<VariantRecord>();
                }

                for (Object[] arr : filteredVariants) {
                    Integer position = (Integer) arr[BasicVariantColumns.INDEX_OF_POSITION];
                    String refString = (String) arr[BasicVariantColumns.INDEX_OF_REF];
                    String key = position.toString() + refString;
                    MergedMedSavantVariantRecord m = recordMap.get(key);
                    if (m == null) {
                        m = new MergedMedSavantVariantRecord(arr, participants.length);
                        recordMap.put(key, m);
                    }
                    m.addRecord(arr, getIndexOfParticipant((String) arr[BasicVariantColumns.INDEX_OF_DNA_ID]));

                }

                List<VariantRecord> records = new ArrayList<VariantRecord>();
                for (String key : recordMap.keySet()) {
                    records.add(recordMap.get(key));
                }

                return records;

            } catch (Exception ex) {
                ex.printStackTrace();
                throw new IOException(ex.getMessage());
            }
        } else {
            return new ArrayList<VariantRecord>();
        }
    }

    @Override
    public URI getURI() {
        return URI.create("meds://placeholder");
    }

    @Override
    public String getName() {
        return "Filtered Variants";
    }

    @Override
    public void close() {
        MedSavantFrame.getInstance().requestClose();
    }

    @Override
    public DataFormat getDataFormat() {
        return DataFormat.VARIANT;
    }

    @Override
    public String[] getColumnNames() {
        return new String[0]; //TODO
    }

    @Override
    public void loadDictionary() throws IOException {
    }

    @Override
    public List<BookmarkAdapter> lookup(String key) {
        return new ArrayList<BookmarkAdapter>(); //TODO?
    }

    private int getIndexOfParticipant(String dnaId) {
        for (int i = 0; i < participants.length; i++) {
            if (participants[i].equals(dnaId)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String[] getParticipants() {
        return participants;
    }

    private Track getTrack() {
        return TrackController.getInstance().getTrack(getName());
    }

    @Override
    public void handleEvent(SelectionEvent event) {
        List<String> dnaIDs = event.getSelections();
        setRestrictToDNAIDs(dnaIDs);
    }

    private void setRestrictToDNAIDs(List<String> dnaIDs) {
        System.out.println("Restricting genome browser to " + dnaIDs + " dna ids");
        this.restrictToTheseDNAIDs = dnaIDs;
        refresh();
    }
}
