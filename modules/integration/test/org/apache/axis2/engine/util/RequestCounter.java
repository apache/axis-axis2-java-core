package org.apache.axis2.engine.util;

import org.apache.axis2.context.MessageContext;

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
 */

public class RequestCounter {

    public static final String REQUEST_COUNT = "Request_Count";

    public void getRequestCount(MessageContext inMessageContext, MessageContext outMessageContext) {
        Integer requestCount = (Integer) inMessageContext.getServiceGroupContext().getProperty(REQUEST_COUNT);
        if (requestCount == null) {
            requestCount = new Integer(1);
        } else {
            requestCount = new Integer(requestCount.intValue() + 1);
        }

        inMessageContext.getServiceGroupContext().setProperty(REQUEST_COUNT, requestCount);
    }

}
