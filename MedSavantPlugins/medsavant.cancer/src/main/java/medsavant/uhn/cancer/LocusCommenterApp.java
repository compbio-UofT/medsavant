/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package medsavant.uhn.cancer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.shared.appapi.MedSavantVariantInspectorApp;
import org.ut.biolab.medsavant.shared.model.LocusComment;
import org.ut.biolab.medsavant.shared.model.LocusCommentGroup;
import org.ut.biolab.medsavant.shared.model.OntologyTerm;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.UserLevel;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

/**
 *
 * @author jim
 */
public class LocusCommenterApp extends MedSavantVariantInspectorApp {

    private static final Log LOG = LogFactory.getLog(LocusCommenterApp.class);

    private static final String NAME = "Cancer Workflow - LocusCommenter";
    private static final String TITLE = "Comments for this Locus";
    //private static final JSeparator ICON_PLACE_HOLDER = new JSeparator();
    private static final int ICON_WIDTH = 16;
    private static final int ICON_HEIGHT = 16;
    private static final int ICON_SPACING = 8;

    private static final Color INNER_BORDER_COLOR = Color.BLACK;
    private static final int INNER_BORDER_WIDTH = 1;
    //Icons that reflect the comment's status.  Note only the colored icons are explicitly set.
    //"Not XXX" is the default -- it does not mean 'rejected', but rather 'unset'.
    private static final ImageIcon PENDING_REVIEW_ICON = getScaledImageIcon("/pending.png");
    private static final ImageIcon APPROVED_ICON = getScaledImageIcon("/approved.png");
    private static final ImageIcon INCLUDED_ICON = getScaledImageIcon("/include_in_report.png");

    //private static final ImageIcon REPLY_TO_ONTOLOGY_ICON = getScaledImageIcon("reply.png"); //could be large
    //private static final ImageIcon EDIT_STATUS_BUTTON_ICON = getScaledImageIcon("/include_in_report.png");
    private static final ImageIcon REPLY_TO_ONTOLOGY_ICON = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ACTION_ON_TOOLBAR);
    private static final ImageIcon EDIT_STATUS_BUTTON_ICON = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.EDIT);
    private static final ImageIcon NOT_PENDING_REVIEW_ICON = getScaledImageIcon("/gray_pending.png");
    private static final ImageIcon NOT_APPROVED_ICON = getScaledImageIcon("/gray_approved.png");
    private static final ImageIcon NOT_INCLUDED_ICON = getScaledImageIcon("/gray_include_in_report.png");

    //Comments will alternate between the given colors in this array.
    private static final Color[] BACKGROUND_COLORS = {new Color(0, 0, 128), new Color(128, 128, 128)};

    //  private static final Color BORDER_COLOR = Color.BLACK;
    //  private static final int BORDER_THICKNESS = 1;
    private JPanel panel;

    /*
     static {
     ICON_PLACE_HOLDER.setPreferredSize(new Dimension(ICON_WIDTH, ICON_HEIGHT));        
     }
     */
    private static ImageIcon getScaledImageIcon(String filename) {
        URL resource = LocusCommenterApp.class.getResource(filename);
        if (resource == null) {
            System.err.println("Couldn't load resource given by " + filename);
        }
        ImageIcon ii = new ImageIcon(resource);
        Image im = ii.getImage().getScaledInstance(ICON_WIDTH, ICON_WIDTH, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(im);
    }

    public LocusCommenterApp() {
        System.out.println("Locus Commenter App initialized.");
        panel = new JPanel();
        JPanel innerPanel = new WaitPanel("Loading...");
        panel.add(innerPanel);

    }

    private JPanel getNewHistoryPanel() {
        JPanel historyPanel = new JPanel();
        historyPanel.setBorder(BorderFactory.createLineBorder(INNER_BORDER_COLOR, INNER_BORDER_WIDTH));
        return historyPanel;
    }

    private void replyToOntology(OntologyTerm ot) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void editStatus(LocusComment lc) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private JPanel getMainCommentPanel(Map<OntologyTerm, Set<LocusComment>> otCommentMap) {
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        //Iterate through the mapping to display the comments.            
        for (final Map.Entry<OntologyTerm, Set<LocusComment>> e : otCommentMap.entrySet()) {
            String header = e.getKey().getID() + " - " + e.getKey().getName(); //ontology title.

            //Define horizontal panel with ontology title and reply to ontology icon.
            JPanel ontologyTitlePanel = new JPanel();
            ontologyTitlePanel.setLayout(new BoxLayout(ontologyTitlePanel, BoxLayout.X_AXIS));
            ontologyTitlePanel.add(new JLabel(header));
            ontologyTitlePanel.add(Box.createHorizontalGlue());
            JButton replyToOntologyButton = new JButton(REPLY_TO_ONTOLOGY_ICON);
            replyToOntologyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    replyToOntology(e.getKey());
                }

            });
            ontologyTitlePanel.add(replyToOntologyButton);
            innerPanel.add(ontologyTitlePanel);

            //JPanel commentPanel = new JPanel();
            //commentPanel.setLayout(new BoxLayout(commentPanel, BoxLayout.Y_AXIS));
            //commentPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS));
            int i = 0;

            LocusComment lastRootComment = null;
            JPanel lastStatusChangePanel = null;
            boolean isRootComment = false;
            JPanel currentPanel = innerPanel;
            JPanel iconPanel = null;
            for (final LocusComment lc : e.getValue()) {
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

                iconPanel = new JPanel();
                iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.X_AXIS));
                iconPanel.setPreferredSize(new Dimension(ICON_WIDTH * 3 + ICON_SPACING * 2, ICON_HEIGHT));
                iconPanel.add(lc.isPendingReview() ? new JLabel(PENDING_REVIEW_ICON) : new JLabel(NOT_PENDING_REVIEW_ICON));
                iconPanel.add(lc.isIncluded() ? new JLabel(INCLUDED_ICON) : new JLabel(NOT_INCLUDED_ICON));
                iconPanel.add(lc.isApproved() ? new JLabel(APPROVED_ICON) : new JLabel(NOT_APPROVED_ICON));
                if (LoginController.getInstance().getUserLevel() == UserLevel.ADMIN) {
                    JButton statusEditButton = new JButton(EDIT_STATUS_BUTTON_ICON);
                    statusEditButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            editStatus(lc);
                        }

                    });
                    iconPanel.add(statusEditButton);
                }

                if (lc.getOriginalComment() == null) { //Not a status-change comment.
                    if (lastStatusChangePanel != null) {
                        lastStatusChangePanel.add(new JLabel(" to "));
                        lastStatusChangePanel.add(iconPanel);
                        currentPanel.add(lastStatusChangePanel);
                    }
                    lastRootComment = lc;
                    //root level comment.                        
                    lastStatusChangePanel = null;
                    isRootComment = true;
                    currentPanel = innerPanel;
                } else if (lastStatusChangePanel == null) { //status change comment.
                    isRootComment = false;
                    //first status change comment.
                    currentPanel = getNewHistoryPanel();
                }

                if (isRootComment) {
                    headerPanel.add(iconPanel);
                } else {
                    JPanel statusChangePanel = new JPanel();
                    statusChangePanel.setLayout(new BoxLayout(statusChangePanel, BoxLayout.X_AXIS));

                    if (lastStatusChangePanel != null) {
                        lastStatusChangePanel.add(new JLabel(" to "));
                        lastStatusChangePanel.add(iconPanel);
                        currentPanel.add(lastStatusChangePanel);
                    }
                    statusChangePanel.add(new JLabel("Status changed from: "));
                    statusChangePanel.add(iconPanel);

                    lastStatusChangePanel = statusChangePanel;
                }

                currentPanel.add(headerPanel);

                //Add the actual comment as a JTextArea.
                JPanel commentPanel = new JPanel();
                commentPanel.setBackground(BACKGROUND_COLORS[i++ % BACKGROUND_COLORS.length]);
                JTextArea commentText = new JTextArea();
                commentText.setText(lc.getCommentText());
                commentText.setEditable(false);
                commentText.setLineWrap(true);
                commentPanel.add(commentText);
                currentPanel.add(commentPanel);

            }
            if (!isRootComment) {
                lastStatusChangePanel.add(new JLabel(" to "));
                lastStatusChangePanel.add(iconPanel);
                currentPanel.add(lastStatusChangePanel);
            }
            innerPanel.add(new JSeparator());
        }
        return innerPanel;
    }

    private void newComment(VariantRecord r) {
      //  HERE: need to popup a dialog to add new comment
        //choose a disease associated with this variant.
        
        //set text for this comment.
        //LocusComment lc = public LocusComment(Boolean isApproved, Boolean isIncluded, Boolean isPendingReview, Boolean isDeleted, String comment, OntologyTerm ontologyTerm) {
        final Boolean isApproved = false;
        final Boolean isIncluded = false;
        final Boolean isPendingReview = false;
        final Boolean isDeleted = false;
        
       // OntologyTerm ot = ??;
        //String comment = "???";
                
       // LocusComment lc = new LocusComment(isApproved, isIncluded, isPendingReview, isDeleted, comment, ontologyTerm);
    }

    private JPanel getNoCommentsPanel() {
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.add(Box.createVerticalGlue());
        innerPanel.add(new JLabel("No Comments"));
        innerPanel.add(Box.createVerticalGlue());
        return innerPanel;
    }

    @Override
    public void setVariantRecord(final VariantRecord variantRecord) {
        //getLocusCommentGroup(String sessID, int projectId, int refId, String chrom, int start_position, int end_position, String ref, String alt)
        try {
            System.out.println("LocusCommenterApp: Setting variant record to " + variantRecord);
            //Get comment group associated with this variant.
            LocusCommentGroup lcg = MedSavantClient.VariantManager.getLocusCommentGroup(
                    LoginController.getSessionID(),
                    ProjectController.getInstance().getCurrentProjectID(),
                    ReferenceController.getInstance().getCurrentReferenceID(),
                    variantRecord);
            int numComments = 0;
            JPanel innerPanel = null;
            if (lcg != null) {
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
                        numComments++;
                        ontologyComments.add(lc);
                    }
                    otCommentMap.put(lc.getOntologyTerm(), ontologyComments);
                }
                if (numComments > 0) {
                    innerPanel = getMainCommentPanel(otCommentMap);
                }
            }

            if (numComments == 0) {
                innerPanel = getNoCommentsPanel();
            }

            JButton newCommentButton = new JButton("New Comment");
            newCommentButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    newComment(variantRecord);
                }
            });
            final JScrollPane jsp = new JScrollPane(innerPanel);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {

                    panel.removeAll();
                    panel.add(jsp);
                    panel.revalidate();
                    panel.repaint();
                }
            });

        } catch (SessionExpiredException see) {
            LOG.error(see.getMessage(), see);
        } catch (SQLException sqe) {
            LOG.error(sqe.getMessage(), sqe);

        } catch (RemoteException rex) {
            LOG.error(rex.getMessage(), rex);
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
