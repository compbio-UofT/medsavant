/*
 *    Copyright 2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.view.genetics.variantinfo;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.VariantComment;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.SQLUtils;
import org.ut.biolab.medsavant.vcf.VariantRecord;
import org.ut.biolab.medsavant.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.view.genetics.TablePanel;
import org.ut.biolab.medsavant.view.genetics.inspector.SubInspector;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class SocialVariantSubInspector extends SubInspector implements Listener<VariantRecord> {

    private VariantRecord selectedVariant;
    private final JTextArea ta = new JTextArea();
    private JPanel existingCommentsPanel;

    public SocialVariantSubInspector() {
        TablePanel.addVariantSelectionChangedListener(this);
    }

    @Override
    public String getName() {
        return "Comments";
    }

    @Override
    public JPanel getInfoPanel() {
        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(p);

        existingCommentsPanel = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(existingCommentsPanel);

        p.add(ViewUtil.getClearBorderlessScrollPane(ta));

        JButton submit = new JButton("Submit");
        ViewUtil.makeSmall(submit);
        p.add(ViewUtil.alignRight(submit));

        final SocialVariantSubInspector instance = this;

        submit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {

                System.out.println("Commenting on " + selectedVariant);

                List<VariantComment> list = new ArrayList<VariantComment>();

                VariantComment sv = new VariantComment(
                        ProjectController.getInstance().getCurrentProjectID(),
                        ReferenceController.getInstance().getCurrentReferenceID(),
                        selectedVariant.getUploadID(),
                        selectedVariant.getFileID(),
                        selectedVariant.getVariantID(),
                        LoginController.getInstance().getUserName(),
                        ta.getText(),
                        SQLUtils.getCurrentTimestamp());

                list.add(sv);
                try {
                    MedSavantClient.VariantManager.addVariantComments(LoginController.sessionId, list);
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Error adding variant comments: %s", ex);
                }

                ta.setText("");

                instance.updateComments();
            }
        });

        p.add(existingCommentsPanel);

        ta.setBorder(ViewUtil.getTinyLineBorder());
        ta.setRows(3);
        ta.setColumns(10);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);

        return p;
    }

    @Override
    public void handleEvent(VariantRecord r) {
        this.selectedVariant = r;
        ta.setText("");
        updateComments();
    }

    private void updateComments() {
        existingCommentsPanel.removeAll();

        KeyValuePairPanel kvp = new KeyValuePairPanel();
        kvp.setKeysVisible(false);

        existingCommentsPanel.add(kvp);

        try {
            List<VariantComment> comments = MedSavantClient.VariantManager.getVariantComments(
                    LoginController.sessionId,
                    ProjectController.getInstance().getCurrentProjectID(),
                    ReferenceController.getInstance().getCurrentReferenceID(),
                    selectedVariant.getUploadID(),
                    selectedVariant.getFileID(),
                    selectedVariant.getVariantID());

            Collections.reverse(comments);

            parent.setTitle(getName() + (comments.isEmpty() ? "" : " (" + comments.size() + ")"));

            int row = 0;
            for (VariantComment sv : comments) {
                String key = (row++) + "";

                if (row == 5) {
                    kvp.addMoreRow();
                }

                kvp.addKey(key);
                kvp.setValue(key, new StarredVariantCommentPanel(sv));
            }

            existingCommentsPanel.updateUI();

        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error updating comments: %s", ex);
        }
    }

    class StarredVariantCommentPanel extends JPanel {

        private JLabel userLabel;
        private JLabel timeStampLabel;
        private JTextArea textArea;
        private final VariantComment comment;

        public StarredVariantCommentPanel(VariantComment v) {
            this.setOpaque(false);
            this.setBorder(ViewUtil.getSmallBorder());

            this.comment = v;
            initComponents();

            userLabel.setText(v.getUser());
            timeStampLabel.setText(v.getTimestamp().toLocaleString());
            textArea.setText(v.getDescription());
        }

        private void initComponents() {

            this.setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.NORTHWEST;

            JPanel header = ViewUtil.getClearPanel();
            ViewUtil.applyHorizontalBoxLayout(header);


            JPanel h2 = ViewUtil.getClearPanel();
            ViewUtil.applyVerticalBoxLayout(h2);

            userLabel = new JLabel();
            userLabel.setFont(ViewUtil.getSmallTitleFont());

            timeStampLabel = new JLabel();

            h2.add(userLabel);
            h2.add(timeStampLabel);

            header.add(h2);

            header.add(Box.createHorizontalGlue());

            if (comment.getUser().equals(LoginController.getInstance().getUserName())) {
                JButton rem = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CLEAR));
                header.add(rem);

                rem.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        List<VariantComment> list = new ArrayList<VariantComment>();
                        list.add(comment);
                        try {
                            MedSavantClient.VariantManager.removeVariantComments(LoginController.sessionId, list);
                        } catch (Exception ex) {
                            ClientMiscUtils.reportError("Error removing variant comments: %s", ex);
                        }
                        updateComments();
                    }
                });
            }

            textArea = new JTextArea("");
            textArea.setOpaque(false);
            textArea.setColumns(20);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);
            textArea.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));

            this.add(header, c);

            c.gridy++;
            c.weighty = 1;
            c.fill = GridBagConstraints.VERTICAL;

            this.add(textArea, c);

            Font defaultFont = header.getFont();

            Font userFont = new Font(defaultFont.getFamily(), Font.BOLD, 12);
            Font timeStampFont = new Font(defaultFont.getFamily(), Font.PLAIN, 9);

            userLabel.setFont(userFont);
            timeStampLabel.setFont(timeStampFont);

            timeStampLabel.setForeground(Color.gray);
            textArea.setForeground(Color.darkGray);
        }
    }
}
