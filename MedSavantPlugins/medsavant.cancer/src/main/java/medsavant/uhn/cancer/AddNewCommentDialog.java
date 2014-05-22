/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package medsavant.uhn.cancer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.model.GeneSet;
import org.ut.biolab.medsavant.shared.model.UserComment;
import org.ut.biolab.medsavant.shared.model.UserCommentGroup;
import org.ut.biolab.medsavant.shared.model.OntologyTerm;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;
import savant.api.util.DialogUtils;

public class AddNewCommentDialog extends JDialog {

    private static final Log LOG = LogFactory.getLog(AddNewCommentDialog.class);
    private static final int DEFAULT_COMMENTBOX_WIDTH = 100; //in characters
    private static final int DEFAULT_COMMENTBOX_HEIGHT = 25;
    private JTextArea commentBox;
    private OntologyTerm selectedOntologyTerm; //only REQUIRED when replying/setting status.  In that case, it's immutable.           
    private final VariantRecord variantRecord;
    private SelectableOntologyTerm[] selectableOntologyTerms;

    private static final int MINIMUM_WIDTH = 640;
    private static final int MINIMUM_HEIGHT = 480;
    private final int mainPanelWidth;
    private final int mainPanelHeight;    
    
    private void submitComment(UserCommentGroup lcg, OntologyTerm term) throws SessionExpiredException, RemoteException, SQLException{
            UserComment lc = new UserComment(commentBox.getText(), term);
            MedSavantClient.VariantManager.replyToUserCommentGroup(LoginController.getSessionID(), lcg.getUserCommentGroupId(), lc);
    }
    
    private void submitComment(UserCommentGroup lcg) throws SessionExpiredException, RemoteException, SQLException {
        if(selectedOntologyTerm != null){
            submitComment(lcg, selectedOntologyTerm);
        }else{
            int n = 0;
            for(SelectableOntologyTerm sot: selectableOntologyTerms){
                if(sot.isSelected()){
                    submitComment(lcg, sot.getOntologyTerm());
                    n++;
                }
            }            
        }
    }

    /**
     * For posting new comments. Presents dialog with an ontology term selector.
     */
    public AddNewCommentDialog(JFrame parentFrame, VariantRecord vr) {
        this(parentFrame, vr, null);
    }

    /**
     * For posting new comments. Presents dialog with an ontology term selector,
     * with the selectedOntologyTerm selected by default.
     */
    public AddNewCommentDialog(JFrame parentFrame, VariantRecord vr, OntologyTerm selectedOntologyTerm) { //post new  message for
        super(parentFrame);
        this.selectedOntologyTerm = selectedOntologyTerm;
        this.variantRecord = vr;                
        Dimension pd = parentFrame.getSize();
        mainPanelWidth = Math.max(MINIMUM_WIDTH, pd.width / 2);
        mainPanelHeight = Math.max(MINIMUM_HEIGHT, pd.height / 2);
        JPanel mainPanel = getMainPanel();
        mainPanel.setPreferredSize(new Dimension(mainPanelWidth, mainPanelHeight));        
        this.add(mainPanel);
        this.pack();
        this.setModal(true);

    }

    private JPanel getHeader(String str) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(str));
        p.add(Box.createHorizontalGlue());
        return p;
    }
    
    private class SelectableOntologyTerm extends JPanel{
        private OntologyTerm ontologyTerm;
        private JCheckBox checkbox;
        
        public SelectableOntologyTerm(OntologyTerm ontologyTerm){
            this.ontologyTerm = ontologyTerm;
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            checkbox = new JCheckBox(ontologyTerm.getID()+" - "+ontologyTerm.getName(), true);
            add(checkbox);
            add(Box.createHorizontalGlue());
        }
        
        public boolean isSelected(){
            return checkbox.isSelected();
        }
        
        public void clear(){
            checkbox.setSelected(false);            
        }
        
        public OntologyTerm getOntologyTerm(){
            return ontologyTerm;
        }
    }    
       
    //should be threaded?
    private JPanel getOntologyTermsForThisVariant(){
        try{
            JPanel ontologyTermPanel = new JPanel();
            ontologyTermPanel.setLayout(new BoxLayout(ontologyTermPanel, BoxLayout.Y_AXIS));
            
            String sessID = LoginController.getSessionID();
            String refName = ReferenceController.getInstance().getCurrentReferenceName();
            if(refName == null){
                DialogUtils.displayError("Error", "Couldn't obtain name of current reference genome - please login again.");
                dispose();
                return null;
            }
            GeneSet geneSet = MedSavantClient.GeneSetManager.getGeneSet(sessID, refName);
            if(geneSet == null){
                DialogUtils.displayError("Error", "Couldn't find a gene set for reference "+refName+". Please check project settings.");
                dispose();
                return null;
            }
            
            Gene[] genesOverlappingVariant
                    = MedSavantClient.GeneSetManager.getGenesInRegion(sessID, geneSet, variantRecord.getChrom(), variantRecord.getStartPosition().intValue(), variantRecord.getEndPosition().intValue());
            if(genesOverlappingVariant == null || genesOverlappingVariant.length < 1){                
                //TODO: Here we could allow the user to choose from a list of ALL hpo terms, not just those
                //that are already associated with the variant.
                DialogUtils.displayError("No genes were found associated with this variant "+variantRecord+" in geneSet "+geneSet);
                dispose();
                return null;
            }
            Set<OntologyTerm> ontologyTermSet = new TreeSet<OntologyTerm>();
            for (Gene gene : genesOverlappingVariant) {
                OntologyTerm[] ontologyTerms = MedSavantClient.OntologyManager.getTermsForGene(sessID, UserCommentApp.getDefaultOntologyType(), gene.getName());
                if (ontologyTerms != null && ontologyTerms.length > 0) {
                    ontologyTermSet.addAll(Arrays.asList(ontologyTerms));
                }
            }
            
            selectableOntologyTerms = new SelectableOntologyTerm[ontologyTermSet.size()];
            int i = 0;
            for(OntologyTerm ontologyTerm : ontologyTermSet){
                selectableOntologyTerms[i] = new SelectableOntologyTerm(ontologyTerm);
                ontologyTermPanel.add(selectableOntologyTerms[i]);
                ++i;
            }
            JScrollPane jsp = new JScrollPane(ontologyTermPanel);
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setPreferredSize(new Dimension(mainPanelWidth, mainPanelHeight/2));
            p.add(jsp);
            return p;
        }catch(Exception ex){
            ex.printStackTrace();
            LOG.error("Error: ", ex);
            DialogUtils.displayException("Error", ex.getMessage(), ex);
            dispose();
            return null;
        }            
    }
    
    private void clearSelections(){
        if(selectableOntologyTerms != null){            
            for(SelectableOntologyTerm sot : selectableOntologyTerms){
                sot.clear();
            }            
        }
    }

    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        if(selectedOntologyTerm == null){
            mainPanel.add(getHeader(UserCommentApp.getDefaultOntologyType().name()+" terms associated with this variant"));
            JButton selectNoneButton = new JButton("Clear Selections");
            selectNoneButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    clearSelections();
                }                
            });
            mainPanel.add(selectNoneButton);
            mainPanel.add(getOntologyTermsForThisVariant());
            mainPanel.add(getHeader("Comment"));
        }else{
            mainPanel.add(getHeader("Comment for "+UserCommentApp.getDefaultOntologyType().name()+" term "+selectedOntologyTerm.getName()));
        }
        
        commentBox = new JTextArea("", DEFAULT_COMMENTBOX_WIDTH, DEFAULT_COMMENTBOX_HEIGHT);
        commentBox.setLineWrap(true);
        JPanel textBoxPanel = new JPanel();
        textBoxPanel.setLayout(new BoxLayout(textBoxPanel, BoxLayout.X_AXIS));
        textBoxPanel.add(Box.createHorizontalGlue());
        textBoxPanel.add(new JScrollPane(commentBox));
        textBoxPanel.add(Box.createHorizontalGlue());

        mainPanel.add(textBoxPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        JButton OKButton = new JButton("OK");
        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String sessID = LoginController.getSessionID();
                    int projId = ProjectController.getInstance().getCurrentProjectID();
                    int refId = ReferenceController.getInstance().getCurrentReferenceID();
                    UserCommentGroup lcg = MedSavantClient.VariantManager.getUserCommentGroup(sessID, projId, refId, variantRecord);
                    if (lcg == null) {
                        lcg = MedSavantClient.VariantManager.createUserCommentGroup(sessID, projId, refId, variantRecord);
                    }
                    submitComment(lcg);
                    dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    LOG.error("Error: ", ex);
                    DialogUtils.displayException("Error", ex.getLocalizedMessage(), ex);
                }
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        buttonPanel.add(OKButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalGlue());
        mainPanel.add(buttonPanel);
        return mainPanel;
    }

}
