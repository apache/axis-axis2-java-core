/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.mail;

import java.io.PrintStream;

import jakarta.mail.Session;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.transport.base.ParamUtils;
import org.apache.axis2.util.LogWriter;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.logging.Log;

public class MailUtils {
    private MailUtils() {}
    
    public static void setupLogging(Session session, Log log, ParameterInclude params) throws AxisFault {
        // Note that debugging might already be enabled by the mail.debug property and we should
        // take care to override it.
        if (log.isTraceEnabled()) {
            // This is the old behavior: just set debug to true
            session.setDebug(true);
        }
        if (ParamUtils.getOptionalParamBoolean(params, MailConstants.TRANSPORT_MAIL_DEBUG, false)) {
            // Redirect debug output to where it belongs, namely to the logs!
            session.setDebugOut(new PrintStream(new WriterOutputStream(new LogWriter(log)), true));
            // Only enable debug afterwards since the call to setDebug might already cause debug output
            session.setDebug(true);
        }
    }
}
