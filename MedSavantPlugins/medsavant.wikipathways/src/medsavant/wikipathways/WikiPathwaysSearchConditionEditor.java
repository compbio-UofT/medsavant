package medsavant.wikipathways;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import medsavant.wikipathways.app.PathwaysTab;
import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.view.SearchConditionEditorView;

/**
 *
 * @author mfiume
 */
public class WikiPathwaysSearchConditionEditor extends SearchConditionEditorView {

    private final PathwaysTab pathwaysTab;

    public WikiPathwaysSearchConditionEditor(SearchConditionItem i) {
        super(i);

        pathwaysTab = new PathwaysTab();
        init();
    }

    @Override
    public void loadViewFromSearchConditionParameters(String encoding) throws SearchConditionEditorView.ConditionRestorationException {
        if (encoding != null) {
            pathwaysTab.getById(encoding);
        }
    }

    private void init() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        JButton chooseButton = new JButton("Choose Pathway");
        chooseButton.setFocusable(false);
        chooseButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                pathwaysTab.setVisible(true);
            }
        });

        this.add(chooseButton);
    }

    public Condition getCondition() {
        return pathwaysTab.getCondition();
    }

    void setPathway(String pathway) {
        this.item.setSearchConditionEncoding(pathway);
        this.item.setDescription(pathway);
    }
}
