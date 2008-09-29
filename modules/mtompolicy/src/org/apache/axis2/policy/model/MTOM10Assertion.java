package org.apache.axis2.policy.model;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.neethi.Constants;
import org.apache.neethi.PolicyComponent;

/** Assertion to pick up the QName <wsoma:OptimizedMimeSerialization xmlns:wsoma="http://schemas.xmlsoap.org/ws/2004/09/policy/optimizedmimeserialization"/> */
public class MTOM10Assertion extends MTOMAssertion {

    public final static String NS = "http://schemas.xmlsoap.org/ws/2004/09/policy/optimizedmimeserialization";

    public final static String MTOM_SERIALIZATION_CONFIG_LN = "OptimizedMimeSerialization";

    public final static String PREFIX = "wsoma";

    public QName getName() {
        return new QName(NS, MTOM_SERIALIZATION_CONFIG_LN);
    }

    public short getType() {
        return Constants.TYPE_ASSERTION;
    }

    public boolean equal(PolicyComponent policyComponent) {
        throw new UnsupportedOperationException("TODO");
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String prefix = writer.getPrefix(NS);

        if (prefix == null) {
            prefix = PREFIX;
            writer.setPrefix(PREFIX, NS);
        }

        writer.writeStartElement(PREFIX, MTOM_SERIALIZATION_CONFIG_LN, NS);

        if (optional)
            writer.writeAttribute("Optional", "true");

        writer.writeNamespace(PREFIX, NS);
        writer.writeEndElement();

    }

    public PolicyComponent normalize() {
        throw new UnsupportedOperationException("TODO");
    }

}
