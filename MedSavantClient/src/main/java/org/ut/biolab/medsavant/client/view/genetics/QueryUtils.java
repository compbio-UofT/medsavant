package org.ut.biolab.medsavant.client.view.genetics;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.ut.biolab.medsavant.client.filter.SearchBar;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.model.RegionSet;
import org.ut.biolab.medsavant.shared.model.SimpleVariantFile;
import org.ut.biolab.mfiume.query.QueryViewController;
import org.ut.biolab.mfiume.query.SearchConditionGroupItem;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.value.encode.NumericConditionEncoder;
import org.ut.biolab.mfiume.query.value.encode.StringConditionEncoder;

/**
 *
 * @author mfiume
 */
public class QueryUtils {
    
    public static SearchConditionGroupItem getRegionGroup(GenomicRegion gr, String alt, boolean setupViews) {
        QueryViewController qvc = SearchBar.getInstance().getQueryViewController();
        SearchConditionGroupItem geneGroup = new SearchConditionGroupItem(SearchConditionGroupItem.QueryRelation.OR, null, null);
        String name = gr.getName();
        if (name == null) {
            geneGroup.setDescription("Region " + gr.getStart());
        } else {
            geneGroup.setDescription("Region " + name);
        }


        SearchConditionItem chromItem = new SearchConditionItem(BasicVariantColumns.CHROM.getAlias(), SearchConditionGroupItem.QueryRelation.AND, geneGroup);
        chromItem.setDescription(gr.getChrom());
        chromItem.setSearchConditionEncoding(StringConditionEncoder.encodeConditions(Arrays.asList(new String[]{gr.getChrom()})));

        SearchConditionItem startPosItem = new SearchConditionItem(BasicVariantColumns.POSITION.getAlias(), SearchConditionGroupItem.QueryRelation.AND, geneGroup);
        startPosItem.setDescription(Long.toString(gr.getStart()) + " - " + Long.toString(gr.getEnd()));
        startPosItem.setSearchConditionEncoding(NumericConditionEncoder.encodeConditions(gr.getStart(), gr.getEnd()));


        if(setupViews){
            qvc.generateItemViewAndAddToGroup(chromItem, geneGroup);
            qvc.generateItemViewAndAddToGroup(startPosItem, geneGroup);
        }else{
            geneGroup.addItem(chromItem);
            geneGroup.addItem(startPosItem);
        }
        if (alt != null) {
            SearchConditionItem altItem = new SearchConditionItem(BasicVariantColumns.ALT.getAlias(), SearchConditionGroupItem.QueryRelation.AND, geneGroup);
            altItem.setDescription(alt);
            altItem.setSearchConditionEncoding(StringConditionEncoder.encodeConditions(Arrays.asList(alt)));
            if(setupViews){
                qvc.generateItemViewAndAddToGroup(altItem, geneGroup);
            }else{
                geneGroup.addItem(altItem);
            }
        }
        return geneGroup;
    }

    /**
     * Adds a new query to the searchbar, similar to addQueryOnRegions, but with
     * an additional 'alt' constraint, and no region set constraint.
     *
     * @param region
     * @param alt
     */
    public static void addQueryOnRegionWithAlt(GenomicRegion region, String alt) {
        QueryViewController qvc = SearchBar.getInstance().getQueryViewController();
        List<SearchConditionItem> sciList = new ArrayList<SearchConditionItem>(1);
        sciList.add(getRegionGroup(region, alt, true));
        SearchConditionGroupItem scg = qvc.replaceFirstLevelGroup("Genomic Region(s)", sciList, SearchConditionGroupItem.QueryRelation.AND, false);
        qvc.refreshView();
    }

    /**
     * Adds a new query to the SearchBar, consisting of a group called "Genomic
     * Region(s)", under which are several groups - one per region, and
     * optionally a "Region Set" group. Each region group consists of a search
     * query on the chromosome and position, and region groups are OR'd
     * together. The Region Set search item appears last, and is AND'd with the
     * region groups.
     *
     * @param regions
     * @param regionSets
     */
    public static void addQueryOnRegions(List<GenomicRegion> regions, List<RegionSet> regionSets) {
        QueryViewController qvc = SearchBar.getInstance().getQueryViewController();
        List<SearchConditionItem> sciList = new ArrayList<SearchConditionItem>(regions.size());
        for (GenomicRegion gr : regions) {
            SearchConditionGroupItem geneGroup = getRegionGroup(gr, null, true);
            sciList.add(geneGroup);
        }

        SearchConditionGroupItem scg = qvc.replaceFirstLevelGroup("Genomic Region(s)", sciList, SearchConditionGroupItem.QueryRelation.AND, false);


        if (regionSets != null) {
            List<String> regionSetNames = new ArrayList<String>(regionSets.size());
            for (RegionSet rs : regionSets) {
                regionSetNames.add(rs.getName());
            }

            SearchConditionItem regionItem = new SearchConditionItem("Region Set", SearchConditionGroupItem.QueryRelation.AND, scg);
            regionItem.setDescription(StringConditionEncoder.getDescription(regionSetNames));
            regionItem.setSearchConditionEncoding(StringConditionEncoder.encodeConditions(regionSetNames));
            qvc.generateItemViewAndAddToGroup(regionItem, scg);
        }

        qvc.refreshView();

    }

    public static void addMultiStringQuery(String alias, List<String> selections) {
        QueryViewController qvc = SearchBar.getInstance().getQueryViewController();
        String desc = StringConditionEncoder.getDescription(selections);
        qvc.replaceFirstLevelItem(alias, StringConditionEncoder.encodeConditions(selections), desc);
    }

    public static void addQueryOnPatients(int[] patientIds) {
        List<String> patientIdStrings = new ArrayList<String>(patientIds.length);
        for (int patientId : patientIds) {
            patientIdStrings.add(Integer.toString(patientId));
        }
        addMultiStringQuery(BasicPatientColumns.PATIENT_ID.getAlias(), patientIdStrings);
    }

    public static void addQueryOnVariantFiles(SimpleVariantFile[] files) {
        QueryViewController qvc = SearchBar.getInstance().getQueryViewController();

        List<SearchConditionItem> sciList = new ArrayList<SearchConditionItem>(files.length);
        for (SimpleVariantFile file : files) {
            int uploadId = file.getUploadId();
            int fileId = file.getFileId();
            SearchConditionGroupItem fileGroup = new SearchConditionGroupItem(SearchConditionGroupItem.QueryRelation.OR, null, null);

            String filename = (new File(file.getName())).getName();

            fileGroup.setDescription(filename);

            SearchConditionItem uploadItem = new SearchConditionItem(BasicVariantColumns.UPLOAD_ID.getAlias(), SearchConditionGroupItem.QueryRelation.AND, fileGroup);
            uploadItem.setDescription("Upload ID " + uploadId);
            uploadItem.setSearchConditionEncoding(
                    StringConditionEncoder.encodeConditions(
                    Arrays.asList(new String[]{Integer.toString(uploadId)})));


            SearchConditionItem fileItem = new SearchConditionItem(BasicVariantColumns.FILE_ID.getAlias(), SearchConditionGroupItem.QueryRelation.AND, fileGroup);
            fileItem.setDescription("File ID " + fileId);
            fileItem.setSearchConditionEncoding(
                    StringConditionEncoder.encodeConditions(
                    Arrays.asList(new String[]{Integer.toString(fileId)})));

            qvc.generateItemViewAndAddToGroup(uploadItem, fileGroup);
            qvc.generateItemViewAndAddToGroup(fileItem, fileGroup);
            sciList.add(fileGroup);
        }
        qvc.replaceFirstLevelGroup("Variant Files", sciList, SearchConditionGroupItem.QueryRelation.AND, false);

    }

    public static void addQueryOnHospitals(String[] hospitalIds, String cohort) {
        QueryViewController qvc = SearchBar.getInstance().getQueryViewController();
        SearchConditionGroupItem p = qvc.getQueryRootGroup();

        List<SearchConditionItem> sciList = new ArrayList<SearchConditionItem>(2);

        SearchConditionItem hosItem = new SearchConditionItem(BasicPatientColumns.HOSPITAL_ID.getAlias(), SearchConditionGroupItem.QueryRelation.AND, p);
        hosItem.setDescription(StringConditionEncoder.getDescription(Arrays.asList(hospitalIds)));
        hosItem.setSearchConditionEncoding(StringConditionEncoder.encodeConditions(Arrays.asList(hospitalIds)));
        sciList.add(hosItem);

        if (cohort != null) {
            SearchConditionItem cohortItem = new SearchConditionItem("Cohort", SearchConditionGroupItem.QueryRelation.AND, p);
            cohortItem.setDescription(StringConditionEncoder.getDescription(Arrays.asList(new String[]{cohort})));
            cohortItem.setSearchConditionEncoding(
                    StringConditionEncoder.encodeConditions(
                    Arrays.asList(new String[]{cohort})));
            sciList.add(cohortItem);
        }

        qvc.replaceFirstLevelGroup("Cohort Member(s)", sciList, SearchConditionGroupItem.QueryRelation.AND, true);
    }

    public static void addQueryOnHospitals(String[] hospitalIds) {
        addQueryOnHospitals(hospitalIds, null);
    }
}
