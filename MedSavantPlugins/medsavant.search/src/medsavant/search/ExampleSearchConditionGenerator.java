package medsavant.search;

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
class ExampleSearchConditionGenerator implements ComprehensiveConditionGenerator {

    public ExampleSearchConditionGenerator() {
    }

    @Override
    public String getName() {
        return "Favorite Chromosome";
    }

    @Override
    public String category() {
        return MedSavantConditionViewGenerator.VARIANT_CONDITIONS;
    }

    @Override
    public Condition getConditionsFromEncoding(String string) throws Exception {
        DbColumn col = ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(BasicVariantColumns.CHROM.getColumnName());
        BinaryCondition bc = BinaryCondition.equalTo(col, string);
        return bc;
    }

    @Override
    public SearchConditionEditorView getViewGeneratorForItem(SearchConditionItem i) {
        ExampleSearchConditionEditorView editor = new ExampleSearchConditionEditorView(i);
        return editor;
    }
}
