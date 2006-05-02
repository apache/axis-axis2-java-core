package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.MessageContextConstants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.i18n.Messages;

import javax.xml.namespace.QName;

/**
 * Utility methods for various clients to use.
 */
public class ClientUtils {

    public static synchronized TransportOutDescription inferOutTransport(AxisConfiguration ac,
                                                                         EndpointReference epr,
                                                                         MessageContext msgctx) throws AxisFault {
        String transportURI = (String) msgctx.getProperty(MessageContextConstants.TRANSPORT_URL);
        if (transportURI != null && !"".equals(transportURI)) {
            int index = transportURI.indexOf(':');
            String transport = (index > 0) ? transportURI.substring(0, index) : null;
            if (transport != null) {
                return ac.getTransportOut(new QName(transport));
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
                return ac.getTransportOut(new QName(transport));
            } else {
                throw new AxisFault(Messages.getMessage("cannotInferTransport", uri));
            }
        }
    }

    public static synchronized TransportInDescription inferInTransport(AxisConfiguration ac,
                                                                       Options options,
                                                                       MessageContext msgCtxt) throws AxisFault {
        String listenerTransportProtocol = options.getTransportInProtocol();
        TransportInDescription transportIn = null;
        if (options.isUseSeparateListener()) {
            if ((listenerTransportProtocol != null) && !"".equals(listenerTransportProtocol)) {
                transportIn = ac.getTransportIn(new QName(listenerTransportProtocol));
                ListenerManager listenerManager =
                        msgCtxt.getConfigurationContext().getListenerManager();
                if (transportIn == null) {
                    // TODO : User should not be mandated to give an IN transport. If it is not given, we should
                    // ask from the ListenerManager to give any available transport for this client.
                    throw new AxisFault(Messages.getMessage("unknownTransport",
                            listenerTransportProtocol));
                }
                if (!listenerManager.isListenerRunning(transportIn.getName().getLocalPart())) {
                    listenerManager.addListener(transportIn, false);
                }
            }
            if (msgCtxt.getAxisService() != null) {
                if (!msgCtxt.isEngaged(new QName(Constants.MODULE_ADDRESSING))) {
                    throw new AxisFault(Messages.getMessage("2channelNeedAddressing"));
                }
            } else {
                if (!ac.isEngaged(new QName(Constants.MODULE_ADDRESSING))) {
                    throw new AxisFault(Messages.getMessage("2channelNeedAddressing"));
                }
            }
        }
        return transportIn;
    }
}
