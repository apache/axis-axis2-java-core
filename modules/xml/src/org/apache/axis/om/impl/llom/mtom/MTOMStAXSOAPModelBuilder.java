
package org.apache.axis.om.impl.llom.mtom;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePartDataSource;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.om.OMAttribute;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMNode;
import org.apache.axis.soap.SOAPFactory;
import org.apache.axis.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Thilina Gunarathne thilina@opensource.lk
 */
public class MTOMStAXSOAPModelBuilder extends StAXSOAPModelBuilder {
    private Log log = LogFactory.getLog(getClass());
    
    InputStream inStream;
    
    LinkedList mimeBodyPartsList;
    
    MimeMessage mimeMessage;
    
    int partIndex = 0;
    
    public MTOMStAXSOAPModelBuilder(SOAPFactory ombuilderFactory,
            InputStream inStream) throws XMLStreamException,
            FactoryConfigurationError, OMException {
                // Fix me SOON
        super(null, ombuilderFactory);
        this.inStream = inStream;
        this.parser = getParserFromMime();
    }
    
    public MTOMStAXSOAPModelBuilder(InputStream inStream)
    throws XMLStreamException, FactoryConfigurationError, IOException,
    MessagingException {
        // TODO Fix me SOON
        super(null);
        this.inStream = inStream;
        this.parser = getParserFromMime();
    }
    
    private XMLStreamReader getParserFromMime() throws OMException, XMLStreamException, FactoryConfigurationError
    {        
        mimeBodyPartsList = new LinkedList();
        Properties props = new Properties();
        javax.mail.Session session = javax.mail.Session.getInstance(props, null);
        try {
            mimeMessage = new MimeMessage(session, inStream);
            
            MimeBodyPart rootMimeBodyPart;
            try {
                rootMimeBodyPart = getRootMimeBodyPart();
                return XMLInputFactory.newInstance().createXMLStreamReader(
                        rootMimeBodyPart.getInputStream());
                
            } catch (IOException e1) {
                throw new OMException(e1.toString());
                
            } catch (MessagingException e) {
                throw new OMException("Unable to extract root MIME part "
                        + e.toString());
            }
        } catch (MessagingException e) {
            throw new OMException(
            "Message identified as MTOM optimised doesn't contain a valid MIME Stream");
        }
    }
    protected OMNode createOMElement() throws OMException {
        
        String elementName = parser.getLocalName();
        
        String namespaceURI = parser.getNamespaceURI();
        
        // create an OMBlob if the element is an <xop:Include>
        if (elementName.equalsIgnoreCase("Include")
                & namespaceURI
                .equalsIgnoreCase("http://www.w3.org/2004/08/xop/include")) {
            
            OMBlobImpl node;
            String contentID = null;
            String contentIDName = null;
            OMAttribute Attr;
            if (lastNode == null) {
                // Decide whether to ckeck the level >3 or not
                throw new OMException(
                "XOP:Include element is not supported here");
            }
            if (parser.getAttributeCount() > 0) {
                contentID = parser.getAttributeValue(0);
                contentID = contentID.trim();
                contentIDName = parser.getAttributeLocalName(0);
                if (contentIDName.equalsIgnoreCase("href")
                        & contentID.substring(0, 3).equalsIgnoreCase("cid")) {
                    contentID = contentID.substring(4);
                } else {
                    throw new OMException(
                    "contentID not Found in XOP:Include element");
                }
            } else {
                throw new OMException(
                "Href attribute not found in XOP:Include element");
            }
            
            if (lastNode.isComplete()) {
                node = new OMBlobImpl(contentID, lastNode.getParent(),this);
                lastNode.setNextSibling(node);
                node.setPreviousSibling(lastNode);
            } else {
                OMElement e = (OMElement) lastNode;
                node = new OMBlobImpl(contentID, (OMElement) lastNode, this);
                e.setFirstChild(node);
            }
            return node;
            
        } else {
            OMElement node;
            if (lastNode == null) {
                node = constructNode(null, elementName, true);
            } else if (lastNode.isComplete()) {
                node = constructNode(lastNode.getParent(), elementName, false);
                lastNode.setNextSibling(node);
                node.setPreviousSibling(lastNode);
            } else {
                OMElement e = (OMElement) lastNode;
                node = constructNode((OMElement) lastNode, elementName, false);
                e.setFirstChild(node);
            }
            
            // fill in the attributes
            processAttributes(node);
            
            return node;
        }
    }
    
    public DataHandler getDataHandler(String blobContentID) throws OMException {
        /*
         * First checks whether the part is already parsed by checking the parts
         * linked list. If it is not parsed yet then call the getnextPart() till
         * we find the required part.
         */
        MimeBodyPart mimeBodyPart;
        
        boolean attachmentFound = false;
        ListIterator partsIterator = mimeBodyPartsList.listIterator();
        try {
            while (partsIterator.hasNext()) {
                mimeBodyPart = (MimeBodyPart) partsIterator.next();
                if (blobContentID.equals(mimeBodyPart.getContentID())) {
                    attachmentFound = true;
                    DataHandler dh = mimeBodyPart.getDataHandler();
                    return dh;
                }
            }
            while (!attachmentFound) {
                mimeBodyPart = this.getNextMimeBodyPart();
                
                if (mimeBodyPart == null) {
                    break;
                }
                String partContentID = mimeBodyPart.getContentID();
                String delimitedBlobContentID = "<" + blobContentID + ">";
                if (delimitedBlobContentID.equals(partContentID)) {
                    attachmentFound = true;
                    DataHandler dh = mimeBodyPart.getDataHandler();
                    return dh;
                }
            }
            return null;
        } catch (MessagingException e) {
            throw new OMException("Invalid Mime Message "
                    + e.toString());
        }
    }
    private MimeBodyPart getMimeBodyPart() throws MessagingException {
        MimeBodyPart mimeBodyPart = null;
        
        DataHandler dh = mimeMessage.getDataHandler();
        MimeMultipart multiPart = new MimeMultipart((MimePartDataSource) dh.getDataSource());
        mimeBodyPart = (MimeBodyPart) multiPart.getBodyPart(partIndex);
        
        partIndex++;
        return mimeBodyPart;
    }
    private MimeBodyPart getRootMimeBodyPart() throws MessagingException {
        MimeBodyPart rootMimeBodyPart;
        if (mimeBodyPartsList.isEmpty()) {
            rootMimeBodyPart = getMimeBodyPart();
            mimeBodyPartsList.add(rootMimeBodyPart);
        } else {
            rootMimeBodyPart = (MimeBodyPart) mimeBodyPartsList.getFirst();
        }
        return rootMimeBodyPart;
    }
    
    private MimeBodyPart getNextMimeBodyPart() throws MessagingException {
        MimeBodyPart nextMimeBodyPart;
        nextMimeBodyPart = getMimeBodyPart();
        if (nextMimeBodyPart != null) {
            mimeBodyPartsList.add(nextMimeBodyPart);
            return nextMimeBodyPart;
        } else
            return null;
    }
}