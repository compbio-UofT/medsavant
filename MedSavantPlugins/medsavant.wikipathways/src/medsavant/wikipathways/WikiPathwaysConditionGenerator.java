package medsavant.wikipathways;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.util.Arrays;
import java.util.List;
import javax.swing.JLabel;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.medsavant.MedSavantConditionViewGenerator;
import org.ut.biolab.mfiume.query.medsavant.complex.ComprehensiveConditionGenerator;
import org.ut.biolab.mfiume.query.value.StringConditionValueGenerator;
import org.ut.biolab.mfiume.query.view.SearchConditionEditorView;
import org.ut.biolab.mfiume.query.view.SearchConditionEditorView.ConditionRestorationException;
import org.ut.biolab.mfiume.query.view.SearchConditionItemView;
import org.ut.biolab.mfiume.query.view.StringSearchConditionEditorView;

/**
 *
 * @author mfiume
 */
public class WikiPathwaysConditionGenerator implements ComprehensiveConditionGenerator {

    private static WikiPathwaysConditionGenerator instance;

    public static WikiPathwaysConditionGenerator getInstance() {
        if (instance == null) {
            instance = new WikiPathwaysConditionGenerator();
        }
        return instance;
    }

    private WikiPathwaysSearchConditionEditor editor;
    private SearchConditionItem item;

    private WikiPathwaysConditionGenerator() {
    }

    @Override
    public String getName() {
        return "Wiki Pathways";

    }

    @Override
    public String category() {
        return MedSavantConditionViewGenerator.VARIANT_CONDITIONS;
    }

    @Override
    public Condition getConditionsFromEncoding(String string) throws Exception {
        return editor.getCondition();
    }

    @Override
    public SearchConditionEditorView getViewGeneratorForItem(SearchConditionItem i) {
        editor = new WikiPathwaysSearchConditionEditor(i);
        this.item = i;
        return editor;
    }

    public void setPathway(String pathway, String explanation) {
        editor.setPathway(pathway, explanation);
    }
}
