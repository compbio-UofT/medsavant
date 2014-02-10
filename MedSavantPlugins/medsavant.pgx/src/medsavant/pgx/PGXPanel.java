package medsavant.pgx;

import javax.swing.JPanel;

/**
 * Default panel for Pharmacogenomics app.
 * @author rammar
 */
public class PGXPanel extends JPanel {
	
	private JPanel view;
	
	public PGXPanel() {
		view= new JPanel();
	}
	
	public JPanel getView() {
		return view;
	}
	
	
}
