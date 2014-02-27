/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package org.ut.biolab.medsavant;

import java.awt.Cursor;
import javax.swing.*;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

public class CursorTest extends JFrame
{
  private CursorTest()
  {
  }

  private void ShowDialog()
  {
        final JLabel label = ViewUtil.getEmphasizedLabel("SIGN OUT", ViewUtil.getMedSavantBlueColor());
        
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        JOptionPane pane = new JOptionPane(label);
        pane.setOptions(new Object[] { "OK" } );

        JDialog dialog = pane.createDialog(this, "Test Dialog");
        dialog.setVisible(true);
  }

  public static void main(String[] args)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        CursorTest testFrame = new CursorTest();
        testFrame.setTitle("Test GUI");
        testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        testFrame.setSize(500, 300);
        testFrame.setVisible(true);
        testFrame.ShowDialog();
      }
    });
  }
}