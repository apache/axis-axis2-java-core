package org.apache.axis.deployment.scheduler;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Deepal Jayasinghe
 *         Oct 5, 2004
 *         9:48:42 AM
 *
 */
public class Scheduler {

    public class SchedulerTimerTask extends TimerTask {
        private SchedulerTask schedulerTask;
        private ScheduleIterator iterator;

        public SchedulerTimerTask(SchedulerTask schedulerTask,
                                  ScheduleIterator iterator) {
            this.schedulerTask = schedulerTask;
            this.iterator = iterator;
        }

        public void run() {
            schedulerTask.run();
            reschedule(schedulerTask, iterator);
        }
    }


    private final Timer timer = new Timer();

    public Scheduler() {
    }

    /**
     * Terminates this <code>scheduler</code>, discarding any currently scheduled tasks.
     * Does not interfere with a currently executing task (if it exists).
     * Once a scheduler has been terminated, its execution thread terminates gracefully,
     * and no more tasks may be scheduled on it.
     * Note that calling this method from within the run method of a scheduler task that
     * was invoked by this scheduler absolutely guarantees that the ongoing task execution is the last
     * task execution that will ever be performed by this scheduler.
     * This method may be called repeatedly; the second and subsequent calls have no effect.
     */

    public void cancel() {
        timer.cancel();
    }

    /**
     * Schedules the specified task for execution according to the specified schedule.
     * If times specified by the <code>ScheduleIterator</code> are in the past they are
     * scheduled for immediate execution.
     * @param schedulerTask task to be scheduled
     * @param iterator iterator that describes the schedule
     * @throws IllegalStateException if task was already scheduled or cancelled,
     * scheduler was cancelled, or scheduler thread terminated.
     */

    public void schedule(SchedulerTask schedulerTask,
                         ScheduleIterator iterator) {

        Date time = iterator.next();
        if (time == null) {
            schedulerTask.cancel();
        } else {
            synchronized (schedulerTask.lock) {
                if (schedulerTask.state != SchedulerTask.VIRGIN) {
                    throw new IllegalStateException("Task already scheduled " +
                            "or cancelled");
                }
                schedulerTask.state = SchedulerTask.SCHEDULED;
                schedulerTask.timerTask =
                        new SchedulerTimerTask(schedulerTask, iterator);
                timer.schedule(schedulerTask.timerTask, time);
            }
        }
    }

    private void reschedule(SchedulerTask schedulerTask,
                            ScheduleIterator iterator) {

        Date time = iterator.next();
        if (time == null) {
            schedulerTask.cancel();
        } else {
            synchronized (schedulerTask.lock) {
                if (schedulerTask.state != SchedulerTask.CANCELLED) {
                    schedulerTask.timerTask =
                            new SchedulerTimerTask(schedulerTask, iterator);
                    timer.schedule(schedulerTask.timerTask, time);
                }
            }
        }
    }
}
