package org.apache.axis2.json;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Echo {
    private static final Log log = LogFactory.getLog(Echo.class);

    public Echo() {
    }

    public OMElement echoOM(OMElement omEle) throws AxisFault {
        MessageContext outMsgCtx = MessageContext.getCurrentMessageContext().getOperationContext().getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
        Object object = outMsgCtx.getProperty(Constants.Configuration.MESSAGE_TYPE);
        String messageType = (String) object;

        //if the request is through GET, the message type is application/xml. otherwise don't allow
        //any non json specific message types
        if (messageType.equalsIgnoreCase(HTTPConstants.MEDIA_TYPE_APPLICATION_XML)) {
            outMsgCtx.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/json");
        } else if (messageType.indexOf("json") < 0) {
            throw new AxisFault("Type of the Received Message is not JSON");
        }
        return omEle;
    }
}
