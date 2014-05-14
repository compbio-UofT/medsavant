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
package org.ut.biolab.medsavant.client.view.app.builtin.task;

import java.util.Date;
import java.util.List;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.shared.model.GeneralLog;

/**
 *
 * @author mfiume
 */
public interface TaskWorker {

    String getTaskName();

    TaskStatus getCurrentStatus();

    List<GeneralLog> getLog();

    double getTaskProgress();

    void cancel();

    void addListener(Listener<TaskWorker> w);

    LaunchableApp getOwner();

    public enum TaskStatus {

        UNSTARTED {

                    @Override
                    public String toString() {
                        return "Not started";
                    }

                },
        INPROGRESS {

                    @Override
                    public String toString() {
                        return "Running";
                    }

                },
        FINISHED {

                    @Override
                    public String toString() {
                        return "Done";
                    }

                },
        CANCELLED {

                    @Override
                    public String toString() {
                        return "Cancelled";
                    }

                },
        ERROR {

                    @Override
                    public String toString() {
                        return "Error";
                    }

                },
        PERSISTENT {

                    @Override
                    public String toString() {
                        return "Persistent";
                    }
                },
        PERSISTENT_AUTOREFRESH {
                    @Override
                    public String toString() {
                        return "Persistent";
                    }
                }
    }
}
