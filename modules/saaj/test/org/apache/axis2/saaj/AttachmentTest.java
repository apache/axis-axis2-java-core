package org.apache.axis2.saaj;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPMessage;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

public class AttachmentTest extends TestCase {

    public AttachmentTest(String name) {
        super(name);
    }
    
    public void testStringAttachment() throws Exception {
    	SOAPConnectionFactory scFactory = SOAPConnectionFactory.newInstance();
    	SOAPConnection con = scFactory.createConnection();
    	
    	MessageFactory factory = MessageFactory.newInstance();
    	SOAPMessage message = factory.createMessage();
    	AttachmentPart attachment = message.createAttachmentPart();
    	String stringContent = "Update address for Sunny Skies " +
    			"Inc., to 10 Upbeat Street, Pleasant Grove, CA 95439";
    	
    	attachment.setContent(stringContent, "text/plain");
    	attachment.setContentId("update_address");
    	message.addAttachmentPart(attachment);
    	
    	assertTrue(message.countAttachments()==1);
    	
    	java.util.Iterator it = message.getAttachments();
    	while (it.hasNext()) {
    		attachment = (AttachmentPart) it.next();
    		Object content = attachment.getContent();
    		String id = attachment.getContentId();
    		System.out.println("Attachment " + id + " contains: " + content);
    		assertEquals(content,stringContent);
    	}
    	System.out.println("Here is what the XML message looks like:");
    	message.writeTo(System.out);
    	
    	message.removeAllAttachments();
    	assertTrue(message.countAttachments()==0);
    }
    
    public void testMultipleAttachments() throws Exception {
        SOAPConnectionFactory scFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection con = scFactory.createConnection();

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage msg = factory.createMessage();
        java.net.URL url1 = new java.net.URL("http://slashdot.org/slashdot.xml");
        java.net.URL url2 = new java.net.URL("http://www.apache.org/LICENSE.txt");

        AttachmentPart a1 = msg.createAttachmentPart(new javax.activation.DataHandler(url1));
        a1.setContentType("text/xml");
        msg.addAttachmentPart(a1);
        AttachmentPart a2 = msg.createAttachmentPart(new javax.activation.DataHandler(url1));
        a2.setContentType("text/xml");
        msg.addAttachmentPart(a2);
        AttachmentPart a3 = msg.createAttachmentPart(new javax.activation.DataHandler(url2));
        a3.setContentType("text/plain");
        msg.addAttachmentPart(a3);

        assertTrue(msg.countAttachments()==3);

        javax.xml.soap.MimeHeaders mimeHeaders = new javax.xml.soap.MimeHeaders();
        mimeHeaders.addHeader("Content-Type", "text/xml");

        int nAttachments = 0;
        java.util.Iterator iterator = msg.getAttachments(mimeHeaders);
	    while (iterator.hasNext()) {
            nAttachments++;
	        AttachmentPart ap = (AttachmentPart)iterator.next();
	        assertTrue(ap.equals(a1) || ap.equals(a2));
	    }
        assertTrue(nAttachments==2);
    }
    
    public void testBadAttSize() throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();

        ByteArrayInputStream ins=new ByteArrayInputStream(new byte[5]);
        DataHandler dh=new DataHandler(new Src(ins,"text/plain"));
        AttachmentPart part = message.createAttachmentPart(dh);
        assertEquals("Size should match",5,part.getSize());
    }

    class Src implements DataSource{
        InputStream m_src;
        String m_type;

        public Src(InputStream data, String type){
            m_src=data;
            m_type=type;
        }
        public String getContentType(){
            return m_type;
        }
        public InputStream getInputStream() throws IOException{
            m_src.reset();
            return m_src;
        }
        public String getName(){
            return "Some-Data";
        }
        public OutputStream getOutputStream(){
            throw new UnsupportedOperationException("I don't give output streams");
        }
    }
    
    public static void main(String[] args) throws Exception {
        AttachmentTest tester = new AttachmentTest("TestSAAJ");
        tester.testMultipleAttachments();
        tester.testStringAttachment();
        tester.testBadAttSize();
    }

}
