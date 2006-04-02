
package org.apache.axis2.security.trust;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.receivers.AbstractInOutAsyncMessageReceiver;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.soap.SOAPEnvelope;

public class STSMessageReceiver extends AbstractInOutAsyncMessageReceiver {

    public void invokeBusinessLogic(MessageContext inMessage,
            MessageContext outMessage) throws AxisFault {

        try {
            Parameter param = inMessage
                    .getParameter(TokenRequestDispatcherConfig.CONFIG_PARAM_KEY);
            Parameter paramFile = inMessage
                    .getParameter(TokenRequestDispatcherConfig.CONFIG_FILE_KEY);
            TokenRequestDispatcher dispatcher = null;
            if (param != null) {
                dispatcher = new TokenRequestDispatcher(param
                        .getParameterElement());
            } else if (paramFile != null) {
                dispatcher = new TokenRequestDispatcher((String) param
                        .getValue());
            } else {
                dispatcher = new TokenRequestDispatcher(
                        (OMElement) inMessage
                                .getProperty(TokenRequestDispatcherConfig.CONFIG_PARAM_KEY));
            }
            
            if(dispatcher != null) {
                SOAPEnvelope responseEnv = dispatcher.handle(inMessage);
                outMessage.setEnvelope(responseEnv);
            } else {
                throw new TrustException("missingDispatcherConfiguration");
            }
        } catch (TrustException e) {
            throw new AxisFault(e.getFaultString(), e.getFaultCode(), e);
        }
    }

}
