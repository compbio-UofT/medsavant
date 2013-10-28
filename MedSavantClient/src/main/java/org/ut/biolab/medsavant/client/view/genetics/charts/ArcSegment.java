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
package org.ut.biolab.medsavant.client.view.genetics.charts;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;


public class ArcSegment {
    public ArcSegment(){

    }

    public Shape Create(
            double centerx,
            double centery,
            double radius,
            double width,
            double start,
            double extent
        )
    {
        GeneralPath gp = new GeneralPath();
        GeneralPath dummy = new GeneralPath(); // used to find arc endpoints

        double left, top;
        left = centerx - radius;
        top = centery - radius;

        Shape outer = new Arc2D.Double(left, top, 2 * radius, 2 * radius, start, extent, Arc2D.OPEN);
        Shape inner = new Arc2D.Double(left + width, top + width, 2 * radius - 2 * width
                        , 2 * radius - 2 * width, start+extent, -extent, Arc2D.OPEN);
        gp.append(outer, false);

        dummy.append(new Arc2D.Double(left + width, top + width, 2 * radius - 2 * width
                , 2 * radius - 2 * width, start, extent, Arc2D.OPEN),false);

        Point2D point = dummy.getCurrentPoint();

        if(point!=null)gp.lineTo(point.getX(), point.getY());
        gp.append(inner, false);

        dummy.append(new Arc2D.Double(left, top, 2 * radius, 2 * radius, start+extent, -extent, Arc2D.OPEN),false);

        point = dummy.getCurrentPoint();
        gp.lineTo(point.getX(), point.getY());
        return gp;
    }
}
