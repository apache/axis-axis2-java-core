package org.apache.axis2.mtom;

import org.apache.axiom.attachments.IncomingAttachmentInputStream;
import org.apache.axiom.attachments.IncomingAttachmentStreams;
import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.attachments.utils.IOUtils;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.Base64;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.wsdl.WSDLConstants;

public class EchoService2 {

    OperationContext oprCtx;

    public void setOperationContext(OperationContext oprCtx) {
        this.oprCtx = oprCtx;
    }

    public OMElement mtomSample(OMElement element) throws Exception {

        Attachments attachments = null;

        if (this.oprCtx != null) {
            attachments = (Attachments) this.oprCtx.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE).getProperty(MTOMConstants.ATTACHMENTS);
        } else {
            throw new AxisFault("Message context not set/Attachments not set");
        }

        // Get image data
        IncomingAttachmentStreams streams = attachments.getIncomingAttachmentStreams();
        IncomingAttachmentInputStream stream = streams.getNextStream();

        byte[] data = IOUtils.getStreamAsByteArray(stream);

        //setting response
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace("urn://fakenamespace", "ns");
        OMElement response, elem;

        response = fac.createOMElement("response", ns);

        elem = fac.createOMElement("data", ns);
        elem.setText(Base64.encode(data));
        response.addChild(elem);

        return response;
    }
}
