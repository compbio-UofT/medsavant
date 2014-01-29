/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.mfiume.query.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.ut.biolab.medsavant.client.view.component.ProgressWheel;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author jim
 */
public class SearchConditionPanel extends JPanel{

    private static final int BORDER_PADDING = 5;
    private final JPanel horizButtonPanel = new JPanel();
    private final SearchConditionEditorView editor;
    private final JPanel conditionsEditorPanel;
    
    public void loadEditorViewInBackground(final Runnable onFinish){
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    editor.loadViewFromExistingSearchConditionParameters();
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            conditionsEditorPanel.removeAll();
                            conditionsEditorPanel.add(editor);
                            if(onFinish != null){
                                onFinish.run();
                            }
                            updateUI();
                        }
                    });
                } catch (Exception ex) {
                    Logger.getLogger(SearchConditionItemView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        t.start();
    }
    
    public SearchConditionPanel(final SearchConditionEditorView editor, final JPopupMenu advancedMenu) {
        this.editor = editor;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        conditionsEditorPanel = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(conditionsEditorPanel);
        ProgressWheel waitForConditions = ViewUtil.getIndeterminateProgressBar();
        conditionsEditorPanel.add(ViewUtil.centerHorizontally(new JLabel("Preparing condition,")));
        conditionsEditorPanel.add(Box.createVerticalStrut(5));
        conditionsEditorPanel.add(ViewUtil.centerHorizontally(new JLabel("please wait...")));
        conditionsEditorPanel.add(ViewUtil.centerHorizontally(waitForConditions));

        add(conditionsEditorPanel);
        setBorder(new EmptyBorder(BORDER_PADDING, BORDER_PADDING, BORDER_PADDING, BORDER_PADDING));

        
        
        horizButtonPanel.setLayout(new BoxLayout(horizButtonPanel, BoxLayout.X_AXIS));        
        if (advancedMenu != null && (advancedMenu.getComponentCount() > 0)) {
            final JButton gearButton = ViewUtil.getConfigureButton();
            gearButton.setToolTipText("More Options");
            gearButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    advancedMenu.show(gearButton, 0, gearButton.getHeight());
                }

            });
            horizButtonPanel.add(gearButton);
        }

        horizButtonPanel.add(Box.createHorizontalGlue());
                
        add(Box.createVerticalGlue());
        add(horizButtonPanel);       
    }
    
    public JPanel getButtonPanel() {
        return horizButtonPanel;
    }
}
