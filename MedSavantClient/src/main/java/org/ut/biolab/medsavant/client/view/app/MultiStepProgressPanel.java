/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.client.view.app;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.app.MultiStepProgressPanel.Step.StepStatus;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class MultiStepProgressPanel extends JPanel {
    private ActionListener cancelAction;
    private final ArrayList<Step> steps;
    private Step currentStep;
    
    public MultiStepProgressPanel() {
        steps = new ArrayList<Step>();
        initView();
    }
    
    public void setCancelAction(ActionListener cancelActionListener) {
        this.cancelAction = cancelActionListener;
    }

    public void reset() {
        steps.removeAll(steps);
        currentStep = null;
        refreshView();
    }
    
    private void initView() {
    }
    
    public void stepFailed() {
        if (currentStep != null) {
            currentStep.setStatus(StepStatus.FAILED);
        }
        refreshView();
    }
    
    public void stepCompleted() {
        if (currentStep != null) {
            currentStep.setStatus(StepStatus.COMPLETED);
        }
        refreshView();
    }
    
    public void startNewStepWithDescription(String d) {
        Step s = new Step(d);
        currentStep = s;
        s.setStatus(StepStatus.INPROGRESS);
        steps.add(s);
        refreshView();
    }

    private void refreshView() {
        this.removeAll();
        
        MigLayout l = new MigLayout("wrap 2");
        this.setLayout(l);
        
        for (Step s : steps) {
            
            switch (s.getStatus()) {
                case UNSTARTED:
                    this.add(new JLabel("?"));
                    break;
                case INPROGRESS:
                    this.add(ViewUtil.getIndeterminateProgressBar());
                    break;
                case FAILED:
                    this.add(new JLabel("x"));
                    break;
                case COMPLETED:
                    this.add(new JLabel("check"));
                    break;
                default:
                    // should never happen
                    this.add(ViewUtil.getClearPanel());
                    break;
            }
            
            this.add(new JLabel(s.getDescription()));
        }
        
    }

    static class Step {
        
        private String description;
        private StepStatus status;
        
        public enum StepStatus { UNSTARTED, INPROGRESS, FAILED, COMPLETED };

        public Step(String description) {
            this.description = description;
            this.status = StepStatus.UNSTARTED;
        }
        
        public void setStatus(StepStatus s) {
            this.status = s;
        }

        public String getDescription() {
            return description;
        }

        public StepStatus getStatus() {
            return status;
        }
    }
    
    
}
