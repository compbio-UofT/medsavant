/**
 * Copyright (C) 2007 Google Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.hibernate.shards.criteria;

import org.hibernate.Criteria;
import org.hibernate.criterion.CountProjection;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;

/**
 * Event that allows a Count Projection to be lazily added to a Criteria. this
 * event supports both distinct and regular count projections.
 * 
 * @see Criteria#add(Criterion)
 * 
 * @author aviadl@sentrigo.com (Aviad Lichtenstadt)
 */
class CountProjectionEvent implements CriteriaEvent {

    // the Projection we're going to add when the event fires
    private final Projection proj;
    private String fieldName;
    private boolean distinct = false;

    public CountProjectionEvent(Projection proj) {
        if (proj instanceof CountProjection) {
            this.proj = proj;
            String projectionAsString = proj.toString();
            if (projectionAsString.startsWith("distinct")) {
                distinct = true;
                fieldName = projectionAsString.substring(projectionAsString.indexOf("(") + 1, projectionAsString.indexOf(")"));
            }
        } else {
            throw new RuntimeException("Event valid only for count projections");
        }
    }

    public void onEvent(Criteria crit) {
        if (distinct) {
            crit.setProjection(Projections.property(fieldName));
        } else {
            crit.setProjection(proj);
        }
    }
}