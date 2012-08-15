/*
 *    Copyright 2012 University of Toronto
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

package org.ut.biolab.medsavant.model;

import java.io.Serializable;


/**
 * Represents the status of a length process.
 *
 * @author tarkvara
 */
public class ProgressStatus implements Serializable {
    
    public static final ProgressStatus CANCELLED = new ProgressStatus("Cancelled", 1.0);

    /**
     * A progress message indicating the operation currently in process (e.g.&nbsp;"Loading…").
     */
    public String message;
    
    /**
     * A value from 0.0–1.0, indicating the amount of progress completed.
     */
    public double fractionCompleted;
    
    public ProgressStatus(String msg, double frac) {
        message = msg;
        fractionCompleted = frac;
    }
    
    @Override
    public String toString() {
        return String.format("%s %.1f%%", message, fractionCompleted * 100.0);
    }
}
