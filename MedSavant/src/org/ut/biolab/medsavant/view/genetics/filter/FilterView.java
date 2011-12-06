/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 *
 * @author mfiume
 */
public abstract class FilterView {

    private JComponent _component;
    private JFrame _frame;
    private String _title;    

    public FilterView(String title, JComponent content) {
        setTitle(title);
        setComponent(content);
    }

    public String getTitle() {
        return _title;
    }

    private void setTitle(String title) {
        _title = title;
    }

    public JComponent getComponent() {
        return _component;
    }

    private void setComponent(JComponent component) {
        _component = component;
    }
    
    public abstract FilterState saveState();
    
    /**
     * Give derived classes a chance to clean up when the filter instance is being removed.
     */
    public void cleanup() {
    }
}
