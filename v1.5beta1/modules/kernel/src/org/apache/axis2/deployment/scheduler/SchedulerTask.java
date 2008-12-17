/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.axis2.deployment.scheduler;

import org.apache.axis2.deployment.RepositoryListener;

import java.util.TimerTask;

public class SchedulerTask implements Runnable {
    static final int SCHEDULED = 1;
    static final int CANCELLED = 2;
    final Object lock = new Object();
    int state = 0;
    TimerTask timerTask;
    private RepositoryListener wsListener;

    /**
     * Creates a new scheduler task.
     */
    public SchedulerTask(RepositoryListener listener) {
        this.wsListener = listener;
    }

    /**
     * Cancels this scheduler task.
     * <p/>
     * This method may be called repeatedly; the second and subsequent calls have no effect.
     *
     * @return Returns true if this task was already scheduled to run.
     */
    public boolean cancel() {
        synchronized (lock) {
            if (timerTask != null) {
                timerTask.cancel();
            }

            boolean result = (state == SCHEDULED);

            state = CANCELLED;

            return result;
        }
    }

    private void checkRepository() {
        wsListener.startListener();
    }

    /**
     * The action to be performed by this scheduler task.
     */
    public void run() {
        checkRepository();
    }
}
