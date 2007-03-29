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

package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.i18n.Messages;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility methods for various clients to use.
 */
public class ClientUtils {

    public static synchronized TransportOutDescription inferOutTransport(AxisConfiguration ac,
                                                                         EndpointReference epr,
                                                                         MessageContext msgctx)
            throws AxisFault {
        String transportURI = (String) msgctx.getProperty(Constants.Configuration.TRANSPORT_URL);
        if (transportURI != null && !"".equals(transportURI)) {
            int index = transportURI.indexOf(':');
            String transport = (index > 0) ? transportURI.substring(0, index) : null;
            if (transport != null) {
                TransportOutDescription transportOut = ac.getTransportOut(transport);
                if (transportOut == null) {
                    throw new AxisFault("No Tranport Sender found for : " + transport);
                } else {
                    return ac.getTransportOut(transport);
                }
            } else {
                throw new AxisFault(Messages.getMessage("cannotInferTransport", transportURI));
            }
        } else {
            if (msgctx.getOptions().getTransportOut() != null) {
                if (msgctx.getOptions().getTransportOut().getSender() == null) {
                    throw new AxisFault("Incomplete transport sender: missing sender!");
                }
                return msgctx.getOptions().getTransportOut();
            }
            if (epr == null || (epr.getAddress() == null)) {
                throw new AxisFault(Messages.getMessage("cannotInferTransportNoAddr"));
            }
            String uri = epr.getAddress();
            int index = uri.indexOf(':');
            String transport = (index > 0) ? uri.substring(0, index) : null;
            if (transport != null) {
                return ac.getTransportOut(transport);
            } else {
                throw new AxisFault(Messages.getMessage("cannotInferTransport", uri));
            }
        }
    }

    public static synchronized TransportInDescription inferInTransport(AxisConfiguration ac,
                                                                       Options options,
                                                                       MessageContext msgCtxt)
            throws AxisFault {
        String listenerTransportProtocol = options.getTransportInProtocol();
        if (listenerTransportProtocol == null) {
            EndpointReference replyTo = msgCtxt.getReplyTo();
            if (replyTo != null) {
                try {
                    URI uri = new URI(replyTo.getAddress());
                    listenerTransportProtocol = uri.getScheme();
                } catch (URISyntaxException e) {
                    //need to ignore
                }
            } else {
                //assume listener transport as sender transport
                if (msgCtxt.getTransportOut() != null) {
                    listenerTransportProtocol = msgCtxt.getTransportOut().getName();
                }
            }
        }
        TransportInDescription transportIn = null;
        if (options.isUseSeparateListener()) {
            if ((listenerTransportProtocol != null) && !"".equals(listenerTransportProtocol)) {
                transportIn = ac.getTransportIn(listenerTransportProtocol);
                ListenerManager listenerManager =
                        msgCtxt.getConfigurationContext().getListenerManager();
                if (transportIn == null) {
                    // TODO : User should not be mandated to give an IN transport. If it is not given, we should
                    // ask from the ListenerManager to give any available transport for this client.
                    throw new AxisFault(Messages.getMessage("unknownTransport",
                                                            listenerTransportProtocol));
                }
                if (!listenerManager.isListenerRunning(transportIn.getName())) {
                    listenerManager.addListener(transportIn, false);
                }
            }
            if (msgCtxt.getAxisService() != null) {
                if (!msgCtxt.isEngaged(Constants.MODULE_ADDRESSING)) {
                    throw new AxisFault(Messages.getMessage("2channelNeedAddressing"));
                }
            } else {
                if (!ac.isEngaged(Constants.MODULE_ADDRESSING)) {
                    throw new AxisFault(Messages.getMessage("2channelNeedAddressing"));
                }
            }
        }

        return transportIn;
    }
}
