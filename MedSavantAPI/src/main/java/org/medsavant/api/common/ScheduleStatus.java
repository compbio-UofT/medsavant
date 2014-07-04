/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.common;

/**
 *
 * @author jim
 */
public enum ScheduleStatus {

        RUNNING_AS_LONGJOB {
                    @Override
                    public String toString() {
                        return "RUNNING_IN_QUEUE";
                    }
                },
        RUNNING_AS_SHORTJOB {
                    @Override
                    public String toString() {
                        return "RUNNING";
                    }
                },
        SCHEDULED_AS_SHORTJOB {
                    @Override
                    public String toString() {
                        return "ABOUT_TO_RUN";
                    }
                },
        SCHEDULED_AS_LONGJOB {
                    @Override
                    public String toString() {
                        return "QUEUED";
                    }
                },
        NOT_STARTED,
        FINISHED,
        CANCELLED
    };