package org.apache.axis2.databinding;

import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.om.impl.OMNodeEx;
import org.apache.axis2.om.OMException;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPBody;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

/**
 * Builds a SOAPEnvelope around an ADB pull parser
 */
public class ADBSOAPModelBuilder extends StAXOMBuilder {
    SOAPBody body = null;
    
    public ADBSOAPModelBuilder(XMLStreamReader parser, SOAPFactory factory) {
        super(factory, parser);
        document = factory.createSOAPMessage(this);
        SOAPEnvelope env = factory.getDefaultEnvelope();
        document.addChild(env);
        body = env.getBody();
        ((OMNodeEx)body).setComplete(false);
        body.setBuilder(this);
        lastNode = body;
    }

    public int next() throws OMException {
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
}
