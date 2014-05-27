/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
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
