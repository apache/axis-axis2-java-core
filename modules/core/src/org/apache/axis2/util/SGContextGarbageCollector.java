package org.apache.axis2.util;

import org.apache.axis2.context.ServiceGroupContext;

import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TimerTask;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author : Eran Chinthaka (chinthaka@apache.org)
 */

public class SGContextGarbageCollector extends TimerTask {

    private Hashtable serviceGroupContextMap;
    private long timeOutInterval;

    public SGContextGarbageCollector(Hashtable serviceGroupContextMap, long timeOutInterval) {
        this.serviceGroupContextMap = serviceGroupContextMap;
        this.timeOutInterval = timeOutInterval;
    }

    public void run() {
        long currentTime = new Date().getTime();
        Iterator sgCtxtMapKeyIter = serviceGroupContextMap.keySet().iterator();
        while (sgCtxtMapKeyIter.hasNext()) {
            String sgCtxtId = (String) sgCtxtMapKeyIter.next();
            ServiceGroupContext serviceGroupContext = (ServiceGroupContext) serviceGroupContextMap.get(sgCtxtId);
            if ((currentTime - serviceGroupContext.getLastTouchedTime()) > timeOutInterval) {

                serviceGroupContextMap.remove(sgCtxtId);
            }
        }
    }
}
