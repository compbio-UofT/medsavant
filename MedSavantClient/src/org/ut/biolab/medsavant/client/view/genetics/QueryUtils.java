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
import org.ut.biolab.mfiume.query.medsavant.MedSavantConditionViewGenerator;
import org.ut.biolab.mfiume.query.value.encode.NumericConditionEncoder;
import org.ut.biolab.mfiume.query.value.encode.StringConditionEncoder;
import org.ut.biolab.mfiume.query.view.SearchConditionItemView;

/**
 *
 * @author mfiume
 */
public class QueryUtils {

    public static void addQueryOnChromPositionAlt(String chrom, int pos, String alt) {
        QueryViewController vc = SearchBar.getInstance().getQueryViewController();
        SearchConditionGroupItem parent = vc.getQueryRootGroup();

        SearchConditionGroupItem g = new SearchConditionGroupItem(parent);
        g.setDescription("Chromosome "+chrom+" Pos. "+pos+" Alt. "+alt);
        SearchConditionItem chromosomeItem = new SearchConditionItem(BasicVariantColumns.CHROM.getAlias(), g);
        String chromosomeConditionEncoded = StringConditionEncoder.encodeConditions(Arrays.asList(new String[]{chrom}));
        chromosomeItem.setSearchConditionEncoding(chromosomeConditionEncoded);
        chromosomeItem.setDescription(StringConditionEncoder.getDescription(StringConditionEncoder.unencodeConditions(chromosomeConditionEncoded)));
        SearchConditionItemView chromosomeView = MedSavantConditionViewGenerator.getInstance().generateViewForItem(chromosomeItem);

        SearchConditionItem positionItem = new SearchConditionItem(BasicVariantColumns.POSITION.getAlias(), g);
        String positionConditionEncoded = NumericConditionEncoder.encodeConditions(pos, pos);
        positionItem.setSearchConditionEncoding(positionConditionEncoded);
        positionItem.setDescription(NumericConditionEncoder.getDescription(NumericConditionEncoder.unencodeConditions(positionConditionEncoded)));
        SearchConditionItemView positionView = MedSavantConditionViewGenerator.getInstance().generateViewForItem(positionItem);

        SearchConditionItem altItem = new SearchConditionItem(BasicVariantColumns.ALT.getAlias(), g);
        String altConditionEncoded = StringConditionEncoder.encodeConditions(Arrays.asList(new String[]{alt}));
        altItem.setSearchConditionEncoding(altConditionEncoded);
        altItem.setDescription(StringConditionEncoder.getDescription(StringConditionEncoder.unencodeConditions(altConditionEncoded)));
        SearchConditionItemView altView = MedSavantConditionViewGenerator.getInstance().generateViewForItem(altItem);

        vc.addItemToGroup(chromosomeItem, chromosomeView, g);
        vc.addItemToGroup(positionItem, positionView, g);
        vc.addItemToGroup(altItem, altView, g);

        vc.addGroupToGroup(g, parent);
    }

    public static void addQueryOnChromPosition(String chrom, int pos) {
        QueryViewController vc = SearchBar.getInstance().getQueryViewController();
        SearchConditionGroupItem parent = vc.getQueryRootGroup();

        SearchConditionGroupItem g = new SearchConditionGroupItem(parent);

        g.setDescription("Chromosome "+chrom+" Pos. "+pos);
        SearchConditionItem chromosomeItem = new SearchConditionItem(BasicVariantColumns.CHROM.getAlias(), g);
        String chromosomeConditionEncoded = StringConditionEncoder.encodeConditions(Arrays.asList(new String[]{chrom}));
        chromosomeItem.setSearchConditionEncoding(chromosomeConditionEncoded);
        chromosomeItem.setDescription(StringConditionEncoder.getDescription(StringConditionEncoder.unencodeConditions(chromosomeConditionEncoded)));
        SearchConditionItemView chromosomeView = MedSavantConditionViewGenerator.getInstance().generateViewForItem(chromosomeItem);

        SearchConditionItem positionItem = new SearchConditionItem(BasicVariantColumns.POSITION.getAlias(), g);
        String positionConditionEncoded = NumericConditionEncoder.encodeConditions(pos, pos);
        positionItem.setSearchConditionEncoding(positionConditionEncoded);
        positionItem.setDescription(NumericConditionEncoder.getDescription(NumericConditionEncoder.unencodeConditions(positionConditionEncoded)));
        SearchConditionItemView positionView = MedSavantConditionViewGenerator.getInstance().generateViewForItem(positionItem);

        vc.addItemToGroup(chromosomeItem, chromosomeView, g);
        vc.addItemToGroup(positionItem, positionView, g);

        vc.addGroupToGroup(g, parent);
    }       
    
    public static void addQueryOnPatients(int[] patientIds){
        QueryViewController qvc = SearchBar.getInstance().getQueryViewController();
        List<String> patientIdStrings = new ArrayList<>(patientIds.length);
        for(int patientId : patientIds){
            patientIdStrings.add(Integer.toString(patientId));
        }                       
        String desc = StringConditionEncoder.getDescription(patientIdStrings);
        qvc.replaceFirstLevelItem(BasicPatientColumns.PATIENT_ID.getAlias(), StringConditionEncoder.encodeConditions(patientIdStrings),desc);
    }
     
    public static void addQueryOnVariantFiles(SimpleVariantFile[] files){
        QueryViewController qvc = SearchBar.getInstance().getQueryViewController();

        List<SearchConditionItem> sciList = new ArrayList<>(files.length);
        for(SimpleVariantFile file : files){
                int uploadId = file.getUploadId();
                int fileId = file.getFileId();
                SearchConditionGroupItem fileGroup = new SearchConditionGroupItem(SearchConditionGroupItem.QueryRelation.OR, null, null);                
                
                String filename = (new File(file.getName())).getName();
                
                fileGroup.setDescription(filename);
                                
                SearchConditionItem uploadItem = new SearchConditionItem(BasicVariantColumns.UPLOAD_ID.getAlias(), SearchConditionGroupItem.QueryRelation.AND, fileGroup);
                uploadItem.setDescription("Upload ID "+uploadId);
                uploadItem.setSearchConditionEncoding(
                        StringConditionEncoder.encodeConditions(
                            Arrays.asList(new String[]{Integer.toString(uploadId)})));

                
                SearchConditionItem fileItem = new SearchConditionItem(BasicVariantColumns.FILE_ID.getAlias(), SearchConditionGroupItem.QueryRelation.AND, fileGroup);
                fileItem.setDescription("File ID "+fileId);
                fileItem.setSearchConditionEncoding(
                        StringConditionEncoder.encodeConditions(
                            Arrays.asList(new String[]{Integer.toString(fileId)})));
                
                qvc.generateItemViewAndAddToGroup(uploadItem, fileGroup);                
                qvc.generateItemViewAndAddToGroup(fileItem, fileGroup);                        
                sciList.add(fileGroup);
        }
        qvc.replaceFirstLevelGroup("Variant Files", sciList, SearchConditionGroupItem.QueryRelation.AND, false);
            
    }
    
    
    public static void addQueryOnRegions(List<GenomicRegion> regions, List<RegionSet> regionSets){
        QueryViewController qvc = SearchBar.getInstance().getQueryViewController();
        List<SearchConditionItem> sciList = new ArrayList<>(regions.size());
        for(GenomicRegion gr : regions){
            SearchConditionGroupItem geneGroup = new SearchConditionGroupItem(SearchConditionGroupItem.QueryRelation.OR, null, null);
            geneGroup.setDescription("Region " + gr.getName());

            SearchConditionItem chromItem = new SearchConditionItem(BasicVariantColumns.CHROM.getAlias(), SearchConditionGroupItem.QueryRelation.AND, geneGroup);
            chromItem.setDescription(gr.getChrom());
            chromItem.setSearchConditionEncoding(StringConditionEncoder.encodeConditions(Arrays.asList(new String[]{gr.getChrom()})));

            SearchConditionItem startPosItem = new SearchConditionItem(BasicVariantColumns.POSITION.getAlias(), SearchConditionGroupItem.QueryRelation.AND, geneGroup);
            startPosItem.setDescription(Integer.toString(gr.getStart())+" - "+Integer.toString(gr.getEnd()));
            startPosItem.setSearchConditionEncoding(NumericConditionEncoder.encodeConditions(gr.getStart(), gr.getEnd()));

            qvc.generateItemViewAndAddToGroup(chromItem, geneGroup);
            qvc.generateItemViewAndAddToGroup(startPosItem, geneGroup);                        
            sciList.add(geneGroup);
        }           
        
        SearchConditionGroupItem scg = null;
        
        //Can we do a query on a number of positions without specifying a region set?  I haven't seen any place
        //in the gui where this happens. 
        if(regionSets == null){
            throw new NullPointerException("QueryUtils.addQueryOnRegions: RegionSets should not be null (not supported)");
        }
        
       // if(sciList.size() > 1 || regionSets != null){
            scg = qvc.replaceFirstLevelGroup("Genomic Region(s)", sciList, SearchConditionGroupItem.QueryRelation.AND, false);
        //}else{                          
          //  qvc.getQueryRootGroup().addItem(sciList.get(0));
        //}
         
        if(regionSets != null){
            List<String> regionSetNames = new ArrayList<>(regionSets.size());
            for(RegionSet rs : regionSets){
                regionSetNames.add(rs.getName());
            }
                
            SearchConditionItem regionItem = new SearchConditionItem("Region Set", SearchConditionGroupItem.QueryRelation.AND, scg);
            regionItem.setDescription("Region Set");
            regionItem.setSearchConditionEncoding(StringConditionEncoder.encodeConditions(regionSetNames));               
            qvc.generateItemViewAndAddToGroup(regionItem, scg);            
        }
                
        qvc.refreshView();    
    }
    
}
