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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.RepositoryListener;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;

import java.util.TimerTask;

public class SchedulerTask implements Runnable {
    static final int SCHEDULED = 1;
    static final int CANCELLED = 2;
    final Object lock = new Object();
    int state = 0;
    TimerTask timerTask;
    private RepositoryListener wsListener;
    private AxisConfiguration axisConfig;
    private static final Parameter DEPLOYMENT_TASK_STATUS_PARAM =
            new Parameter(DeploymentEngine.DEPLOYMENT_TASK_RUNNING, Boolean.FALSE);

    /**
     * Creates a new scheduler task.
     */
    public SchedulerTask(RepositoryListener listener, AxisConfiguration axisConfig) {
        this.wsListener = listener;
        this.axisConfig = axisConfig;
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
        synchronized (axisConfig) {
            Parameter param =
                    axisConfig.getParameter(DeploymentEngine.DEPLOYMENT_TASK_RUNNING);
            if (param == null) {
                try {
                    axisConfig.addParameter(DEPLOYMENT_TASK_STATUS_PARAM);
                } catch (AxisFault e) {
                    // this is thrown only if the parameter is locked. Since we are sure that this
                    // param will not be locked, we will ignore this
                }
            }

            try {
                DEPLOYMENT_TASK_STATUS_PARAM.setValue(Boolean.TRUE);
                checkRepository();
            } finally {
                DEPLOYMENT_TASK_STATUS_PARAM.setValue(Boolean.FALSE);
            }
        }
    }
}
