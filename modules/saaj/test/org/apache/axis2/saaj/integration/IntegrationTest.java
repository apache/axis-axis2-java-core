package org.apache.axis2.saaj.integration;

import junit.framework.TestCase;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.util.Utils;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class IntegrationTest extends TestCase {

    private static final String ADDRESS = "http://127.0.0.1:" +
                                          (UtilServer.TESTING_PORT) +
                                          "/axis2/services/Echo";
//    private static final String ADDRESS = "http://127.0.0.1:8081" +
//                                          "/axis2/services/Echo";
    public static final EndpointReference TARGET_EPR = new EndpointReference(ADDRESS);

    public static final QName SERVICE_NAME = new QName("Echo");
    public static final QName OPERATION_NAME = new QName("echo");

    public static final String SAAJ_REPO = "target/test-resources/saaj-repo";

    public IntegrationTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        UtilServer.start(SAAJ_REPO);
        final AxisService service = Utils.createSimpleService(SERVICE_NAME,
                                                              EchoService.class.getName(),
                                                              OPERATION_NAME);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(SERVICE_NAME);
        UtilServer.stop();
        UtilServer.unDeployClientService();
    }

    public void testSendReceiveSimpleSOAPMessage() {
        try {
            MessageFactory mf = MessageFactory.newInstance();
            SOAPMessage request = mf.createMessage();

            createSimpleSOAPPart(request);

            SOAPConnection sCon = SOAPConnectionFactory.newInstance().createConnection();
            SOAPMessage response = sCon.call(request, ADDRESS);
            assertFalse(response.getAttachments().hasNext());
            assertEquals(0, response.countAttachments());

            String responseStr = printResponse(response);
            assertTrue(responseStr.indexOf("echo") != -1);
        } catch (SOAPException e) {
            e.printStackTrace();
            fail("Unexpected Exception while running test: " + e);
        } catch (IOException e) {
            fail("Unexpected Exception while running test: " + e);
        }
    }

    private String printResponse(final SOAPMessage response) throws SOAPException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.writeTo(baos);
        String responseStr = baos.toString();

        System.out.println("\n\n----------------------Response-------------------------\n" +
                           responseStr);
        System.out.println("-------------------------------------------------------\n\n");
        assertTrue(responseStr.indexOf("This is some text") != -1);
        return responseStr;
    }

    public void testSendReceiveMessageWithAttachment() throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage request = mf.createMessage();

        //create the SOAPPart
        createSOAPPart(request);

        //Attach a text/plain object with the SOAP request
        String sampleMessage = "Sample Message: Hello World!";
        AttachmentPart textAttach = request.createAttachmentPart(sampleMessage, "text/plain");
        textAttach.addMimeHeader("Content-Transfer-Encoding", "binary");
        textAttach.setContentId("submitSampleText@apache.org");
        request.addAttachmentPart(textAttach);

        //Attach a java.awt.Image object to the SOAP request
        String jpgfilename = "test-resources/axis2.jpg";
        File myfile = new File(jpgfilename);
        FileDataSource fds = new FileDataSource(myfile);
        DataHandler imageDH = new DataHandler(fds);
        AttachmentPart jpegAttach = request.createAttachmentPart(imageDH);
        jpegAttach.addMimeHeader("Content-Transfer-Encoding", "binary");
        jpegAttach.setContentId("submitSampleImage@apache.org");
        jpegAttach.setContentType("image/jpg");
        request.addAttachmentPart(jpegAttach);

        SOAPConnection sCon = SOAPConnectionFactory.newInstance().createConnection();

        SOAPMessage response = sCon.call(request, ADDRESS);

        int attachmentCount = response.countAttachments();
        assertTrue(attachmentCount == 2);

        Iterator attachIter = response.getAttachments();

        int i = 0;
        while (attachIter.hasNext()) {
            AttachmentPart attachment = (AttachmentPart) attachIter.next();
            final Object content = attachment.getDataHandler().getContent();
            if (content instanceof String) {
                assertEquals(sampleMessage, (String) content);
            } else if (content instanceof ByteArrayInputStream) {
                ByteArrayInputStream bais = (ByteArrayInputStream) content;
                byte[] b = new byte[15000];
                final int lengthRead = bais.read(b);
                FileOutputStream fos =
                        new FileOutputStream(new File("target/test-resources/result" + (i++) + ".jpg"));
                fos.write(b, 0, lengthRead);
                fos.flush();
                fos.close();

                assertTrue(attachment.getContentType().equals("image/jpeg")
                           || attachment.getContentType().equals("text/plain"));
            }
        }

        /*final SOAPBody respBody = response.getSOAPPart().getEnvelope().getBody();
        System.out.println("------------------------------------");
        for (Iterator childEleIter = respBody.getChildElements(); childEleIter.hasNext();) {
            SOAPElement o = (SOAPElement) childEleIter.next();
            System.out.println("@@@@@@@@@ o.tn=" + o.getTagName());
            System.out.println("------------------------------------------");
            for (Iterator iter = o.getChildElements(); iter.hasNext();) {
                SOAPElement p = (SOAPElement) iter.next();
                System.out.println("@@@@@@@@@ p.o=" + p);
                System.out.println("@@@@@@@@@ p.pre=" + p.getPrefix());
                System.out.println("@@@@@@@@@ p.ln=" + p.getLocalName());
                System.out.println("@@@@@@@@@ p.tn=" + p.getTagName());
                System.out.println("@@@@@@@@@ p.ns URI=" + p.getNamespaceURI());
                System.out.println("@@@@@@@@@ p.Val=" + p.getValue());
            }
        }
        System.out.println("------------------------------------");*/

//        response.getSOAPPart().getEnvelope().getHeader().extractAllHeaderElements();
//        sCon.call(response, ADDRESS);

//        printResponse(response);
    }

    private void createSOAPPart(SOAPMessage message) throws SOAPException {
        SOAPPart sPart = message.getSOAPPart();
        SOAPEnvelope env = sPart.getEnvelope();
        SOAPBody body = env.getBody();

        final SOAPHeader soapHeader = env.getHeader();
        soapHeader.addHeaderElement(env.createName("TestHeader1", "swa", "http://fakeNamespace.org"));
        soapHeader.addHeaderElement(env.createName("TestHeader2", "swa", "http://fakeNamespace.org"));
        final SOAPHeaderElement headerEle3 =
                soapHeader.addHeaderElement(env.createName("TestHeader3", "swa", "http://fakeNamespace.org"));
        final SOAPElement ch1 = headerEle3.addChildElement("he3", "swa");
        ch1.addTextNode("Im Header Element of header3");

        Name ns = env.createName("echo", "swa", "http://fakeNamespace.org");
        SOAPBodyElement bodyElement = body.addBodyElement(ns);

        Name nameMain = env.createName("internal");
        SOAPElement mainChildEle = bodyElement.addChildElement(nameMain);

        Name ns2 = env.createName("text");
        SOAPElement textReference = mainChildEle.addChildElement(ns2);
        Name hrefAttr = env.createName("href");
        textReference.addAttribute(hrefAttr, "cid:submitSampleText@apache.org");

        Name ns3 = env.createName("image");
        SOAPElement imageReference = mainChildEle.addChildElement(ns3);
        Name ns31 = env.createName("inner");
        final SOAPElement img = imageReference.addChildElement(ns31);
        img.addAttribute(hrefAttr, "cid:submitSampleImage@apache.org");

        Name ns4 = env.createName("plaintxt");
        SOAPElement plainTxt = mainChildEle.addChildElement(ns4);
        plainTxt.addTextNode("This is simple plain text");

        Name ns5 = env.createName("nested");
        SOAPElement nested = mainChildEle.addChildElement(ns5);
        nested.addTextNode("Nested1 Plain Text");
        Name ns6 = env.createName("nested2");
        SOAPElement nested2 = nested.addChildElement(ns6);
        nested2.addTextNode("Nested2 Plain Text");
    }

    private void createSimpleSOAPPart(SOAPMessage message) throws SOAPException {
        SOAPPart sPart = message.getSOAPPart();
        SOAPEnvelope env = sPart.getEnvelope();
        SOAPBody body = env.getBody();

        Name ns = env.createName("echo", "swa2", "http://fakeNamespace2.org");
        final SOAPBodyElement bodyElement = body.addBodyElement(ns);
        Name ns2 = env.createName("something");
        final SOAPElement ele1 = bodyElement.addChildElement(ns2);
        ele1.addTextNode("This is some text");

        Name ns3 = env.createName("ping", "swa3", "http://fakeNamespace3.org");
        final SOAPBodyElement bodyElement2 = body.addBodyElement(ns3);
        Name ns4 = env.createName("another");
        final SOAPElement ele2 = bodyElement2.addChildElement(ns4);
        ele2.addTextNode("This is another text");
    }
}
