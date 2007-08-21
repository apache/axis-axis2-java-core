package org.apache.axis2.rmi.databind;

import org.apache.axis2.rmi.metadata.Type;
import org.apache.axis2.rmi.util.NamespacePrefix;
import org.apache.axis2.rmi.exception.XmlSerializingException;
import org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;

/**
 * this interface is used to customize the
 * the default way axis2 rmi works
 */
public interface RMIBean {

    /**
     * serialize the class accordingly.
     * this method supposed to serialize this object to the
     * writer. ParentQname is passed so that this method should wirte
     * the start and end elements for the bean as well.
     * @param writer
     * @param parentQName
     */
    public void serialize(XMLStreamWriter writer,
                         JavaObjectSerializer serializer,
                         QName parentQName,
                         NamespacePrefix namespacePrefix)
            throws XMLStreamException, XmlSerializingException;

}
