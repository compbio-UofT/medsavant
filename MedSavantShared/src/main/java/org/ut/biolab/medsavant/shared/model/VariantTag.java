/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.shared.model;

/**
 *
 * @author mfiume
 */
public class VariantTag {

    // A few default tag-keys.
    public static final String UPLOAD_DATE = "Upload Date";
    public static final String UPLOADER = "Uploader";

    public String key;
    public String value;

    public VariantTag(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return key + " = " + value;
    }

    /**
     * The inverse of <c>toString</c>.
     * @param stringVal
     */
    public static VariantTag fromString(String stringVal) {
        String[] vals = stringVal.split(" = ");
        return new VariantTag(vals[0], vals[1]);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
