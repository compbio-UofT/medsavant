package org.ut.biolab.medsavant.view.genetics.family;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class FamilyMattersPage extends SubSectionView {

    private JPanel view;
    private FamilyMattersOptionView fo;

    public static final String PAGE_NAME = "Cohort Analysis";

    public FamilyMattersPage(SectionView parent) {
        super(parent, PAGE_NAME);
    }

    @Override
    public JPanel getView() {

        if (view == null) {
            view = new JPanel();
            view.setLayout(new BorderLayout());
            view.setBackground(Color.white);

            JPanel titlePanel = new JPanel();
            ViewUtil.applyHorizontalBoxLayout(titlePanel);

            JLabel title = new JLabel("Cohort Analysis");
            title.setOpaque(true);
            title.setBorder(ViewUtil.getBigBorder());
            title.setOpaque(false);
            title.setFont(ViewUtil.getMediumTitleFont());
            titlePanel.add(title);

            view.add(titlePanel,BorderLayout.NORTH);

            JPanel p = ViewUtil.getClearPanel();
            p.setBorder(ViewUtil.getBigBorder());
            p.setLayout(new BorderLayout());

            fo = new FamilyMattersOptionView();
            p.add(ViewUtil.centerHorizontally(fo.getView()),BorderLayout.NORTH);

            view.add(p, BorderLayout.CENTER);
        }

        return view;
    }

    @Override
    public void viewDidLoad() {
        super.viewDidLoad();
        fo.viewDidLoad();
    }
}
