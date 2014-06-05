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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.app.api.AppNotInitializedException;
import org.ut.biolab.medsavant.client.app.api.AppRoleManagerBuilder;
import org.ut.biolab.medsavant.client.app.api.AppRoleManagerBuilder.AppRoleManager;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.appapi.MedSavantVariantInspectorApp;
import org.ut.biolab.medsavant.shared.format.UserRole;
import org.ut.biolab.medsavant.shared.model.UserComment;
import org.ut.biolab.medsavant.shared.model.UserCommentGroup;
import org.ut.biolab.medsavant.shared.model.OntologyTerm;
import org.ut.biolab.medsavant.shared.model.OntologyType;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.UserLevel;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;
import savant.api.util.DialogUtils;

public class UserCommentApp extends MedSavantVariantInspectorApp {

    private static final Log LOG = LogFactory.getLog(UserCommentApp.class);
    private static final OntologyType DEFAULT_ONTOLOGY_TYPE = OntologyType.HPO;
    private static final String NAME = "Cancer Workflow - User Comments";
    private static final String TITLE = "User Comments for this position ";
    private static final int ICON_WIDTH = 16;
    private static final int ICON_HEIGHT = 16;
    private static final int COMMENT_SEPARATOR_HEIGHT = 10;
    private static final int ONTOLOGY_SEPARATOR_HEIGHT = 20;
    private static final ImageIcon REPLY_TO_ONTOLOGY_ICON = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ACTION_ON_TOOLBAR);
    private static final ImageIcon EDIT_STATUS_BUTTON_ICON = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.EDIT);
    private static final int COMMENTTEXT_PREFERRED_WIDTH = 200;
    private static final int COMMENTTEXT_PREFERRED_HEIGHT = 100;

    private Set<UserRole> rolesForThisUser; //All roles associated to this user. (user running the app).

    private final JPanel panel;

    public static OntologyType getDefaultOntologyType() {
        return DEFAULT_ONTOLOGY_TYPE;
    }

    public static final String GENETIC_COUNSELLOR_ROLENAME = "Genetic Counsellor";
    private static final String GENETIC_COUNSELLOR_DESCRIPTION = "Genetic Counsellor";
    private static final Set<UserLevel> GENETIC_COUNSELLOR_USERLEVELS = EnumSet.of(UserLevel.ADMIN);

    public static final String TECHNICIAN_ROLENAME = "Technician";
    private static final String TECHNICIAN_DESCRIPTION = "Technician";
    private static final Set<UserLevel> TECHNICIAN_USERLEVELS = EnumSet.allOf(UserLevel.class);

    public static final String RESIDENT_ROLENAME = "Resident";
    private static final String RESIDENT_DESCRIPTION = "Resident";
    private static final Set<UserLevel> RESIDENT_USERLEVELS = EnumSet.of(UserLevel.ADMIN, UserLevel.USER);

    private static AppRoleManager roleManager;

    static AppRoleManager getRoleManager() {
        return roleManager;
    }

    public UserCommentApp() throws AppNotInitializedException {
        System.out.println("User Comment App initialized.");
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JPanel innerPanel = new WaitPanel("Loading...");
        panel.add(innerPanel, BorderLayout.CENTER);
        try {
            if (roleManager == null) {
                AppRoleManagerBuilder armBuilder = new AppRoleManagerBuilder();
                roleManager = armBuilder
                        .addRole(GENETIC_COUNSELLOR_ROLENAME, GENETIC_COUNSELLOR_DESCRIPTION, GENETIC_COUNSELLOR_USERLEVELS)
                        .addRole(RESIDENT_ROLENAME, RESIDENT_DESCRIPTION, RESIDENT_USERLEVELS)
                        .addDefaultRole(TECHNICIAN_ROLENAME, TECHNICIAN_DESCRIPTION, TECHNICIAN_USERLEVELS)
                        .autoAssignRolesToExistingUsers(true)
                        .build();
            }
        } catch (AppNotInitializedException anie) {
            LOG.error(anie);
            DialogUtils.displayError("The app '" + getTitle() + "' requires initialization by the project administrator.  Some features will be disabled.");
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error("Couldn't fetch this user's roles from database");
            DialogUtils.displayError("Error setting up user roles for the App " + getTitle());
        }
    }

    private void replyToOntology(VariantRecord vr, OntologyTerm ot) {
        JDialog acd = new AddNewCommentDialog(MedSavantFrame.getInstance(), vr, ot);
        acd.setVisible(true);
    }

    private void editStatus(UserCommentGroup lcg, UserComment lc) {
        JDialog scd = new SetCommentStatusDialog(MedSavantFrame.getInstance(), lcg, lc);
        scd.setVisible(true);
    }

    //Define horizontal panel with ontology title, and button to
    //post to the ontology.
    private JPanel getOntologyTitlePanel(final VariantRecord vr, final OntologyTerm ot) {
        String header = ot.getID() + " - " + ot.getName(); //ontology title.

        JPanel ontologyTitlePanel = new JPanel();
        ontologyTitlePanel.setLayout(new BoxLayout(ontologyTitlePanel, BoxLayout.X_AXIS));
        ontologyTitlePanel.add(new JLabel(header));
        ontologyTitlePanel.add(Box.createHorizontalGlue());

        JButton replyToOntologyButton = new JButton(new ImageIcon(REPLY_TO_ONTOLOGY_ICON.getImage().getScaledInstance(
                ICON_WIDTH, ICON_HEIGHT, java.awt.Image.SCALE_SMOOTH
        )));

        replyToOntologyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                replyToOntology(vr, ot);
            }
        });

        ontologyTitlePanel.add(replyToOntologyButton);
        return ontologyTitlePanel;
    }

    private JSeparator getCommentSeparator() {
        JSeparator js = new JSeparator();
        js.setPreferredSize(new Dimension(panel.getWidth(), COMMENT_SEPARATOR_HEIGHT));
        return js;
    }

    private JSeparator getOntologySeparator() {
        JSeparator js = new JSeparator();
        js.setPreferredSize(new Dimension(panel.getWidth(), ONTOLOGY_SEPARATOR_HEIGHT));
        return js;
    }

    private JPanel getHeaderPanel(UserComment lc) {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        JPanel leftJustPanel = new JPanel();
        leftJustPanel.setLayout(new BoxLayout(leftJustPanel, BoxLayout.X_AXIS));
        leftJustPanel.add(new JLabel("Posted " + lc.getModificationDate().toString() + " by: "));
        leftJustPanel.add(Box.createHorizontalGlue());
        headerPanel.add(leftJustPanel);

        leftJustPanel = new JPanel();
        leftJustPanel.setLayout(new BoxLayout(leftJustPanel, BoxLayout.X_AXIS));
        leftJustPanel.add(new JLabel(lc.getUser()));
        leftJustPanel.add(Box.createHorizontalGlue());
        headerPanel.add(leftJustPanel);
        headerPanel.add(Box.createHorizontalGlue());
        return headerPanel;
    }

    private JPanel getStatusIconPanel(final UserCommentGroup lcg, final UserComment lc) {
        JPanel sip = new JPanel();
        sip.setLayout(new BoxLayout(sip, BoxLayout.X_AXIS));

        JPanel statusIconPanel = new StatusIconPanel(ICON_WIDTH, ICON_HEIGHT, false,
                lc.isApproved(), lc.isIncluded(), lc.isDeleted());

        sip.add(statusIconPanel);
        sip.add(Box.createHorizontalGlue());

        //technicains and admins can change status, but technicians can't.
        if (roleManager.checkRole(GENETIC_COUNSELLOR_ROLENAME) || roleManager.checkRole(RESIDENT_ROLENAME)) {
            JButton statusEditButton
                    = new JButton(new ImageIcon(EDIT_STATUS_BUTTON_ICON.getImage().getScaledInstance(
                                            ICON_WIDTH, ICON_HEIGHT, java.awt.Image.SCALE_SMOOTH
                                    )));

            statusEditButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editStatus(lcg, lc);
                }
            });
            sip.add(statusEditButton);
        }

        return sip;
    }

    private JPanel getCommentPanel(UserComment lc) {
        JTextArea commentText = new JTextArea();
        commentText.setText(lc.getCommentText());
        commentText.setEditable(false);
        commentText.setLineWrap(true);
        commentText.setPreferredSize(new Dimension(COMMENTTEXT_PREFERRED_WIDTH, COMMENTTEXT_PREFERRED_HEIGHT));
        JScrollPane jsp = new JScrollPane(commentText);

        JPanel outerCommentPanel = new JPanel();
        outerCommentPanel.setLayout(new BoxLayout(outerCommentPanel, BoxLayout.X_AXIS));
        outerCommentPanel.add(jsp);
        outerCommentPanel.add(Box.createHorizontalGlue());
        return outerCommentPanel;
    }

    private static final int STATUS_COMMENT_INDENT_WIDTH = 20;

    private JPanel oldStatusPanel;

    private void updateStatusPanel(UserComment oldComment) {
        //Write the from-to status to the last comment's status panel.
        if (oldComment != null && oldComment.getOriginalComment() != null) {//status change comment.                        
            JPanel statusIconPanel = new StatusIconPanel(ICON_WIDTH, ICON_HEIGHT, false,
                    oldComment.getOriginalComment().isApproved(),
                    oldComment.getOriginalComment().isIncluded(),
                    oldComment.getOriginalComment().isDeleted());

            oldStatusPanel.add(new JLabel(" to "));
            oldStatusPanel.add(statusIconPanel);
            oldStatusPanel.add(Box.createHorizontalGlue());
            oldStatusPanel = null;
        }
    }

    private JPanel getCommentBlock(UserCommentGroup lcg, UserComment lc/*, UserComment oldComment*/) {

        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));

        JPanel headerPanel = getHeaderPanel(lc);
        JPanel commentPanel = getCommentPanel(lc);

        if (lc.getOriginalComment() == null) { //root level comment.
            innerPanel.add(headerPanel);
            innerPanel.add(getStatusIconPanel(lcg, lc));
            innerPanel.add(commentPanel);
            innerPanel.add(getCommentSeparator());

            return innerPanel;
        } else { //status comment.
            JPanel outerPanel = new JPanel();
            outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.X_AXIS));
            JSeparator js = new JSeparator();
            js.setPreferredSize(new Dimension(STATUS_COMMENT_INDENT_WIDTH, 1));
            outerPanel.add(js);

            innerPanel.add(headerPanel);

            //System.out.println("Constructing statusIconPanel with "+lc.isApproved()+","+lc.isIncluded()+","+lc.isPendingReview());
            JPanel statusIconPanel = new StatusIconPanel(ICON_WIDTH, ICON_HEIGHT, false,
                    lc.isApproved(), lc.isIncluded(), lc.isDeleted());

            JPanel centeredLabel = new JPanel();
            centeredLabel.setLayout(new BoxLayout(centeredLabel, BoxLayout.X_AXIS));
            centeredLabel.add(Box.createHorizontalGlue());
            centeredLabel.add(new JLabel("Status Change:"));
            centeredLabel.add(Box.createHorizontalGlue());
            innerPanel.add(centeredLabel);
            oldStatusPanel = new JPanel();
            oldStatusPanel.setLayout(new BoxLayout(oldStatusPanel, BoxLayout.X_AXIS));
            oldStatusPanel.add(Box.createHorizontalGlue());
            oldStatusPanel.add(statusIconPanel);
            innerPanel.add(oldStatusPanel);
            innerPanel.add(commentPanel);
            innerPanel.add(getCommentSeparator());
            outerPanel.add(innerPanel);
            return outerPanel;
        }
    }

    private JPanel getMainCommentPanel(Map<OntologyTerm, Collection<UserComment>> otCommentMap, final UserCommentGroup lcg, final VariantRecord vr) {
        JPanel innerPanel = new JPanel(); //Todo: may need to specify width of this panel?                
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        for (final Map.Entry<OntologyTerm, Collection<UserComment>> e : otCommentMap.entrySet()) {
            OntologyTerm ot = e.getKey();
            Collection<UserComment> userComments = e.getValue();

            JPanel otPanel = new JPanel();
            otPanel.setLayout(new BoxLayout(otPanel, BoxLayout.Y_AXIS));
            //otPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            //Define horizontal panel with ontology title and reply to ontology icon.
            JPanel ontologyTitlePanel = getOntologyTitlePanel(vr, ot);
            otPanel.add(ontologyTitlePanel);
            UserComment oldComment = null;
            for (final UserComment userComment : userComments) {
                if (userComment.isDeleted()) {
                    continue;
                }
                updateStatusPanel(oldComment);
                otPanel.add(getCommentBlock(lcg, userComment));
                oldComment = userComment;
            }
            updateStatusPanel(oldComment);
            otPanel.add(getOntologySeparator());
            innerPanel.add(otPanel);
        }

        return innerPanel;
    }

    private void newComment(VariantRecord vr) {
        JDialog acd = new AddNewCommentDialog(MedSavantFrame.getInstance(), vr);
        acd.setVisible(true);
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
        try {
            //Get comment group associated with this variant.
            UserCommentGroup lcg = MedSavantClient.VariantManager.getUserCommentGroup(
                    LoginController.getSessionID(),
                    ProjectController.getInstance().getCurrentProjectID(),
                    ReferenceController.getInstance().getCurrentReferenceID(),
                    variantRecord);
            boolean hasComments = false;
            JPanel innerPanel = null;
            if (lcg != null) {
                //Build a mapping from ontology terms to all comments pertaining to that ontology term.
                //Iterating through this map will return the ontology terms in alphabetical order, and the comments
                //within each ontology will be ordered by their insertion id.
                Map<OntologyTerm, Collection<UserComment>> otCommentMap = new TreeMap<OntologyTerm, Collection<UserComment>>();

                for (Iterator<UserComment> li = lcg.iterator(); li.hasNext();) {
                    UserComment lc = li.next();
                    Collection<UserComment> ontologyComments = otCommentMap.get(lc.getOntologyTerm());
                    if (ontologyComments == null) {
                        ontologyComments = new ArrayList<UserComment>();
                        hasComments = true;
                    }
                    ontologyComments.add(lc);

                    otCommentMap.put(lc.getOntologyTerm(), ontologyComments);
                }
                if (hasComments) {
                    innerPanel = getMainCommentPanel(otCommentMap, lcg, variantRecord);
                }
            }

            if (innerPanel == null) {
                innerPanel = getNoCommentsPanel();
            }

            JButton newCommentButton = new JButton("New Comment");
            newCommentButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    newComment(variantRecord);
                }
            });

            JPanel cp = new JPanel();
            cp.setLayout(new BoxLayout(cp, BoxLayout.X_AXIS));
            cp.add(Box.createHorizontalGlue());
            if (roleManager.checkRole(GENETIC_COUNSELLOR_ROLENAME) || roleManager.checkRole(RESIDENT_ROLENAME)) {
                cp.add(newCommentButton);
            }
            cp.add(Box.createHorizontalGlue());
            innerPanel.add(cp);
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
