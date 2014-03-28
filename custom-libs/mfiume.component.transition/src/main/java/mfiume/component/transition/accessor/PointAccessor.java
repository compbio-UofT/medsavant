/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mfiume.component.transition.accessor;

import aurelienribon.tweenengine.TweenAccessor;
import java.awt.Point;

/**
 *
 * @author mfiume
 */
public class PointAccessor implements TweenAccessor<Point> {

    public static final int TWEET_TYPE_BASIC = 0;

    @Override
    public int getValues(Point t, int tweenType, float[] returnValues) {
        returnValues[0] = (int) t.x;
        returnValues[1] = (int) t.y;
        return 2;
    }

    @Override
    public void setValues(Point t, int tweenType, float[] newValues) {
        t.setLocation(new Point(Math.round(newValues[0]),Math.round(newValues[1])));
    }

}
