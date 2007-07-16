/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.provider;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.util.SOAPMustUnderstandHeaderChecker;

import javax.xml.namespace.QName;

import java.util.HashMap;

/**
 * Plugin to remove "understood" headers for the SoapMessageMUProviderTests.  This class must
 * be configured in the axis2.xml file on both the client and the server. 
 */
public class SoapMessageMUProviderChecker implements SOAPMustUnderstandHeaderChecker {
    public HashMap removeUnderstoodHeaders(MessageContext msgContext, HashMap headers) {
        headers = testDriver(headers);
        return headers;
    }

    private HashMap testDriver(HashMap notUnderstoodHeaders) {
        String ns1 = "http://ws.apache.org/axis2";
        String clientLocalName = "muclientunderstood";
        String serverLocalName = "muserverunderstood";

        // Remove the two headers the test expects us to understand
        QName clientQN = new QName(ns1, clientLocalName);
        QName serverQN = new QName(ns1, serverLocalName);

        if (notUnderstoodHeaders.containsKey(clientQN)) {
            notUnderstoodHeaders.remove(clientQN);
        }
        if (notUnderstoodHeaders.containsKey(serverQN)) {
            notUnderstoodHeaders.remove(serverQN);

        }
        return notUnderstoodHeaders;
    }
}