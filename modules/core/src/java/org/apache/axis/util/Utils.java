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
package org.apache.axis.util;

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.axis.Constants;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.AxisTransport;
import org.apache.axis.description.Flow;
import org.apache.axis.description.HandlerMetadata;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.Handler;
import org.apache.axis.engine.Phase;
import org.apache.axis.transport.TransportReceiver;
import org.apache.axis.transport.TransportSender;

public class Utils {

    public static void addHandler(Flow flow, Handler handler) {
        HandlerMetadata hmd = new HandlerMetadata();
        hmd.setHandler(handler);
        flow.addHandler(hmd);
    }
    public static void addPhasesToServiceFromFlow(
        AxisService service,
        String phaseName,
        Flow flow,
        int flowtype)
        throws AxisFault {
        ArrayList faultchain = new ArrayList();
        Phase p = new Phase(Constants.PHASE_SERVICE);
        faultchain.add(p);
        addHandlers(flow, p);
        service.setPhases(faultchain, flowtype);
    }
    public static void createExecutionChains(AxisService service) throws AxisFault {
        addPhasesToServiceFromFlow(
            service,
            Constants.PHASE_SERVICE,
            service.getInFlow(),
            EngineRegistry.INFLOW);
        addPhasesToServiceFromFlow(
            service,
            Constants.PHASE_SERVICE,
            service.getOutFlow(),
            EngineRegistry.OUTFLOW);
        addPhasesToServiceFromFlow(
            service,
            Constants.PHASE_SERVICE,
            service.getFaultFlow(),
            EngineRegistry.FAULTFLOW);
    }
    public static void addHandlers(Flow flow, Phase phase) throws AxisFault {
        if (flow != null) {
            int handlerCount = flow.getHandlerCount();
            for (int i = 0; i < handlerCount; i++) {
                phase.addHandler(flow.getHandler(i).getHandler());
            }
        }
    }

    public static AxisTransport createHTTPTransport(EngineRegistry er) throws AxisFault {
        try {
            QName transportName = new QName("http");
            //AxisTransport httpTransport = er.getTransport(transportName);
            AxisTransport httpTransport = null;
            if (httpTransport == null) {
                httpTransport = new AxisTransport(transportName);
                Class className =
                    Class.forName("org.apache.axis.transport.http.HTTPTransportSender");
                httpTransport.setSender((TransportSender) className.newInstance());

                className = Class.forName("org.apache.axis.transport.http.HTTPTransportReceiver");
                httpTransport.setReciever((TransportReceiver) className.newInstance());
            }
            return httpTransport;
        } catch (ClassNotFoundException e) {
            throw new AxisFault(e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new AxisFault(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new AxisFault(e.getMessage(), e);
        }
    }
    public static AxisTransport createMailTransport(EngineRegistry er) throws AxisFault {
        try {
            QName transportName = new QName("http");

            AxisTransport mailTransport = null;
            if (mailTransport == null) {
                mailTransport = new AxisTransport(new QName("http"));
                Class className =
                    Class.forName("org.apache.axis.transport.mail.MailTransportSender");
                mailTransport.setSender((TransportSender) className.newInstance());
            }
            return mailTransport;
        } catch (ClassNotFoundException e) {
            throw new AxisFault(e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new AxisFault(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new AxisFault(e.getMessage(), e);
        }
    }

}
