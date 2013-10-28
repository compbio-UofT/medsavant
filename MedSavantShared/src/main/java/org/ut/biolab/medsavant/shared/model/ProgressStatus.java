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
package org.ut.biolab.medsavant.shared.model;

import java.io.Serializable;


/**
 * Represents the status of a lengthy process.
 *
 * @author tarkvara
 */
public class ProgressStatus implements Serializable {

    public static final ProgressStatus CANCELLED = new ProgressStatus("Cancelled by user", 1.0);

    /**
     * A progress message indicating the operation currently in process (e.g.&nbsp;"Loading…").
     */
    public String message;

    /**
     * A value from 0.0–1.0, indicating the amount of progress completed.
     */
    public double fractionCompleted;

    public ProgressStatus(String msg, double frac) {
        message = msg;
        fractionCompleted = frac;
    }

    @Override
    public String toString() {
        return String.format("%s %.1f%%", message, fractionCompleted * 100.0);
    }
}
