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
import org.hibernate.criterion.AggregateProjection;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projection;

/**
 * Event that allows a Projection (valid only for max/min projection to be
 * lazily added to a Criteria.
 * 
 * @see Criteria#add(Criterion)
 * 
 * @author aviadl@sentrigo.com (Aviad Lichtenstadt)
 */
class AggregateProjectionEvent implements CriteriaEvent {

    // the Projection we're going to add when the event fires
    private final Projection proj;

    public AggregateProjectionEvent(Projection proj) {
        if (proj instanceof AggregateProjection) {
            this.proj = proj;
        } else {
            throw new RuntimeException("Event valid only for max/min projections");
        }
    }

    public void onEvent(Criteria crit) {
        crit.setProjection(proj);
    }
}