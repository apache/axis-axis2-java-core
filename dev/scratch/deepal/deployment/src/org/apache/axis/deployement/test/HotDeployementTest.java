package org.apache.axis.deployement.test;

import org.apache.axis.deployement.Scheduler.Scheduler;
import org.apache.axis.deployement.Scheduler.SchedulerTask;
import org.apache.axis.deployement.Scheduler.DeploymentIterator;

import java.text.SimpleDateFormat;
import java.util.Date;

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
 *         9:43:39 AM
 *
 */
public class HotDeployementTest {
    private final Scheduler scheduler = new Scheduler();
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");
    private final int hourOfDay, minute, second;

    public HotDeployementTest(int hourOfDay, int minute, int second) {
        this.hourOfDay = hourOfDay;
        this.minute = minute;
        this.second = second;
    }

    public void start() {
        scheduler.schedule(new SchedulerTask(),new DeploymentIterator(hourOfDay, minute, second));
    }

    public static void main(String[] args) {
        HotDeployementTest alarmClock = new HotDeployementTest(7, 0, 0);
        alarmClock.start();
    }
}


