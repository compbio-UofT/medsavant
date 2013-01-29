package org.ut.biolab.medsavant.client.view.component;

import com.jidesoft.list.FilterableCheckBoxList;
import com.jidesoft.list.QuickListFilterField;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.component.SelectableListView.SelectionEvent;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class GenericStringChooser extends JDialog implements Listener<SelectionEvent> {

    private QuickListFilterField field;
    protected FilterableCheckBoxList filterableList;
    private final List<String> options;
    private final ArrayList<Listener<SelectionEvent>> listeners;

    public GenericStringChooser(List<String> options, String title) {
        super();
        this.setTitle(title);
        this.options = options;
        initUI();
        this.setLocationRelativeTo(null);
        listeners = new ArrayList<Listener<SelectionEvent>>();
    }

    private void initUI() {

        Container contentPane = this.getContentPane();
        contentPane.setPreferredSize(new Dimension(350,400));
        contentPane.setLayout(new BorderLayout());

        JPanel innerPanel = new JPanel();
        innerPanel.setBorder(ViewUtil.getMediumBorder());
        innerPanel.setLayout(new BorderLayout());
        contentPane.add(innerPanel, BorderLayout.CENTER);

        SelectableListView<String> strContainer = new SelectableListView<String>();
        strContainer.setAvailableValues(options);
        strContainer.initContentPanel();
        strContainer.addListener(this);

        innerPanel.add(strContainer, BorderLayout.CENTER);

        this.pack();
    }

    public void addListener(Listener<SelectionEvent> l) {
        listeners.add(l);
    }

    @Override
    public void handleEvent(SelectionEvent event) {
        System.out.println("GSC caught change, dispatching to " + listeners.size() + " listeners");
        this.setVisible(false);
        for (Listener l : listeners) {
            l.handleEvent(event);
        }
    }
}
