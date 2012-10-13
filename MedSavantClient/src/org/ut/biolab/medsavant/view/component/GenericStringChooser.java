package org.ut.biolab.medsavant.view.component;

import com.jidesoft.list.FilterableCheckBoxList;
import com.jidesoft.list.QuickListFilterField;
import com.jidesoft.swing.SearchableUtils;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Position;
import org.ut.biolab.medsavant.filter.TabularFilterView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class GenericStringChooser extends JDialog {

    private QuickListFilterField field;
    protected FilterableCheckBoxList filterableList;

    public GenericStringChooser(ValueRetriever r, String title) {
        super();
        this.setTitle(title);
        initUI();
        this.setLocationRelativeTo(null);
    }

    private void initUI() {
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());

        JPanel innerPanel = new JPanel();
        innerPanel.setBorder(ViewUtil.getMediumBorder());
        innerPanel.setLayout(new BorderLayout());
        contentPane.add(innerPanel, BorderLayout.CENTER);

        //JPanel topPanel = new JPanel();
        //this.add(topPanel,BorderLayout.NORTH);

        SelectableListView<String> strContainer = new SelectableListView<String>();

        List<String> vals = new ArrayList<String>();
        vals.add("a");
        vals.add("b");
        strContainer.appliedValues = vals;
        strContainer.initContentPanel();

        innerPanel.add(strContainer, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        innerPanel.add(bottomPanel, BorderLayout.SOUTH);

        ViewUtil.applyHorizontalBoxLayout(bottomPanel);
        JLabel numSelectedLabel = new JLabel("Number selected");

        bottomPanel.add(numSelectedLabel);
        bottomPanel.add(Box.createHorizontalGlue());

    }


    public static abstract class ValueRetriever<T> {

        public abstract List<T> retrieveStringValues();
    }
}
