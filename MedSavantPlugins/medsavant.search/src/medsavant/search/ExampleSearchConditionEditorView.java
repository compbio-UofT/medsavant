package medsavant.search;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.view.SearchConditionEditorView;

/**
 *
 * @author mfiume
 */
public class ExampleSearchConditionEditorView extends SearchConditionEditorView {

    private JTextField inputField;

    public ExampleSearchConditionEditorView(SearchConditionItem i) {
        super(i);
        init();
    }

    @Override
    public void loadViewFromSearchConditionParameters(String encoding) throws SearchConditionEditorView.ConditionRestorationException {
        if (encoding != null) {
            inputField.setText(encoding.replace("chr", ""));
        }
    }

    private void init() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(new JLabel("chr"));

        inputField = new JTextField();
        inputField.setColumns(5);
        this.add(inputField);

        inputField.addCaretListener(new CaretListener() {

            @Override
            public void caretUpdate(CaretEvent e) {
                encodeAndSaveConditionSettings();
            }
        });
    }

    private void encodeAndSaveConditionSettings() {
        String input = inputField.getText();
        String encoding = "chr" + input;
        this.item.setSearchConditionEncoding(encoding);
        this.item.setDescription(encoding);
    }
}
