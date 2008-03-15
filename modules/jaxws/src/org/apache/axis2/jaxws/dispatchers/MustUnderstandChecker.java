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

package org.apache.axis2.jaxws.dispatchers;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Plugin to remove "understood" headers for the JAXWS related headers.  This class must
 * be configured in the axis2.xml file on both the client and the server.
 * 
 *  Understood headers (per JAXWS 2.0 Section 10.2) include
 *  - Headers that correspond to SEI method parameters.
 */
public class MustUnderstandChecker extends org.apache.axis2.handlers.AbstractHandler {
    private static final Log log = LogFactory.getLog(MustUnderstandChecker.class);

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        // Get the list of headers for the roles we're acting in, then mark any we understand
        // as processed.
        MustUnderstandUtils.markUnderstoodHeaderParameters(msgContext);
        return InvocationResponse.CONTINUE;
    }
}
