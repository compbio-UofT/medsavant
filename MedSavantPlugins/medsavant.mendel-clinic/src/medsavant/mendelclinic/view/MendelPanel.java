package medsavant.mendelclinic.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.view.subview.SectionView;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;
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
            view.setBackground(Color.white);

            JPanel p = ViewUtil.getClearPanel();
            p.setBorder(ViewUtil.getBigBorder());
            p.setLayout(new BorderLayout());

            fo = new OptionView();
            p.add(ViewUtil.centerHorizontally(fo.getView()),BorderLayout.NORTH);

            view.add(p, BorderLayout.CENTER);
        }

        return view;
    }

    public void refresh() {
        fo.viewDidLoad();
    }
}
