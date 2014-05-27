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
import mfiume.component.transition.accessor.FloatAccessor.FloatStruct;

/**
 *
 * @author mfiume
 */
public class FloatAccessor implements TweenAccessor<FloatStruct> {
    
    public static class FloatStruct {
        private float f;
        
        public FloatStruct(float f) {
            this.f = f;
        }

        public float getFloat() {
            return f;
        }

        public void setFloat(float f) {
            this.f = f;
        }
        
        
    }
    
    public static final int TWEET_TYPE_BASIC = 0;

    @Override
    public int getValues(FloatStruct t, int tweenType, float[] returnValues) {
        returnValues[0] = t.getFloat();
        return 2;
    }

    @Override
    public void setValues(FloatStruct t, int tweenType, float[] newValues) {
        t.setFloat(newValues[0]);
    }

}