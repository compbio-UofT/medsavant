/*
 *    Copyright 2010-2011 University of Toronto
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
package org.ut.biolab.medsavant.client.util;

import org.ut.biolab.medsavant.client.api.Listener;


/**
 * Basically just a Listener class, but also allows for the possibility that the
 * listener wants to cancel the download process.
 *
 * @author tarkvara
 */
public interface DownloadMonitor extends Listener<DownloadEvent> {
    public boolean isCancelled();
}
