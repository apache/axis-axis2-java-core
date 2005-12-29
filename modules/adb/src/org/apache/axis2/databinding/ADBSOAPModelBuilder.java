package org.apache.axis2.databinding;

import org.apache.axis2.om.OMException;
import org.apache.axis2.om.impl.OMNodeEx;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Builds a SOAPEnvelope around an ADB pull parser
 */
public class ADBSOAPModelBuilder extends StAXOMBuilder {
    private SOAPBody body = null;
    private SOAPEnvelope envelope = null;
    
    public ADBSOAPModelBuilder(XMLStreamReader parser, SOAPFactory factory) {
        super(factory, parser);

        document = factory.createSOAPMessage(this);
        envelope = factory.getDefaultEnvelope();
        document.addChild(envelope);
        body = envelope.getBody();

        envelope.setBuilder(this);
        envelope.getHeader().setBuilder(this);
        body.setBuilder(this);
        lastNode = body;

        ((OMNodeEx)body).setComplete(false);
    }

    long count = -1;
    public int next() throws OMException {
        count++;
        if(count == 0)
            return 0;
        
        int ret = super.next();
        try {
            // Peek to see if the parser has any more and set the done flag.
            if(!parser.hasNext()) {
                done = true;
                ((OMNodeEx)body).setComplete(true);
            }
        } catch (XMLStreamException e) {
            throw new OMException(e);
        }
        return ret;
    }

    public SOAPEnvelope getEnvelope() {
        return envelope;
    }
}
