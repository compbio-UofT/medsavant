/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.mfiume.query;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.ut.biolab.mfiume.query.value.DefaultStringConditionValueGenerator;
import org.ut.biolab.mfiume.query.view.SearchConditionEditorView;
import org.ut.biolab.mfiume.query.view.SearchConditionItemView;
import org.ut.biolab.mfiume.query.view.StringSearchConditionEditorView;

/**
 *
 * @author mfiume
 */
public class QueryConstructor {

    public static void main(String[] args) {

        JFrame f = new JFrame();
        f.setPreferredSize(new Dimension(500,500));

        final SearchConditionGroupItem entireQueryModel = new SearchConditionGroupItem(null);
        final QueryView entireQueryView = new QueryView(entireQueryModel,new SillyConditionGenerator());

        final JTextField field = new JTextField();
        field.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent ke) {
            }

            @Override
            public void keyPressed(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_ENTER) {

                    SearchConditionItem item = new SearchConditionItem(field.getText(),entireQueryModel);
                    StringSearchConditionEditorView editor = new StringSearchConditionEditorView(item, new DefaultStringConditionValueGenerator());
                    SearchConditionItemView view = new SearchConditionItemView(item,editor);
                    entireQueryView.registerViewWithItem(view,item);
                    entireQueryModel.addItem(item);

                    field.setText("");
                }
            }

            @Override
            public void keyReleased(KeyEvent ke) {
            }
        });

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(entireQueryView, BorderLayout.CENTER);


        f.add(p);
        f.pack();
        f.setVisible(true);

    }
}
