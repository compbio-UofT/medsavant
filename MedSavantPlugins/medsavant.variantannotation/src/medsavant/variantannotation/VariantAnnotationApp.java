package medsavant.variantannotation;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.client.api.MedSavantClinicApp;
import org.ut.biolab.medsavant.client.view.component.RoundedPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 * Imports VCFs into database with functional annotations.
 * 
 * @author rammar
 */
public class VariantAnnotationApp extends MedSavantClinicApp {
	
	private int TOP_MARGIN= 0;
	private int SIDE_MARGIN= 100;
	private int BOTTOM_MARGIN= 400;
	
	private static final String iconroot= "/medsavant/variantannotation/icon/";

    private JPanel mainview;
	private RoundedPanel workview;
	private VCFAnnotationWizard vaw;
	private JButton importButton;
	
    @Override
    public JPanel getContent() {
		if (mainview == null) {
			
			/* Display setup */
            mainview= ViewUtil.getClearPanel();;
			mainview.setLayout(new BorderLayout());
			mainview.setBorder(BorderFactory.createEmptyBorder(TOP_MARGIN, SIDE_MARGIN, BOTTOM_MARGIN, SIDE_MARGIN));
		
			workview= new RoundedPanel(10);
			
			importButton= new JButton("Add variants to database...");
			importButton.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						vaw= new VCFAnnotationWizard();
						vaw.setVisible(true);
					}
				}
			);
			
			workview.add(importButton, BorderLayout.CENTER);
			
			mainview.add(workview, BorderLayout.CENTER);
		}
        return mainview;
    }

    /**
     * Title which will appear on plugin's tab in Savant user interface.
     */
    @Override
    public String getTitle() {
        return "Import VCFs";
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
    }

    @Override
    public ImageIcon getIcon() {
        return getIcon(iconroot + "icon.png");
    }

    public ImageIcon getIcon(String resourcePath) {
        return new ImageIcon(getClass().getResource(resourcePath));
    }
}
