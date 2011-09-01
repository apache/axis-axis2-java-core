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

package org.apache.axis2.engine;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.integration.LocalTestCase;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

public class EnumTest extends LocalTestCase{
    private static final Log log = LogFactory.getLog(EnumTest.class);
    @Override
    public void setUp() throws Exception {
        super.setUp();
        serverConfig.addMessageReceiver(WSDL2Constants.MEP_URI_IN_ONLY,
                new RPCInOnlyMessageReceiver());
        serverConfig.addMessageReceiver(WSDL2Constants.MEP_URI_IN_OUT,
                new RPCMessageReceiver());
        deployClassAsService("EnumService", EnumService.class);
    }


    public void testTestDay()throws Exception {

        QName opTestDay = new QName("http://engine.axis2.apache.org", "testDay" , "asix");
//        QName paraDay = new QName("http://engine.axis2.apache.org", "day" , "asix");

        EnumService.Day aDay = EnumService.Day.MONDAY;

        Object[] opDayArg = new Object[]{aDay};
        RPCServiceClient serviceClient = getRPCClient();
        Options options = serviceClient.getOptions();
        EndpointReference targetEPR = new EndpointReference(
                "local://services/EnumService");
        options.setTo(targetEPR);
        options.setAction("testDay");

//        Object[] ret = serviceClient.invokeBlocking(
//                opTestDay, opDayArg, paraDay ,  new Class[]{String.class});
        Object[]  ret = serviceClient.invokeBlocking(opTestDay ,opDayArg , new Class[]{String.class});
        log.debug(ret[0].toString());
        assertEquals(ret[0], "MONDAY");

    }


    public void testEnumPojo()throws Exception{
        QName opEnumPojo = new QName("http://engine.axis2.apache.org", "enumPojo" , "asix");

        Event event = new Event();

        event.setEventDesc("Event Description");
        event.setEventName("Event Name");
        event.setStartingDay(EnumService.Day.FRIDAY);

        // Constructing the arguments array for the method invocation
        Object[] opAddEventArgs = new Object[] { event};

        // Invoking the method
        RPCServiceClient serviceClient = getRPCClient();
        Options options = serviceClient.getOptions();
        EndpointReference targetEPR = new EndpointReference(
                "local://services/EnumService");
        options.setTo(targetEPR);
        options.setAction("enumPojo");
        Class[] returnTypes = new Class[] { Event.class };

        Object[] ret = serviceClient.invokeBlocking(opEnumPojo, opAddEventArgs, returnTypes);
//                assertEquals(event, ret[0]);
        Event res = (Event)ret[0];
        assertEquals(event.getEventDesc() , res.getEventDesc());
        assertEquals(event.getEventName(), res.getEventName());
        assertEquals(event.getStartingDay() , res.getStartingDay());

    }
}
