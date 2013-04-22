package org.ut.biolab.medsavant.client.view.genetics;

import java.util.Arrays;
import org.ut.biolab.medsavant.client.filter.SearchBar;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
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
}
