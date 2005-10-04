package org.apache.axis2.om.impl;

import junit.framework.TestCase;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.StringWriter;

public class OMBlankElementTest extends TestCase {

    public OMBlankElementTest(String name) {
        super(name);
    }

    public void testBlankOMElem() throws XMLStreamException {
        try {
            //We should not get anything as the return value here: the output of the serialization
            String value = buildBlankOMElem();
            assertNull(
                "There's a serialized output for a blank XML element that cannot exist",
                value);
        } catch (OMException e) {
        }
    }

    String buildBlankOMElem() throws XMLStreamException {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace namespace1 = factory.createOMNamespace("", "");
        OMElement elem1 = factory.createOMElement("", namespace1);

        StringWriter writer = new StringWriter();
        elem1.build();
        elem1.serialize(
            new OMOutputImpl(XMLOutputFactory.newInstance().createXMLStreamWriter(writer)));
        writer.flush();
        return writer.toString();
    }

    public void testOMElemWithWhiteSpace() throws XMLStreamException {
        try {
            //We should not get anything as the return value here: the output of the serialization
            String value = buildWithWhiteSpaceOMElem();
            assertNull(
                "There's a serialized output for a blank XML element that cannot exist",
                value);
        } catch (OMException e) {
        }
    }

    String buildWithWhiteSpaceOMElem() throws XMLStreamException {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace namespace1 = factory.createOMNamespace("  ", "");
        OMElement elem1 = factory.createOMElement("  ", namespace1);

        StringWriter writer = new StringWriter();
        elem1.build();
        elem1.serialize(
            new OMOutputImpl(XMLOutputFactory.newInstance().createXMLStreamWriter(writer)));
        writer.flush();
        return writer.toString();
    }
}
