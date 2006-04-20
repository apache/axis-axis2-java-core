package org.apache.axis2.mtom;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;

import javax.activation.DataHandler;

import org.apache.axiom.attachments.IncomingAttachmentInputStream;
import org.apache.axiom.attachments.IncomingAttachmentStreams;
import org.apache.axiom.attachments.MIMEHelper;
import org.apache.axiom.attachments.utils.IOUtils;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
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

        MIMEHelper mimeHelper = null;

        if (this.oprCtx != null) {
            mimeHelper = (MIMEHelper) this.oprCtx.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE).getProperty(MTOMConstants.ATTACHMENTS);
        } else {
            throw new AxisFault("Message context not set/MIMEHelper not set");
        }

        // Get image data
        IncomingAttachmentStreams streams = mimeHelper.getIncomingAttachmentStreams();
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
