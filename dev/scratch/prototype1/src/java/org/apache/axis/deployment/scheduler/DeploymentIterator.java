package org.apache.axis.deployment.scheduler;

import java.util.Date;
import java.util.Calendar;

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
 *         9:36:14 AM
 *
 */
public class DeploymentIterator implements ScheduleIterator {
    private Calendar calendar = Calendar.getInstance();

    public DeploymentIterator(){

    }

    public Date next() {
        // calendar.add(Calendar.MINUTE, 1);
        calendar.add(Calendar.SECOND, 10);
        return calendar.getTime();
    }

}
