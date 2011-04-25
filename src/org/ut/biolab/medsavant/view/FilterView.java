/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

/**
 *
 * @author mfiume
 */
class FilterView {

    private JComponent _component;
    private String _title;
    private FilterGenerator filterGenerator;

    public FilterView(String title, JComponent content, FilterGenerator fg) {
        setTitle(title);
        setComponent(content);
        setFilterGenerator(fg);
    }

    public String getTitle() {
        return _title;
    }

    private void setTitle(String title) {
        _title = title;
    }

    public JComponent getComponent() {
        return new JScrollPane(_component);
    }

    private void setComponent(JComponent component) {
        _component = component;
    }

    private void setFilterGenerator(FilterGenerator fg) {
        this.filterGenerator = fg;
    }

     public FilterGenerator getFilterGenerator() {
        return filterGenerator;
    }
}
