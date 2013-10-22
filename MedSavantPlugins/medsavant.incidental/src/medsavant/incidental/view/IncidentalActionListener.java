package medsavant.incidental.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.ut.biolab.medsavant.client.view.dialog.IndividualSelector;

/**
 *
 * @author rammar
 */
public class IncidentalActionListener implements ActionListener {
    
    private IndividualSelector customSelector;
    
    
    public IncidentalActionListener(IndividualSelector i) {
        customSelector= i;
    }
    
    
    public void actionPerformed(ActionEvent e) {
        customSelector.setVisible(true);
    }
    
}
