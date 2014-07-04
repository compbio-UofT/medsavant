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

package org.ut.biolab.medsavant.shared.util;

import org.medsavant.api.common.MedSavantSession;

/**
 * A collection of utilities providing functionality required by the MedSavant Legacy component. 
 * As the legacy component is refactored/rewritten, the need for these utility methods will 
 * disappear.
 * @author jim
 */
public class MedSavantLegacyUtils {
    public static MedSavantSession getMedSavantSession(String sessionId){
        throw new UnsupportedOperationException("TODO: Implement MedSavantLegacyUtils.getMedSAvantSession");
    }
}
