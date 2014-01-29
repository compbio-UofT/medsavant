/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.util;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

/**
 * @author mfiume
 */
public class MedSavantExceptionHandler {

    public static boolean handleSessionExpiredException(SessionExpiredException e) {
        MedSavantFrame.getInstance().dispose();
        int result = DialogUtils.askYesNo("Session Expired","Would you like to log in again?");
        if (result == DialogUtils.YES) {
            MedSavantFrame.getInstance().forceRestart();
        } else {
            MedSavantFrame.getInstance().forceClose();
        }
        return false;
    }
}
