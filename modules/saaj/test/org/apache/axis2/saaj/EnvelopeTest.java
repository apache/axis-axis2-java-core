package org.apache.axis2.saaj;

import junit.framework.TestCase;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.Text;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConnection;
import java.io.ByteArrayInputStream;
import java.util.Iterator;

public class EnvelopeTest extends TestCase {

    private static final String XML_STRING =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "                   xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            " <soapenv:Header>\n" +
            "  <shw:Hello xmlns:shw=\"http://www.jcommerce.net/soap/ns/SOAPHelloWorld\">\n" +
            "    <shw:Myname>Tony</shw:Myname>\n" +
            "  </shw:Hello>\n" +
            " </soapenv:Header>\n" +
            " <soapenv:Body>\n" +
            "  <shw:Address shw:t='test' xmlns:shw=\"http://www.jcommerce.net/soap/ns/SOAPHelloWorld\">\n" +
            "    <shw:City>GENT</shw:City>\n" +
            "  </shw:Address>\n" +
            " </soapenv:Body>\n" +
            "</soapenv:Envelope>";

    public EnvelopeTest(String name) {
        super(name);
    }

    public void testEnvelope() throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage smsg =
                mf.createMessage(new MimeHeaders(), new ByteArrayInputStream(XML_STRING.getBytes()));
        SOAPPart sp = smsg.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        smsg.writeTo(System.out);
        assertTrue(se != null);
    }

    public void testEnvelope2() throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        final ByteArrayInputStream baIS = new ByteArrayInputStream(XML_STRING.getBytes());
        final MimeHeaders mimeheaders = new MimeHeaders();
        mimeheaders.addHeader("Content-Type", "multipart/related");
        SOAPMessage smsg =
                mf.createMessage(mimeheaders, baIS);

        smsg.writeTo(System.out);

        SOAPEnvelope envelope = smsg.getSOAPPart().getEnvelope();
        SOAPBody body = envelope.getBody();
        assertTrue(body != null);
    }

    // TODO: This test fails due to some issues in OM. Needs to be added to the test suite
    //   that issue is fixed
    public void _testEnvelopeWithLeadingComment() throws Exception {
        String soapMessageWithLeadingComment =
                "<?xml version='1.0' encoding='UTF-8'?>" +
                "<!-- Comment -->" +
                "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
                "<env:Body><echo><arg0>Hello</arg0></echo></env:Body>" +
                "</env:Envelope>";

        SOAPConnectionFactory scFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection con = scFactory.createConnection();

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message =
                factory.createMessage(new MimeHeaders(),
                                      new ByteArrayInputStream(soapMessageWithLeadingComment.getBytes()));
        SOAPPart part = message.getSOAPPart();
        SOAPEnvelope envelope = part.getEnvelope();
        message.writeTo(System.out);
        assertTrue(envelope != null);
        assertTrue(envelope.getBody() != null);
    }

    public void testEnvelopeWithCommentInEnvelope() throws Exception {

        String soapMessageWithLeadingComment =
                "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<soapenv:Envelope  xmlns='http://somewhere.com/html'\n" +
                "                   xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'\n" +
                "                   xmlns:xsd='http://www.w3.org/2001/XMLSchema'\n" +
                "                   xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>\n" +
                "<!-- Comment -->" +
                " <soapenv:Body>\n" +
                "    <echo><arg0>Hello</arg0></echo>" +
//                "    <t:echo xmlns:t='http://test.org/Test'><t:arg0>Hello</t:arg0></t:echo>" +
" </soapenv:Body>\n" +
"</soapenv:Envelope>";

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message =
                factory.createMessage(new MimeHeaders(),
                                      new ByteArrayInputStream(soapMessageWithLeadingComment.getBytes()));
        SOAPPart part = message.getSOAPPart();
        SOAPEnvelope envelope = part.getEnvelope();
        message.writeTo(System.out);
        assertTrue(envelope != null);
        assertTrue(envelope.getBody() != null);
    }

    public void testEnvelopeWithCommentInBody() throws Exception {

        String soapMessageWithLeadingComment =
                "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'\n" +
                "                   xmlns:xsd='http://www.w3.org/2001/XMLSchema'\n" +
                "                   xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>\n" +
                " <soapenv:Body>\n" +
                "<!-- Comment -->" +
//                "    <echo><arg0>Hello</arg0></echo>" +
"    <t:echo xmlns:t='http://test.org/Test'><t:arg0>Hello</t:arg0></t:echo>" +
" </soapenv:Body>\n" +
"</soapenv:Envelope>";

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message =
                factory.createMessage(new MimeHeaders(),
                                      new ByteArrayInputStream(soapMessageWithLeadingComment.getBytes()));
        SOAPPart part = message.getSOAPPart();
        SOAPEnvelope envelope = part.getEnvelope();
        message.writeTo(System.out);
        assertTrue(envelope != null);
        assertTrue(envelope.getBody() != null);
    }

    public void testEnvelopeWithComments() throws Exception {

        String soapMessageWithLeadingComment =
                "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'\n" +
                "                   xmlns:xsd='http://www.w3.org/2001/XMLSchema'\n" +
                "                   xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>\n" +
                " <soapenv:Header>\n" +
                "<!-- Comment -->" +
                "  <shw:Hello xmlns:shw=\"http://www.jcommerce.net/soap/ns/SOAPHelloWorld\">\n" +
                "<!-- Comment -->" +
                "    <shw:Myname><!-- Comment -->Tony</shw:Myname>\n" +
                "  </shw:Hello>\n" +
                " </soapenv:Header>\n" +
                " <soapenv:Body>\n" +
                "<!-- Comment -->" +
                "    <t:echo xmlns:t='http://test.org/Test'><t:arg0>Hello</t:arg0></t:echo>" +
                " </soapenv:Body>\n" +
                "</soapenv:Envelope>";

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message =
                factory.createMessage(new MimeHeaders(),
                                      new ByteArrayInputStream(soapMessageWithLeadingComment.getBytes()));
        SOAPPart part = message.getSOAPPart();
        SOAPEnvelope envelope = part.getEnvelope();
        message.writeTo(System.out);
        assertTrue(envelope != null);
        assertTrue(envelope.getBody() != null);
    }

    //TODO: Fails. Check this. Faults need more thorough testing
    public void testFaults() throws Exception {
        SOAPEnvelope envelope = getSOAPEnvelope();
        SOAPBody body = envelope.getBody();

        assertFalse(body.hasFault());
        SOAPFault soapFault = body.addFault();
        assertTrue(body.hasFault());
        assertNotNull(body.getFault());

        soapFault.setFaultString("myFault");
        String faultString = soapFault.getFaultString();
        System.err.println("######## faultString=" + faultString);

//        soapFault.setFaultCode("CODE");
//        soapFault.getFaultCode();
//        System.err.println("######## faultCode=" + soapFault.getFaultCode());

//        assertEquals()

        //soapFault.setFaultCode("myFault");
        //String fc = soapFault.getFaultCode();
        /*soapFault.setFaultString("myFault");
        String fc = soapFault.getFaultString(); //Chk the same for FaultCode as well

        // currently not done in SAAJ
        assertTrue(fc.equals("myFault"));*/
    }
    /*
   public void testFaults2() throws Exception {

       SOAPEnvelope envelope = getSOAPEnvelope();
       SOAPBody body = envelope.getBody();
       SOAPFault sf = body.addFault();

       assertTrue(body.getFault() != null);

       Detail d1 = sf.addDetail();
       Name name = envelope.createName("GetLastTradePrice", "WOMBAT",
                                       "http://www.wombat.org/trader");
       d1.addDetailEntry(name);

       Detail d2 = sf.getDetail();
       assertTrue(d2 != null);
       Iterator i = d2.getDetailEntries();
       assertTrue(getIteratorCount(i) == 1);
       i = d2.getDetailEntries();
       //message.writeTo(System.out);
       while (i.hasNext()) {
           DetailEntry de = (DetailEntry) i.next();
           assertEquals(de.getElementName(), name);
       }
   }*/

    public void testHeaderElements() throws Exception {
        SOAPEnvelope envelope = getSOAPEnvelope();
        SOAPHeader header = envelope.getHeader();

        SOAPHeaderElement headerEle = header.addHeaderElement(envelope.createName("foo1",
                                                                                  "f1",
                                                                                  "foo1-URI"));
        headerEle.setActor("actor-URI");
        headerEle.setMustUnderstand(true);

        Iterator iterator = header.extractHeaderElements("actor-URI");
        int cnt = 0;
        while (iterator.hasNext()) {
            cnt++;
            SOAPHeaderElement resultHeaderEle = (SOAPHeaderElement) iterator.next();

            assertEquals(headerEle.getActor(), resultHeaderEle.getActor());
            assertEquals(resultHeaderEle.getMustUnderstand(),headerEle.getMustUnderstand());
        }
        assertTrue(cnt == 1);
        iterator = header.extractHeaderElements("actor-URI");
        assertTrue(!iterator.hasNext());
    }

    public void testText() throws Exception {
        SOAPEnvelope envelope = getSOAPEnvelope();
        SOAPBody body = envelope.getBody();
        Iterator iStart = body.getChildElements();
        int countStart = getIteratorCount(iStart);

        final String bodyText = "<txt>This is the body text</txt>";

        SOAPElement se = body.addTextNode(bodyText);
        assertTrue(se != null);

        assertTrue(body.getValue().equals(bodyText));

        Iterator i = body.getChildElements();
        int count = getIteratorCount(i);
        assertTrue(count == countStart + 1);
    }

    public void testNonCommentText() throws Exception {
        SOAPEnvelope envelope = getSOAPEnvelope();
        SOAPBody body = envelope.getBody();
        SOAPElement se = body.addTextNode("<txt>This is text</txt>");
        Iterator iterator = se.getChildElements();
        Object o = null;
        while (iterator.hasNext()) {
            o = iterator.next();
            if (o instanceof Text) {
                break;
            }
        }
        assertTrue(o instanceof Text);
        Text t = (Text) o;
        assertTrue(!t.isComment());
    }

    public void testCommentText() throws Exception {
        SOAPEnvelope envelope = getSOAPEnvelope();
        SOAPBody body = envelope.getBody();
        SOAPElement se = body.addTextNode("<!-- This is a comment -->");
        Iterator iterator = se.getChildElements();
        Node n = null;
        while (iterator.hasNext()) {
            n = (Node) iterator.next();
            if (n instanceof Text)
                break;
        }
        assertTrue(n instanceof Text);
        Text t = (Text) n;
        assertTrue(t.isComment());
    }

    public void testAttributes() throws Exception {
        SOAPEnvelope envelope = getSOAPEnvelope();
        SOAPBody body = envelope.getBody();

        Name name1 = envelope.createName("MyAttr1");
        String value1 = "MyValue1";

        Name name2 = envelope.createName("MyAttr2");
        String value2 = "MyValue2";

        Name name3 = envelope.createName("MyAttr3");
        String value3 = "MyValue3";

        body.addAttribute(name1, value1);
        body.addAttribute(name2, value2);
        body.addAttribute(name3, value3);

        Iterator iterator = body.getAllAttributes();
        assertTrue(getIteratorCount(iterator) == 3);
        iterator = body.getAllAttributes();

        boolean foundName1 = false;
        boolean foundName2 = false;
        boolean foundName3 = false;
        while (iterator.hasNext()) {
            Name name = (Name) iterator.next();
            if (name.equals(name1)) {
                foundName1 = true;
                assertEquals(value1, body.getAttributeValue(name));
            } else if (name.equals(name2)) {
                foundName2 = true;
                assertEquals(value2, body.getAttributeValue(name));
            } else if (name.equals(name3)) {
                foundName3 = true;
                assertEquals(value3, body.getAttributeValue(name));
            }
        }
        assertTrue(foundName1 && foundName2 && foundName3);
    }

    public void testAttributes2() throws Exception {
        SOAPEnvelope envelope = getSOAPEnvelope();
        SOAPBody body = envelope.getBody();

        Name name1 = envelope.createName("MyAttr1", "att", "http://test.com/Attr");
        String value1 = "MyValue1";

        Name name2 = envelope.createName("MyAttr2");
        String value2 = "MyValue2";

        Name name3 = envelope.createName("MyAttr3");
        String value3 = "MyValue3";

        body.addAttribute(name1, value1);
        body.addAttribute(name2, value2);
        body.addAttribute(name3, value3);

        Iterator iterator = body.getAllAttributes();
        assertTrue(getIteratorCount(iterator) == 3);
        iterator = body.getAllAttributes();

        boolean foundName1 = false;
        boolean foundName2 = false;
        boolean foundName3 = false;
        while (iterator.hasNext()) {
            Name name = (Name) iterator.next();
            if (name.equals(name1)) {
                foundName1 = true;
                assertEquals(value1, body.getAttributeValue(name));
            } else if (name.equals(name2)) {
                foundName2 = true;
                assertEquals(value2, body.getAttributeValue(name));
            } else if (name.equals(name3)) {
                foundName3 = true;
                assertEquals(value3, body.getAttributeValue(name));
            }
        }
        assertTrue(foundName1 && foundName2 && foundName3);
    }

    public void testAttributes3() throws Exception {
        SOAPEnvelope envelope = getSOAPEnvelope();
        SOAPBody body = envelope.getBody();

        Name name1 = envelope.createName("MyAttr1", "att", "http://test.com/Attr");
        String value1 = "MyValue1";

        Name name2 = envelope.createName("MyAttr2", "att", "http://test.com/Attr");
        String value2 = "MyValue2";

        Name name3 = envelope.createName("MyAttr3", "att", "http://test.com/Attr");
        String value3 = "MyValue3";

        body.addAttribute(name1, value1);
        body.addAttribute(name2, value2);
        body.addAttribute(name3, value3);

        Iterator iterator = body.getAllAttributes();
        assertTrue(getIteratorCount(iterator) == 3);
        iterator = body.getAllAttributes();

        boolean foundName1 = false;
        boolean foundName2 = false;
        boolean foundName3 = false;
        while (iterator.hasNext()) {
            Name name = (Name) iterator.next();
            if (name.equals(name1)) {
                foundName1 = true;
                assertEquals(value1, body.getAttributeValue(name));
            } else if (name.equals(name2)) {
                foundName2 = true;
                assertEquals(value2, body.getAttributeValue(name));
            } else if (name.equals(name3)) {
                foundName3 = true;
                assertEquals(value3, body.getAttributeValue(name));
            }
        }
        assertTrue(foundName1 && foundName2 && foundName3);
    }

    private SOAPEnvelope getSOAPEnvelope() throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        return message.getSOAPPart().getEnvelope();
    }

    private int getIteratorCount(java.util.Iterator i) {
        int count = 0;
        while (i.hasNext()) {
            count++;
            i.next();
        }
        return count;
    }

}
