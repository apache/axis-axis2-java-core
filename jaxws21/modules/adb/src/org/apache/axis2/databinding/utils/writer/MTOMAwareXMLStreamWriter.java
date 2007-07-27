package org.apache.axis2.databinding.utils.writer;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.activation.DataHandler;

public interface MTOMAwareXMLStreamWriter extends XMLStreamWriter {
    public void writeDataHandler(DataHandler dataHandler) throws XMLStreamException;
}
