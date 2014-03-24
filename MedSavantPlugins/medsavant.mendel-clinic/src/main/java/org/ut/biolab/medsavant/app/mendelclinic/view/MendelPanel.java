package org.ut.biolab.medsavant.app.mendelclinic.view;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.view.util.StandardAppContainer;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class MendelPanel {
    public static String PAGE_NAME = "Mendel";

    private JPanel view;
    private OptionView fo;

    public MendelPanel() {
    }

    public JPanel getView() {

        if (view == null) {
            view = new JPanel();
            view.setLayout(new BorderLayout());
            view.setBackground(ViewUtil.getLightGrayBackgroundColor());
            fo = new OptionView();
            JPanel p = new StandardAppContainer(fo.getView(), true);
            p.setBackground(ViewUtil.getLightGrayBackgroundColor());
            view.add(p, BorderLayout.CENTER);
        }

        return view;
    }

    public void refresh() {
        fo.viewDidLoad();
    }
}
