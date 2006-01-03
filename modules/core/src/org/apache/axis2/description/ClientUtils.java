package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ListenerManager;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.UUIDGenerator;

import javax.xml.namespace.QName;

/**
 * Utility methods for various clients to use.
 */
public class ClientUtils {
    public static TransportOutDescription inferOutTransport(AxisConfiguration ac,
                                                            EndpointReference epr) throws AxisFault {
        if (epr == null || (epr.getAddress() == null)) {
            throw new AxisFault(Messages.getMessage("cannotInferTransport"));
        }

        String uri = epr.getAddress();
        int index = uri.indexOf(':');
        String transport = (index > 0) ? uri.substring(0, index) : null;
        if (transport != null) {
            return ac.getTransportOut(new QName(transport));
        } else {
            throw new AxisFault(Messages.getMessage("cannotInferTransport"));
        }
    }

    public static TransportInDescription inferInTransport(AxisConfiguration ac,
                                                          Options options,
                                                          ServiceContext serviceContext) throws AxisFault {
        String listenerTransportProtocol = options.getTransportInProtocol();
        TransportInDescription transportIn = null;
        if (options.isUseSeparateListener()) {
            if ((listenerTransportProtocol != null) && !"".equals(listenerTransportProtocol)) {
                transportIn = ac.getTransportIn(new QName(listenerTransportProtocol));

                if (transportIn == null) {
                    throw new AxisFault(Messages.getMessage("unknownTransport",
                            listenerTransportProtocol));
                }
                options.setTransportInDescription(transportIn);
            }
            // if separate transport is used, start the required listeners
            if (!ac.isEngaged(new QName(Constants.MODULE_ADDRESSING))) {
                throw new AxisFault(Messages.getMessage("2channelNeedAddressing"));
            }
            ListenerManager.makeSureStarted(options.getTransportInProtocol(),
                    serviceContext.getConfigurationContext());
        }
        return transportIn;

    }
}
