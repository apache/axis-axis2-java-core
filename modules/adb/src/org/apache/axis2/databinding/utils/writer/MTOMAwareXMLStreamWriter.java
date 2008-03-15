package org.apache.axis2.databinding.utils.writer;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public interface MTOMAwareXMLStreamWriter extends XMLStreamWriter {
    public void writeDataHandler(DataHandler dataHandler) throws XMLStreamException;
}
