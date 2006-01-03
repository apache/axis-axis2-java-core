package org.apache.axis2.saaj;

import junit.framework.TestCase;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.soap.*;
import javax.xml.soap.MimeHeaders;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.apache.axis2.om.impl.dom.DocumentImpl;

public class AttachmentSerializationTest extends TestCase {

    public AttachmentSerializationTest(String name) {
        super(name);
    }

    public static void main(String args[]) throws Exception {
        AttachmentSerializationTest tester = new AttachmentSerializationTest("tester");
        tester.testAttachments();
    }

    public void testAttachments() throws Exception {
        try {
            ByteArrayOutputStream bais = new ByteArrayOutputStream();
            int count = saveMsgWithAttachments(bais);
            assertEquals(count, 2);
        } catch (Exception e) {
//            throw new Exception("Fault returned from test: " + e);
            e.printStackTrace();
            fail("Unexpected Exception : " + e);
        }
    }

    public static final String MIME_MULTIPART_RELATED = "multipart/related";
    public static final String MIME_APPLICATION_DIME = "application/dime";
    public static final String NS_PREFIX = "jaxmtst";
    public static final String NS_URI = "http://www.jcommerce.net/soap/jaxm/TestJaxm";

    public int saveMsgWithAttachments(OutputStream os) throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage msg = mf.createMessage();

        SOAPPart soapPart = msg.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPHeader header = envelope.getHeader();
        SOAPBody body = envelope.getBody();

        SOAPElement el = header.addHeaderElement(envelope.createName("field4", NS_PREFIX, NS_URI));

        SOAPElement el2 = el.addChildElement("field4b", NS_PREFIX);
        SOAPElement el3 = el2.addTextNode("field4value");

        el = body.addBodyElement(envelope.createName("bodyfield3", NS_PREFIX, NS_URI));
        el2 = el.addChildElement("bodyfield3a", NS_PREFIX);
        el2.addTextNode("bodyvalue3a");
        el2 = el.addChildElement("bodyfield3b", NS_PREFIX);
        el2.addTextNode("bodyvalue3b");
        el2 = el.addChildElement("datefield", NS_PREFIX);

        // First Attachment
        AttachmentPart ap = msg.createAttachmentPart();
        final String testText = "some attachment text...";
        ap.setContent(testText, "text/plain");
        msg.addAttachmentPart(ap);

        // Second attachment
        String jpgfilename = "./test-resources/axis.jpg";
        File myfile = new File(jpgfilename);
        FileDataSource fds = new FileDataSource(myfile);
        DataHandler dh = new DataHandler(fds);
        AttachmentPart ap2 = msg.createAttachmentPart(dh);
        ap2.setContentType("image/jpg");
        msg.addAttachmentPart(ap2);

        MimeHeaders headers = msg.getMimeHeaders();
        assertTrue(headers != null);
        String [] contentType = headers.getHeader("Content-Type");
        assertTrue(contentType != null);

        for (Iterator iter = msg.getAttachments(); iter.hasNext();) {
            AttachmentPart attachmentPart =  (AttachmentPart) iter.next();
            final Object content = attachmentPart.getDataHandler().getContent();
            if(content instanceof String){
                assertEquals(testText, (String) content);
            } else if(content instanceof FileInputStream){
                final FileInputStream fis = (FileInputStream) content;
                /*File file = new File("output-file.jpg");
                file.createNewFile();

                fis.read(new byte[(int)file.length()])*/
            }
        }

        msg.writeTo(os);
        os.flush();
        msg.writeTo(System.out);
        return msg.countAttachments();
    }

    public int loadMsgWithAttachments(InputStream is) throws Exception {
        MimeHeaders headers = new MimeHeaders();
        headers.setHeader("Content-Type", MIME_MULTIPART_RELATED);
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage msg = mf.createMessage(headers, is);
        SOAPPart sp = msg.getSOAPPart();
        SOAPEnvelope envelope = sp.getEnvelope();
        assertTrue(sp != null);
        assertTrue(envelope != null);
        return msg.countAttachments();
    }
}
