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
    private String _title;
    protected int queryId;

    public FilterView(String title, JComponent content, int queryId) {
        setTitle(title);
        setComponent(content);
        this.queryId = queryId;
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

    public int getQueryId(){
        return queryId;
    }
}
