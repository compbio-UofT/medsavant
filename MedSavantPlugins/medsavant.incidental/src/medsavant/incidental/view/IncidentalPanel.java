package medsavant.incidental.view;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import org.ut.biolab.medsavant.client.view.dialog.IndividualSelector;

/**
 * Default panel view for Incidentalome app
 * 
 * @author rammar
 */
public class IncidentalPanel {
    
    public static String PAGE_NAME = "Incidentalome";
    
    private JPanel view;
    private JButton choosePatientButton;
    private IndividualSelector customSelector;
    private IncidentalActionListener patientSelectActionListener;

    public IncidentalPanel() {
    }
        
    public JPanel getView() {
        if (view == null) {
            view= new JPanel();
            view.add(new JLabel("Incidentalome"));
            
            choosePatientButton= new JButton("Choose Patient");
            view.add(choosePatientButton);
            
            customSelector= new IndividualSelector();
            patientSelectActionListener= new IncidentalActionListener(customSelector);
                    
            choosePatientButton.addActionListener(patientSelectActionListener);
        }
        return view;
    }
    
    
    
}
