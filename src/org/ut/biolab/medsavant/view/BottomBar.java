/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.sql.SQLException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import medsavant.exception.AccessDeniedDatabaseException;
import medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class BottomBar extends JPanel implements FiltersChangedListener{
    private static BottomBar instance;

    public static BottomBar getInstance() {
        if (instance == null) {
            instance = new BottomBar();
        }

        return instance;
    }
    private final JLabel statusLabel;
    private final JLabel loginStatusLabel;

    public BottomBar() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBorder(ViewUtil.getMediumBorder());
        this.setPreferredSize(new Dimension(20,20));

        loginStatusLabel = new JLabel();


        statusLabel = new JLabel("");
        statusLabel.setFont(ViewUtil.getMediumTitleFont());
        
        this.add(loginStatusLabel);
        this.add(Box.createHorizontalGlue());
        this.add(statusLabel);
        this.add(Box.createHorizontalGlue());

        FilterController.addFilterListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        GradientPaint p = new GradientPaint(0, 0, Color.white, 0, 40, Color.lightGray);
        ((Graphics2D) g).setPaint(p);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }

    public void filtersChanged() throws SQLException, FatalDatabaseException, AccessDeniedDatabaseException {
        //setStatus(ResultController.getInstance().getAllVariantRecords().size() + " records");
    }

    public void updateLoginStatus() {
        if (LoginController.isLoggedIn()) {
            this.loginStatusLabel.setText("Signed in as " + LoginController.getUsername());
        } else {
            this.loginStatusLabel.setText("Not signed in");
        }
    } 

    private void setStatus(String status) {
        statusLabel.setText(status);
    }
}
