/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package medsavant.uhn.cancer;

import java.awt.Color;
import java.awt.Dimension;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.shared.appapi.MedSavantVariantInspectorApp;
import org.ut.biolab.medsavant.shared.model.LocusComment;
import org.ut.biolab.medsavant.shared.model.LocusCommentGroup;
import org.ut.biolab.medsavant.shared.model.OntologyTerm;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

/**
 *
 * @author jim
 */
public class LocusCommenterApp extends MedSavantVariantInspectorApp {

    private static final String NAME = "Cancer Workflow - LocusCommenter";
    private static final String TITLE = "Comments for this Locus";
    private static final JSeparator ICON_PLACE_HOLDER = new JSeparator();
    private static final int ICON_WIDTH = 16;
    private static final int ICON_HEIGHT = 16;
    private static final int ICON_SPACING = 8;

    //Icons that reflect the comment's status.  Note only the colored icons are explicitly set.
    //"Not XXX" is the default -- it does not mean 'rejected', but rather 'unset'.
    private static final ImageIcon PENDING_REVIEW_ICON = new ImageIcon(LocusCommenterApp.class.getResource("pending.png"));
    private static final ImageIcon APPROVED_ICON = new ImageIcon(LocusCommenterApp.class.getResource("approved.png"));
    private static final ImageIcon INCLUDED_ICON = new ImageIcon(LocusCommenterApp.class.getResource("include_in_report.png"));
    private static final ImageIcon NOT_PENDING_REVIEW_ICON = new ImageIcon(LocusCommenterApp.class.getResource("gray_pending.png"));
    private static final ImageIcon NOT_APPROVED_ICON = new ImageIcon(LocusCommenterApp.class.getResource("gray_approved.png"));
    private static final ImageIcon NOT_INCLUDED_ICON = new ImageIcon(LocusCommenterApp.class.getResource("gray_include_in_report.png"));

    //Comments will alternate between the given colors in this array.
    private static final Color[] BACKGROUND_COLORS = {new Color(0, 0, 128), new Color(128, 128, 128)};

    //  private static final Color BORDER_COLOR = Color.BLACK;
    //  private static final int BORDER_THICKNESS = 1;
    private JPanel panel;

    static {
        ICON_PLACE_HOLDER.setPreferredSize(new Dimension(ICON_WIDTH, ICON_HEIGHT));
    }

    public LocusCommenterApp() {
        panel = new JPanel();
        JPanel innerPanel = new WaitPanel("Loading...");
        panel.add(innerPanel);

    }

    @Override
    public void setVariantRecord(VariantRecord r) {
        //getLocusCommentGroup(String sessID, int projectId, int refId, String chrom, int start_position, int end_position, String ref, String alt)
        try {
            JPanel innerPanel = new JPanel();
            innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));

            //Get comment group associated with this variant.
            LocusCommentGroup lcg = MedSavantClient.VariantManager.getLocusCommentGroup(
                    LoginController.getSessionID(),
                    ProjectController.getInstance().getCurrentProjectID(),
                    ReferenceController.getInstance().getCurrentReferenceID(),
                    r);

            //Build a mapping from ontology terms to all comments pertaining to that ontology term.
            //Iterating through this map will return the ontology terms in alphabetical order, and the comments
            //within each ontology will be ordered by modification date.
            Map<OntologyTerm, Set<LocusComment>> otCommentMap = new TreeMap<OntologyTerm, Set<LocusComment>>();
            for (Iterator<LocusComment> li = lcg.iterator(); li.hasNext();) {
                LocusComment lc = li.next();
                Set<LocusComment> ontologyComments = otCommentMap.get(lc.getOntologyTerm());
                if (ontologyComments == null) {
                    ontologyComments = new TreeSet<LocusComment>(new Comparator<LocusComment>() {
                        @Override
                        public int compare(LocusComment o1, LocusComment o2) {
                            return o1.getModificationDate().compareTo(o2.getModificationDate());
                        }
                    });
                    ontologyComments.add(lc);
                }
                otCommentMap.put(lc.getOntologyTerm(), ontologyComments);

            }

            //Iterate through the mapping to display the comments.            
            for (Map.Entry<OntologyTerm, Set<LocusComment>> e : otCommentMap.entrySet()) {
                String header = e.getKey().getID() + " - " + e.getKey().getName();
                innerPanel.add(new JLabel(header));

                //JPanel commentPanel = new JPanel();
                //commentPanel.setLayout(new BoxLayout(commentPanel, BoxLayout.Y_AXIS));
                //commentPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS));
                int i = 0;
                for (LocusComment lc : e.getValue()) {
                    if (lc.isDeleted()) {
                        //for now, we don't display deleted comments.
                        continue;
                    }

                    //Setup header panel showing the posting date, user, and icons that reflect this comment's 
                    //status
                    JPanel headerPanel = new JPanel();
                    headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
                    headerPanel.add(new JLabel("Posted " + lc.getModificationDate().toString() + " by " + lc.getUser() + ":"));
                    headerPanel.add(Box.createHorizontalGlue());

                    JPanel iconPanel = new JPanel();
                    iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.X_AXIS));
                    iconPanel.setPreferredSize(new Dimension(ICON_WIDTH * 3 + ICON_SPACING * 2, ICON_HEIGHT));

                    iconPanel.add(lc.isPendingReview() ? new JLabel(PENDING_REVIEW_ICON) : new JLabel(NOT_PENDING_REVIEW_ICON));
                    iconPanel.add(lc.isIncluded() ? new JLabel(INCLUDED_ICON) : new JLabel(NOT_INCLUDED_ICON));
                    iconPanel.add(lc.isApproved() ? new JLabel(APPROVED_ICON) : new JLabel(NOT_APPROVED_ICON));
                    headerPanel.add(iconPanel);
                    innerPanel.add(headerPanel);
                    
                    //Add the actual comment as a JTextArea.
                    JPanel commentPanel = new JPanel();
                    commentPanel.setBackground(BACKGROUND_COLORS[i++ % BACKGROUND_COLORS.length]);
                    JTextArea commentText = new JTextArea();
                    commentText.setText(lc.getCommentText());
                    commentText.setEditable(false);
                    commentText.setLineWrap(true);
                    commentPanel.add(commentText);
                    innerPanel.add(commentPanel);
                }
                innerPanel.add(new JSeparator());
            }
        } catch (SessionExpiredException see) {

        } catch (SQLException sqe) {

        } catch (RemoteException rex) {

        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public JPanel getInfoPanel() {
        return panel;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

}
