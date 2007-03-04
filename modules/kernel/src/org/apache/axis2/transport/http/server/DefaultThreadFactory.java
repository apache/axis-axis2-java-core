/*
* $HeadURL$
* $Revision$
* $Date$
*
* ====================================================================
*
*  Copyright 1999-2004 The Apache Software Foundation
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation.  For more
* information on the Apache Software Foundation, please see
* <http://www.apache.org/>.
*/

package org.apache.axis2.transport.http.server;

import edu.emory.mathcs.backport.java.util.concurrent.ThreadFactory;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;

public class DefaultThreadFactory implements ThreadFactory {

    final ThreadGroup group;
    final AtomicInteger count;
    final String namePrefix;

    public DefaultThreadFactory(final ThreadGroup group, final String namePrefix) {
        super();
        this.count = new AtomicInteger(1);
        this.group = group;
        this.namePrefix = namePrefix;
    }

    public Thread newThread(final Runnable runnable) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.namePrefix);
        buffer.append('-');
        buffer.append(this.count.getAndIncrement());
        Thread t = new Thread(group, runnable, buffer.toString(), 0);
        t.setDaemon(false);
        t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }

}
