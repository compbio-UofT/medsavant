/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.util;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.rmi.ConnectException;
import java.rmi.NoSuchObjectException;
import java.rmi.UnmarshalException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.shared.util.MiscUtils;

import com.jidesoft.list.FilterableCheckBoxList;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;

/**
 * @author Andrew
 */
public class ClientMiscUtils extends MiscUtils {
    private static final Log LOG = LogFactory.getLog(ClientMiscUtils.class);

    public static final String GENDER_MALE = "Male";

    public static final String GENDER_FEMALE = "Female";

    public static final String GENDER_UNKNOWN = "Undesignated";

    public static String genderToString(int gender) {
        switch (gender) {
            case 1:
                return GENDER_MALE;
            case 2:
                return GENDER_FEMALE;
            default:
                return GENDER_UNKNOWN;
        }
    }

    public static int stringToGender(String gender) {
        if (gender.equals(GENDER_MALE)) {
            return 1;
        } else if (gender.equals(GENDER_FEMALE)) {
            return 2;
        } else {
            return 0;
        }
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    public static double getDouble(Object o) {
        if (o instanceof Double) {
            return (Double) o;
        } else if (o instanceof Integer) {
            return ((Integer) o).doubleValue();
        } else if (o instanceof Long) {
            return ((Long) o).doubleValue();
        } else if (o instanceof Float) {
            return ((Float) o).doubleValue();
        } else {
            return -1;
        }
    }

    /**
     * Displays an error message to the user appropriately.
     * 
     * @param message human-readable message, may include a %s specification
     * @param t error being reported
     */
    public static void reportError(String message, Throwable t) {
        if (t instanceof ConnectException) {
            message = String.format(message, "server refused connection");
        } else if (t instanceof UnmarshalException) {
            message = String.format(message, "connection to server lost");
            MedSavantFrame.getInstance().forceRestart();
        } else if (t instanceof NoSuchObjectException) {
            message = String.format(message, "server has been restarted");
            MedSavantFrame.getInstance().forceRestart();
        } else {
            message = String.format(message, getMessage(t));
        }
        LOG.error(message, t);
        if (!checkSQLException(t)) {
            DialogUtils.displayException("MedSavant", message, t);
        }
    }

    public static boolean checkSQLException(Throwable t) {
        if ((t instanceof SQLException)
            && (t.getMessage().contains("Unknown column") || t.getMessage().contains("doesn't exist"))) {
            DialogUtils
                .displayErrorMessage(
                    "<html>It appears that the database structure has been modified.<br>Please log back in for the changes to take effect.</html>",
                    t);
            MedSavantFrame.getInstance().forceRestart();
            return true;
        }
        return false;
    }

    public static Map<Object, List<String>> modifyGenderMap(Map<Object, List<String>> original) {
        Map<Object, List<String>> result = new HashMap<Object, List<String>>();
        for (Object key : original.keySet()) {
            String s;
            if (key instanceof Long) {
                s = ClientMiscUtils.genderToString(ClientMiscUtils.safeLongToInt((Long) key));
            } else if (key instanceof Integer) {
                Long x = new Long(((Integer) (key)).longValue());
                s = ClientMiscUtils.genderToString(ClientMiscUtils.safeLongToInt(x));
            } else {
                s = ClientMiscUtils.GENDER_UNKNOWN;
            }
            if (result.get(s) == null) {
                result.put(s, original.get(key));
            } else {
                result.get(s).addAll(original.get(key));
            }
        }
        return result;
    }

    /**
     * Register the escape key so that it can be used to cancel the associated JDialog.
     */
    public static void registerCancelButton(final JButton cancelButton) {
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JDialog dialog = (JDialog) SwingUtilities.getWindowAncestor(cancelButton);
        dialog.getRootPane().registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                cancelButton.doClick();
            }
        }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /**
     * Set the title of a window to reflect whether it is saved or not. On Windows and Linux, this appends an asterisk
     * to the title of an unsaved document; on Mac, it puts a dot inside the close-button.
     * 
     * @param f
     * @param unsaved
     */
    public static void setUnsavedTitle(JFrame f, String title, boolean unsaved) {
        f.getRootPane().putClientProperty("Window.documentModified", unsaved);
        if (!MAC && unsaved) {
            f.setTitle(title + UNSAVED_MARK);
        } else {
            f.setTitle(title);
        }
    }

    /**
     * Draws a string centred in the given box.
     */
    public static void drawCentred(Graphics2D g2, String message, Rectangle2D box) {
        FontMetrics metrics = g2.getFontMetrics();
        Rectangle2D stringBounds = g2.getFont().getStringBounds(message, g2.getFontRenderContext());
        float x = (float) (box.getX() + (box.getWidth() - stringBounds.getWidth()) / 2.0);
        float y = (float) (box.getY() + (box.getHeight() + metrics.getAscent() - metrics.getDescent()) / 2.0);

        g2.drawString(message, x, y);
    }

    /**
     * Creates html representation of a given string that includes line breaks
     */
    public static String breakString(String remainingStr, String soFar, int maxLineLength) {
        if (remainingStr.length() < maxLineLength) {
            return "<html>" + soFar + remainingStr + "</html>";
        }
        int endIndex;
        if (maxLineLength > remainingStr.length()) {
            endIndex = remainingStr.length();
        } else {
            endIndex = maxLineLength;
        }
        int indexOfBreak = remainingStr.substring(0, endIndex).lastIndexOf(" ");

        if (indexOfBreak == -1) {
            return breakString(remainingStr.substring(maxLineLength), soFar +=
                remainingStr.substring(0, maxLineLength) + "<br>-", maxLineLength);
        } else {
            soFar += remainingStr.substring(0, indexOfBreak) + "<br>";
            return breakString(remainingStr.substring(indexOfBreak), soFar, maxLineLength);
        }
    }

    /**
     * Rounds a double up safely
     */
    public static double round(double unrounded, int precision) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, BigDecimal.ROUND_HALF_UP);
        return rounded.doubleValue();
    }

    public static void selectOnlyTheseIndicies(FilterableCheckBoxList filterableList, int[] selectedIndices) {
        for (int k = 0; k < filterableList.getModel().getSize(); k++) {
            filterableList.removeCheckBoxListSelectedIndex(k);
        }
        for (int j : selectedIndices) {
            if (j == -1) {
                continue;
            }
            filterableList.addCheckBoxListSelectedIndex(j);
        }
    }
}
