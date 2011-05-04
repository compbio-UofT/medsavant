/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.filter;

import javax.swing.JComponent;

/**
 *
 * @author mfiume
 */
class FilterView {

    private JComponent _component;
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

}
