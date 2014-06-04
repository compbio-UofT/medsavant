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
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.format.UserRole;
import org.ut.biolab.medsavant.shared.model.UserComment;
import org.ut.biolab.medsavant.shared.model.UserCommentGroup;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import savant.api.util.DialogUtils;

public class SetCommentStatusDialog extends JDialog {

    private Log LOG = LogFactory.getLog(SetCommentStatusDialog.class);
    private final UserCommentGroup lcg;
    private final UserComment parentComment;
    private JTextArea comment;
    private StatusIconPanel statusIconPanel;

    private static final int MINIMUM_WIDTH = 640;
    private static final int MINIMUM_HEIGHT = 480;

    private static final int ICON_HEIGHT = 64;
    private static final int ICON_WIDTH = 64;
    private final int mainPanelWidth;
    private final int mainPanelHeight;    
    public SetCommentStatusDialog(JFrame parentFrame, UserCommentGroup lcg, UserComment parentComment) {
        super(parentFrame);
        System.out.println("Constructing setcomment status dialog with parentComment id=" + parentComment.getCommentID());
        this.parentComment = parentComment;
        this.lcg = lcg;
        JPanel mainPanel = getMainPanel();
        this.add(mainPanel);
        Dimension pd = parentFrame.getSize();
        mainPanelWidth = Math.max(MINIMUM_WIDTH, pd.width / 2);
        mainPanelHeight = Math.max(MINIMUM_HEIGHT, pd.height / 2);
        this.setPreferredSize(new Dimension(mainPanelWidth, mainPanelHeight));
        this.pack();
        this.setModal(true);
    }

    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        //Status box.
        mainPanel.add(getStatusEditorPanel());

        //Comment box.
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createHorizontalGlue());
        comment = new JTextArea();
        p.add(comment);
        p.add(Box.createHorizontalGlue());
        mainPanel.add(p);

        //Button box.        
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createHorizontalGlue());
        JButton OKButton = new JButton("Update status");
        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    updateStatus();
                    dispose();
                } catch (Exception ex) {
                    LOG.error(ex.getMessage(), ex);
                    DialogUtils.displayException("Error", ex.getMessage(), ex);
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
        p.add(OKButton);
        p.add(cancelButton);
        p.add(Box.createHorizontalGlue());
        mainPanel.add(p);

        return mainPanel;
    }

    private JPanel getStatusEditorPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createHorizontalGlue());
        statusIconPanel = new StatusIconPanel(ICON_WIDTH,
                ICON_HEIGHT,
                true,
                parentComment.isApproved(), parentComment.isIncluded(), parentComment.isDeleted());
        p.add(statusIconPanel);
        p.add(Box.createHorizontalGlue());
        return p;
    }

    private void updateStatus() throws SessionExpiredException, SQLException, RemoteException {
        String sessID = LoginController.getSessionID();
        UserComment lc = new UserComment(parentComment,
                statusIconPanel.getApprovedIcon().getState(),
                statusIconPanel.getIncludedIcon().getState(),                
                statusIconPanel.getDeletedIcon().getState(),
                comment.getText());
                                        
        MedSavantClient.VariantManager.replyToUserCommentGroup(sessID, lcg.getUserCommentGroupId(), lc);
    }
}
