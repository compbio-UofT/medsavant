/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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