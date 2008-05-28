package org.apache.axis2.builder;

import org.apache.axiom.attachments.utils.IOUtils;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

public class DataSourceBuilder implements Builder {

    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext msgContext)
            throws AxisFault {
        msgContext.setDoingREST(true);
        OMNamespace ns = new OMNamespaceImpl("", "");
        OMFactory factory = OMAbstractFactory.getOMFactory();
        byte[] bytes;
        try {
            bytes = IOUtils.getStreamAsByteArray(inputStream);
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        }
        ByteArrayDataSourceEx ds = new ByteArrayDataSourceEx(bytes, contentType);
        return new OMSourcedElementImpl("dummy", ns, factory, ds);
    }

    public class ByteArrayDataSourceEx extends javax.mail.util.ByteArrayDataSource implements OMDataSource {
        private byte[] bytes;
    
        public ByteArrayDataSourceEx(byte[] bytes, String s) {
            super(bytes, s);
            this.bytes = bytes;
        }

        public void serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException {
            try {
                output.write(bytes);
            } catch (IOException e) {
                throw new XMLStreamException(e);
            }
        }
    
        public java.lang.String getContentType() {
            return super.getContentType();
        }

        public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {
            throw new UnsupportedOperationException("FIXME");
        }

        public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
            throw new UnsupportedOperationException("FIXME");
        }

        public XMLStreamReader getReader() throws XMLStreamException {
            throw new UnsupportedOperationException("FIXME");
        }
    }
    
}
