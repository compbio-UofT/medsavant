package makeposterbg;

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
