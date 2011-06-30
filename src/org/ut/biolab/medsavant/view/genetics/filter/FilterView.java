/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class FilterView {

    private JComponent _component;
    private JFrame _frame;
    private String _title;
    private FilterViewType _type;
    
    public enum FilterViewType {NORMAL, FRAME};

    public FilterView(String title, JComponent content) {
        setTitle(title);
        setComponent(content);
        setFilterViewType(FilterViewType.NORMAL);
    }
    
    public FilterView(String title, JFrame frame){
        setTitle(title);
        setFrame(frame);
        setFilterViewType(FilterViewType.FRAME);
        setComponent(new JPanel());
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
    
    private void setFrame(JFrame frame) {
        _frame = frame;
    }
    
    public JFrame getFrame(){
        return _frame;
    }

    private void setFilterViewType(FilterViewType type){
        _type = type;
    }
    
    public FilterViewType getFilterViewType(){
        return _type;
    }
}
