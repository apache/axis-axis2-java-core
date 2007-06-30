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


package org.apache.axis2.transport;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.Handler;

/**
 * TransportSender sends the SOAP Message to other SOAP nodes. A TransportSender is responsible for
 * writing the SOAP Message to the wire. Out flow must be terminated end with a TransportSender
 */
public interface TransportSender extends Handler {

    /**
     * Clean up
     *
     * @param msgContext
     * @throws org.apache.axis2.AxisFault
     */
    public void cleanup(MessageContext msgContext) throws AxisFault;

    /**
     * Initialize
     *
     * @param confContext
     * @param transportOut
     * @throws org.apache.axis2.AxisFault
     */
    public void init(ConfigurationContext confContext, TransportOutDescription transportOut)
            throws AxisFault;

    public void stop();
}
