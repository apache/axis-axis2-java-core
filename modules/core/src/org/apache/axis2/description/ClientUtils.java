package org.apache.axis2.description;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.UUIDGenerator;

/**
 * Utility methods for various clients to use.
 */
class ClientUtils {
    static TransportOutDescription inferOutTransport(AxisConfiguration ac,
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

    /**
     * Copy data from options to the message context. We really should revisit
     * this and push options down into the message context directly.
     * 
     * @param options
     *            options to copy from
     * @param mc
     *            the message context to copy into
     */
    public static void copyInfoFromOptionsToMessageContext(Options options,
            MessageContext mc) {
        mc.setTo(options.getTo());
        mc.setFrom(options.getFrom());
        mc.setFaultTo(options.getFaultTo());
        mc.setReplyTo(options.getReplyTo());
        mc.setRelatesTo(options.getRelatesTo());
        mc.setMessageID(((options.getMessageId() == null) || "".equals(options
                .getMessageId())) ? ("uuid:" + UUIDGenerator.getUUID())
                : options.getMessageId());
        mc.setWSAAction(options.getAction());
        mc.setSoapAction(options.getSoapAction());
        mc.setProperty(Constants.Configuration.IS_USING_SEPARATE_LISTENER,
                Boolean.valueOf(options.isUseSeparateListener()));
    }

}
