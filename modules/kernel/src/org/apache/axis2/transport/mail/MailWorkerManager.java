/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
 */
package org.apache.axis2.transport.mail;

import edu.emory.mathcs.backport.java.util.concurrent.ExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingQueue;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
/*
 *
 */

public class MailWorkerManager {
    private LinkedBlockingQueue messageQueue;
    private ExecutorService workerPool;
    private int poolSize;
    private ConfigurationContext configurationContext;

    public MailWorkerManager() {
    }

    public MailWorkerManager(ConfigurationContext configurationContext,
                             LinkedBlockingQueue messageQueue, ExecutorService workerPool,
                             int poolSize) {
        this.messageQueue = messageQueue;
        this.workerPool = workerPool;
        this.poolSize = poolSize;
        this.configurationContext = configurationContext;
    }

    public void start() throws AxisFault {
        for (int i = 0; i < poolSize; i++) {
//            workerPool.execute(new MailWorker(configurationContext, messageQueue));
        }
    }
}
