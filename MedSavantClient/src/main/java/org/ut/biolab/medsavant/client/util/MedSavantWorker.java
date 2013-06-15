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

package org.ut.biolab.medsavant.client.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.shared.model.ProgressStatus;

/**
 * SwingWorker wrapper which provides hooks do the right thing in response to errors, cancellation, etc.
 * 
 * @author tarkvara
 */
public abstract class MedSavantWorker<T> extends SwingWorker<T, Object> {
    private static final Log LOG = LogFactory.getLog(MedSavantWorker.class);

    private String pageName;

    private Timer progressTimer;

    /**
     * @param pageName which view created this worker
     */
    public MedSavantWorker(String pageName) {
        this.pageName = pageName;
        ThreadController.getInstance().addWorker(pageName, this);
    }

    @Override
    public void done() {
        if (this.progressTimer != null) {
            this.progressTimer.stop();
        }
        showProgress(1.0);
        try {
            if (!isCancelled()) {
                showSuccess(get());
            } else {
                // Send the server one last checkProgress call so that server knows that we've cancelled.
                try {
                    checkProgress();
                } catch (Exception ex) {
                    LOG.info("Ignoring exception thrown while cancelling.", ex);
                }
                throw new InterruptedException();
            }
        } catch (InterruptedException x) {
            showFailure(x);
        } catch (ExecutionException x) {
            showFailure(x.getCause());
        } finally {
            // Succeed or fail, we want to remove the worker from our page.
            ThreadController.getInstance().removeWorker(this.pageName, this);
        }
    }

    /**
     * Show progress during a lengthy operation. As a special case, pass 1.0 to remove the progress display.
     * 
     * @param fract the fraction completed (1.0 to indicate full completion; -1.0 as special flag to indicate
     *        indeterminate progress-bar).
     */
    protected abstract void showProgress(double fract);

    /**
     * Called when the worker has successfully completed its task.
     * 
     * @param result the value returned by <code>doInBackground()</code>.
     */
    protected abstract void showSuccess(T result);

    /**
     * Called when the task has thrown an exception. Default behaviour is to log the exception and put up a dialog box.
     */
    protected void showFailure(Throwable t) {
        if (!(t instanceof InterruptedException)) {
            ClientMiscUtils.reportError("Exception thrown by background task: %s", t);
        }
    }

    /**
     * Base-class does no progress checking.
     */
    protected ProgressStatus checkProgress() throws Exception {
        return null;
    }

    /**
     * Workers which want intermittent progress checks should start a timer which will call checkProgress and
     * showProgress intermittently.
     */
    protected void startProgressTimer() {
        this.progressTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    ProgressStatus status = checkProgress();
                    if (status != null) {
                        showProgress(status.fractionCompleted);
                    }
                } catch (Exception ex) {
                    LOG.info("Ignoring exception thrown while checking for progress.", ex);
                }
            }
        });
        this.progressTimer.start();
    }
}
