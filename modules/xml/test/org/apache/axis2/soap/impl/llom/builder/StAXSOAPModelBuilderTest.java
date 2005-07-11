package org.apache.axis2.soap.impl.llom.builder;

import junit.framework.TestCase;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFault;
import org.apache.axis2.soap.SOAPFaultCode;
import org.apache.axis2.soap.SOAPFaultDetail;
import org.apache.axis2.soap.SOAPFaultNode;
import org.apache.axis2.soap.SOAPFaultReason;
import org.apache.axis2.soap.SOAPFaultRole;
import org.apache.axis2.soap.SOAPFaultSubCode;
import org.apache.axis2.soap.SOAPFaultText;
import org.apache.axis2.soap.SOAPFaultValue;
import org.apache.axis2.soap.SOAPHeader;
import org.apache.axis2.soap.SOAPHeaderBlock;
import org.apache.axis2.soap.impl.llom.SOAPConstants;
import org.apache.axis2.soap.impl.llom.soap11.SOAP11Constants;
import org.apache.axis2.soap.impl.llom.soap12.SOAP12Constants;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Anushka
 * Date: Jun 3, 2005
 * Time: 10:22:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class StAXSOAPModelBuilderTest extends TestCase {

    public void setUp() {

    }

    public void testStAXSOAPModelBuilder() {
        String soap12Message =
                "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                "   <env:Header>\n" +
                "       <test:echoOk xmlns:test=\"http://example.org/ts-tests\"\n" +
                "                    env:role=\"http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver\"\n" +
                "                    env:mustUnderstand=\"true\">\n" +
                "                       foo\n" +
                "       </test:echoOk>\n" +
                "   </env:Header>\n" +
                "   <env:Body>\n" +
                "       <env:Fault>\n" +
                "           <env:Code>\n" +
                "               <env:Value>env:Sender</env:Value>\n" +
                "               <env:SubCode>\n" +
                "                   <env:Value>m:MessageTimeout</env:Value>\n" +
                "                   <env:SubCode>\n" +
                "                       <env:Value>m:MessageTimeout</env:Value>\n" +
                "                   </env:SubCode>\n" +
                "               </env:SubCode>\n" +
                "           </env:Code>\n" +
                "           <env:Reason>\n" +
                "               <env:Text>Sender Timeout</env:Text>\n" +
                "           </env:Reason>\n" +
                "           <env:Node>\n" +
                "               http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver\n" +
                "           </env:Node>\n" +
                "           <env:Role>\n" +
                "               ultimateReceiver\n" +
                "           </env:Role>\n" +
                "           <env:Detail xmlns:m=\"http:www.sample.org\">\n" +
                "               Details of error\n" +
                "               <m:MaxTime m:detail=\"This is only a test\">\n" +
                "                   P5M\n" +
                "               </m:MaxTime>\n" +
                "               <m:AveTime>\n" +
                "                   <m:Time>\n" +
                "                       P3M\n" +
                "                   </m:Time>\n" +
                "               </m:AveTime>\n" +
                "           </env:Detail>\n" +
                "       </env:Fault>\n" +
                "   </env:Body>\n" +
                "</env:Envelope>";

        String soap11Message =
                "<?xml version='1.0' ?>" +
                "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "   <env:Header>\n" +
                "       <test:echoOk xmlns:test=\"http://example.org/ts-tests\"\n" +
                "                    env:actor=\"http://schemas.xmlsoap.org/soap/actor/next\"\n" +
                "                    env:mustUnderstand=\"1\"" +
                "       >\n" +
                "                       foo\n" +
                "       </test:echoOk>\n" +
                "   </env:Header>\n" +
                "   <env:Body>\n" +
                "       <env:Fault>\n" +
                "           <env:faultcode>\n" +
                "               env:Sender\n" +
                "           </env:faultcode>\n" +
                "           <env:faultstring>\n" +
                "               Sender Timeout\n" +
                "           </env:faultstring>\n" +
                "           <env:faultactor>\n" +
                "               http://schemas.xmlsoap.org/soap/envelope/actor/ultimateReceiver\n" +
                "           </env:faultactor>\n" +
                "           <env:detail xmlns:m=\"http:www.sample.org\">\n" +
                "               Details of error\n" +
                "               <m:MaxTime m:detail=\"This is only a test\">\n" +
                "                   P5M\n" +
                "               </m:MaxTime>\n" +
                "               <m:AveTime>\n" +
                "                   <m:Time>\n" +
                "                       P3M\n" +
                "                   </m:Time>\n" +
                "               </m:AveTime>\n" +
                "           </env:detail>\n" +
                "           <n:Test xmlns:n=\"http:www.Test.org\">\n" +
                "               <n:TestElement>\n" +
                "                   This is only a test\n" +
                "               </n:TestElement>\n" +
                "           </n:Test>\n" +
                "       </env:Fault>\n" +
                "   </env:Body>\n" +
                "</env:Envelope>";


        try {
            XMLStreamReader sopa12Parser = XMLInputFactory.newInstance()
                    .createXMLStreamReader(new StringReader(soap12Message));
            OMXMLParserWrapper soap12Builder = new StAXSOAPModelBuilder(
                    sopa12Parser);
            SOAPEnvelope soap12Envelope = (SOAPEnvelope) soap12Builder.getDocumentElement();
//            soap12Envelope.build();
//            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
//            soap12Envelope.serialize(writer);
//		    writer.flush();

            assertTrue("SOAP 1.2 :- envelope local name mismatch",
                    soap12Envelope.getLocalName().equals(
                            SOAPConstants.SOAPENVELOPE_LOCAL_NAME));
            assertTrue("SOAP 1.2 :- envelope namespace uri mismatch",
                    soap12Envelope.getNamespace().getName().equals(
                            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

            SOAPHeader header = soap12Envelope.getHeader();
            assertTrue("SOAP 1.2 :- Header local name mismatch",
                    header.getLocalName().equals(
                            SOAPConstants.HEADER_LOCAL_NAME));
            assertTrue("SOAP 1.2 :- Header namespace uri mismatch",
                    header.getNamespace().getName().equals(
                            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

            SOAPHeaderBlock headerBlock = (SOAPHeaderBlock) header.getFirstElement();
            assertTrue("SOAP 1.2 :- Header block name mismatch",
                    headerBlock.getLocalName().equals("echoOk"));
            assertTrue("SOAP 1.2 :- Header block name space uri mismatch",
                    headerBlock.getNamespace().getName().equals(
                            "http://example.org/ts-tests"));
            assertTrue("SOAP 1.2 :- Headaer block text mismatch",
                    headerBlock.getText().equals("foo"));

            Iterator headerBlockAttributes = headerBlock.getAttributes();
            OMAttribute roleAttribute = (OMAttribute) headerBlockAttributes.next();
            assertTrue("SOAP 1.2 :- Role attribute name mismatch",
                    roleAttribute.getLocalName().equals(
                            SOAP12Constants.SOAP_ROLE));
            assertTrue("SOAP 1.2 :- Role value mismatch",
                    roleAttribute.getValue().equals(
                            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI + "/" +
                    SOAP12Constants.SOAP_ROLE +
                    "/" +
                    "ultimateReceiver"));
            assertTrue("SOAP 1.2 :- Role attribute namespace uri mismatch",
                    roleAttribute.getNamespace().getName().equals(
                            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

            OMAttribute mustUnderstandAttribute = (OMAttribute) headerBlockAttributes.next();
            assertTrue("SOAP 1.2 :- Mustunderstand attribute name mismatch",
                    mustUnderstandAttribute.getLocalName().equals(
                            SOAPConstants.ATTR_MUSTUNDERSTAND));
            assertTrue("SOAP 1.2 :- Mustunderstand value mismatch",
                    mustUnderstandAttribute.getValue().equals(
                            SOAPConstants.ATTR_MUSTUNDERSTAND_TRUE));
            assertTrue(
                    "SOAP 1.2 :- Mustunderstand attribute namespace uri mismatch",
                    mustUnderstandAttribute.getNamespace().getName().equals(
                            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

            SOAPBody body = soap12Envelope.getBody();
            assertTrue("SOAP 1.2 :- Body local name mismatch",
                    body.getLocalName().equals(SOAPConstants.BODY_LOCAL_NAME));
            assertTrue("SOAP 1.2 :- Body namespace uri mismatch",
                    body.getNamespace().getName().equals(
                            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

            SOAPFault fault = body.getFault();
            assertTrue("SOAP 1.2 :- Fault local name mismatch",
                    fault.getLocalName().equals(
                            SOAPConstants.SOAPFAULT_LOCAL_NAME));
            assertTrue("SOAP 1.2 :- Fault namespace uri mismatch",
                    fault.getNamespace().getName().equals(
                            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

            Iterator iteratorInFault = fault.getChildren();

            iteratorInFault.next();
            SOAPFaultCode code = (SOAPFaultCode) iteratorInFault.next();
            assertTrue("SOAP 1.2 :- Fault code local name mismatch",
                    code.getLocalName().equals(
                            SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME));
            assertTrue("SOAP 1.2 :- Fault code namespace uri mismatch",
                    code.getNamespace().getName().equals(
                            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

            Iterator iteratorInCode = code.getChildren();

            iteratorInCode.next();
            SOAPFaultValue value1 = (SOAPFaultValue) iteratorInCode.next();
            assertTrue("SOAP 1.2 :- Fault code value local name mismatch",
                    value1.getLocalName().equals(
                            SOAP12Constants.SOAP_FAULT_VALUE_LOCAL_NAME));
            assertTrue("SOAP 1.2 :- Fault code namespace uri mismatch",
                    value1.getNamespace().getName().equals(
                            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));
            assertTrue("SOAP 1.2 :- Value1 text mismatch",
                    value1.getText().equals("env:Sender"));

            iteratorInCode.next();
            SOAPFaultSubCode subCode1 = (SOAPFaultSubCode) iteratorInCode.next();
            assertTrue("SOAP 1.2 :- Fault sub code local name mismatch",
                    subCode1.getLocalName().equals(
                            SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME));
            assertTrue("SOAP 1.2 :- Fault subcode namespace uri mismatch",
                    subCode1.getNamespace().getName().equals(
                            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

            Iterator iteratorInSubCode1 = subCode1.getChildren();

            iteratorInSubCode1.next();
            SOAPFaultValue value2 = (SOAPFaultValue) iteratorInSubCode1.next();
            assertTrue("SOAP 1.2 :- Fault code value local name mismatch",
                    value2.getLocalName().equals(
                            SOAP12Constants.SOAP_FAULT_VALUE_LOCAL_NAME));
            assertTrue("SOAP 1.2 :- Fault code namespace uri mismatch",
                    value2.getNamespace().getName().equals(
                            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));
            assertTrue("SOAP 1.2 :- Value2 text mismatch",
                    value2.getText().equals("m:MessageTimeout"));

            iteratorInSubCode1.next();
            SOAPFaultSubCode subCode2 = (SOAPFaultSubCode) iteratorInSubCode1.next();
            assertTrue("SOAP 1.2 :- Fault sub code local name mismatch",
                    subCode2.getLocalName().equals(
                            SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME));
            assertTrue("SOAP 1.2 :- Fault subcode namespace uri mismatch",
                    subCode2.getNamespace().getName().equals(
                            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

            Iterator iteratorInSubCode2 = subCode2.getChildren();

            iteratorInSubCode2.next();
            SOAPFaultValue value3 = (SOAPFaultValue) iteratorInSubCode2.next();
            assertTrue("SOAP 1.2 :- Fault code value local name mismatch",
                    value3.getLocalName().equals(
                            SOAP12Constants.SOAP_FAULT_VALUE_LOCAL_NAME));
            assertTrue("SOAP 1.2 :- Fault code namespace uri mismatch",
                    value3.getNamespace().getName().equals(
                            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));
            assertTrue("SOAP 1.2 :- Value2 text mismatch",
                    value3.getText().equals("m:MessageTimeout"));

            iteratorInFault.next();
            SOAPFaultReason reason = (SOAPFaultReason) iteratorInFault.next();
            assertTrue("SOAP 1.2 :- Fault reason local name mismatch",
                    reason.getLocalName().equals(
                            SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME));
            assertTrue("SOAP 1.2 :- Fault reason namespace uri mismatch",
                    reason.getNamespace().getName().equals(
                            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

            Iterator iteratorInReason = reason.getChildren();

            iteratorInReason.next();
            SOAPFaultText text = (SOAPFaultText) iteratorInReason.next();
            assertTrue("SOAP 1.2 :- Fault text local name mismatch",
                    text.getLocalName().equals(
                            SOAP12Constants.SOAP_FAULT_TEXT_LOCAL_NAME));
            assertTrue("SOAP 1.2 :- Text namespace uri mismatch",
                    text.getNamespace().getName().equals(
                            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));
            assertTrue("SOAP 1.2 :- Text value mismatch",
                    text.getText().equals("Sender Timeout"));

            iteratorInFault.next();
            SOAPFaultNode node = (SOAPFaultNode) iteratorInFault.next();
            assertTrue("SOAP 1.2 :- Fault node local name mismatch",
                    node.getLocalName().equals(
                            SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME));
            assertTrue("SOAP 1.2 :- Fault node namespace uri mismatch",
                    node.getNamespace().getName().equals(
                            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));
            assertTrue("SOAP 1.2 :- Node value mismatch",
                    node.getText().equals(
                            "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver"));

            iteratorInFault.next();
            SOAPFaultRole role = (SOAPFaultRole) iteratorInFault.next();
            assertTrue("SOAP 1.2 :- Fault role local name mismatch",
                    role.getLocalName().equals(
                            SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME));
            assertTrue("SOAP 1.2 :- Fault role namespace uri mismatch",
                    role.getNamespace().getName().equals(
                            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));
            assertTrue("SOAP 1.2 :- Role value mismatch",
                    role.getText().equals("ultimateReceiver"));

            iteratorInFault.next();
            SOAPFaultDetail detail = (SOAPFaultDetail) iteratorInFault.next();
            assertTrue("SOAP 1.2 :- Fault detail local name mismatch",
                    detail.getLocalName().equals(
                            SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME));
            assertTrue("SOAP 1.2 :- Fault detail namespace uri mismatch",
                    detail.getNamespace().getName().equals(
                            SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

            assertTrue("SOAP 1.2 :- Text in detail mismatch",
                    detail.getText().equals("Details of error"));

            Iterator iteratorInDetail = detail.getChildren();

            iteratorInDetail.next();
            OMElement element1 = (OMElement) iteratorInDetail.next();
            assertTrue("SOAP 1.2 :- MaxTime element mismatch",
                    element1.getLocalName().equals("MaxTime"));
            assertTrue("SOAP 1.2 :- MaxTime element namespace mismatch",
                    element1.getNamespace().getName().equals(
                            "http:www.sample.org"));
            assertTrue("SOAP 1.2 :- Text value in MaxTime element mismatch",
                    element1.getText().equals("P5M"));

            Iterator attributeIterator = element1.getAttributes();
            OMAttribute attributeInMaxTime = (OMAttribute) attributeIterator.next();
            assertTrue("SOAP 1.2 :- Attribute local name mismatch",
                    attributeInMaxTime.getLocalName().equals("detail"));
            assertTrue("SOAP 1.2 :- Attribute namespace mismatch",
                    attributeInMaxTime.getNamespace().getName().equals(
                            "http:www.sample.org"));
            assertTrue("SOAP 1.2 :- Attribute value mismatch",
                    attributeInMaxTime.getValue().equals("This is only a test"));

            iteratorInDetail.next();
            OMElement element2 = (OMElement) iteratorInDetail.next();
            assertTrue("SOAP 1.2 :- AveTime element mismatch",
                    element2.getLocalName().equals("AveTime"));
            assertTrue("SOAP 1.2 :- AveTime element namespace mismatch",
                    element2.getNamespace().getName().equals(
                            "http:www.sample.org"));

            Iterator iteratorInAveTimeElement = element2.getChildren();

            iteratorInAveTimeElement.next();
            OMElement element21 = (OMElement) iteratorInAveTimeElement.next();
            assertTrue("SOAP 1.2 :- Time element mismatch",
                    element21.getLocalName().equals("Time"));
            assertTrue("SOAP 1.2 :- Time element namespace mismatch",
                    element21.getNamespace().getName().equals(
                            "http:www.sample.org"));
            assertTrue("SOAP 1.2 :- Text value in Time element mismatch",
                    element21.getText().equals("P3M"));

            XMLStreamReader sopa11Parser = XMLInputFactory.newInstance()
                    .createXMLStreamReader(new StringReader(soap11Message));
            OMXMLParserWrapper soap11Builder = new StAXSOAPModelBuilder(
                    sopa11Parser);
            SOAPEnvelope soap11Envelope = (SOAPEnvelope) soap11Builder.getDocumentElement();
//            soap11Envelope.build();
//            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
//            soap11Envelope.serialize(writer);
//		    writer.flush();

            assertTrue("SOAP 1.1 :- envelope local name mismatch",
                    soap11Envelope.getLocalName().equals(
                            SOAPConstants.SOAPENVELOPE_LOCAL_NAME));
            assertTrue("SOAP 1.1 :- envelope namespace uri mismatch",
                    soap11Envelope.getNamespace().getName().equals(
                            SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));

            header = soap11Envelope.getHeader();
            assertTrue("SOAP 1.1 :- Header local name mismatch",
                    header.getLocalName().equals(
                            SOAPConstants.HEADER_LOCAL_NAME));
            assertTrue("SOAP 1.1 :- Header namespace uri mismatch",
                    header.getNamespace().getName().equals(
                            SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));

            headerBlock = (SOAPHeaderBlock) header.getFirstElement();
            assertTrue("SOAP 1.1 :- Header block name mismatch",
                    headerBlock.getLocalName().equals("echoOk"));
            assertTrue("SOAP 1.1 :- Header block name space uri mismatch",
                    headerBlock.getNamespace().getName().equals(
                            "http://example.org/ts-tests"));
            assertTrue("SOAP 1.1 :- Headaer block text mismatch",
                    headerBlock.getText().equals("foo"));

            headerBlockAttributes = headerBlock.getAttributes();

            mustUnderstandAttribute =
                    (OMAttribute) headerBlockAttributes.next();
            assertTrue("SOAP 1.1 :- Mustunderstand attribute name mismatch",
                    mustUnderstandAttribute.getLocalName().equals(
                            SOAPConstants.ATTR_MUSTUNDERSTAND));
            assertTrue("SOAP 1.1 :- Mustunderstand value mismatch",
                    mustUnderstandAttribute.getValue().equals(
                            SOAPConstants.ATTR_MUSTUNDERSTAND_1));
            assertTrue(
                    "SOAP 1.1 :- Mustunderstand attribute namespace uri mismatch",
                    mustUnderstandAttribute.getNamespace().getName().equals(
                            SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));

            OMAttribute actorAttribute = (OMAttribute) headerBlockAttributes.next();
            assertTrue("SOAP 1.1 :- Actor attribute name mismatch",
                    actorAttribute.getLocalName().equals(
                            SOAP11Constants.ATTR_ACTOR));
            assertTrue("SOAP 1.1 :- Actor value mismatch",
                    actorAttribute.getValue().equals(
                            "http://schemas.xmlsoap.org/soap/" +
                    SOAP11Constants.ATTR_ACTOR +
                    "/" +
                    "next"));
            assertTrue("SOAP 1.1 :- Actor attribute namespace uri mismatch",
                    actorAttribute.getNamespace().getName().equals(
                            SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));

            body = soap11Envelope.getBody();
            assertTrue("SOAP 1.1 :- Body local name mismatch",
                    body.getLocalName().equals(SOAPConstants.BODY_LOCAL_NAME));
            assertTrue("SOAP 1.1 :- Body namespace uri mismatch",
                    body.getNamespace().getName().equals(
                            SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));

            fault = body.getFault();
            assertTrue("SOAP 1.1 :- Fault namespace uri mismatch",
                    fault.getNamespace().getName().equals(
                            SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));

            iteratorInFault = fault.getChildren();

            iteratorInFault.next();
            code = (SOAPFaultCode) iteratorInFault.next();
            assertEquals("SOAP Fault code local name mismatch",
                    code.getLocalName(),
                    (SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME));
            assertTrue("SOAP 1.1 :- Fault code namespace uri mismatch",
                    code.getNamespace().getName().equals(
                            SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));
            assertEquals("SOAP 1.1 :- Fault code value mismatch",
                    code.getValue().getText().trim(),
                    "env:Sender");

            iteratorInFault.next();
            reason = (SOAPFaultReason) iteratorInFault.next();
            assertTrue("SOAP 1.1 :- Fault string local name mismatch",
                    reason.getLocalName().equals(
                            SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME));
            assertTrue("SOAP 1.2 :- Fault string namespace uri mismatch",
                    reason.getNamespace().getName().equals(
                            SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));
            assertTrue("SOAP 1.1 :- Fault string value mismatch",
                    reason.getSOAPText().getText().equals("Sender Timeout"));

            iteratorInFault.next();
            role = (SOAPFaultRole) iteratorInFault.next();
            assertTrue("SOAP 1.1 :- Fault actor local name mismatch",
                    role.getLocalName().equals(
                            SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME));
            assertTrue("SOAP 1.1 :- Fault actor namespace uri mismatch",
                    role.getNamespace().getName().equals(
                            SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));
            assertTrue("SOAP 1.1 :- Actor value mismatch",
                    role.getText().equals(
                            "http://schemas.xmlsoap.org/soap/envelope/actor/ultimateReceiver"));

            iteratorInFault.next();
            detail = (SOAPFaultDetail) iteratorInFault.next();
            assertTrue("SOAP 1.1 :- Fault detail local name mismatch",
                    detail.getLocalName().equals(
                            SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME));
            assertTrue("SOAP 1.1 :- Fault detail namespace uri mismatch",
                    detail.getNamespace().getName().equals(
                            SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));
            assertTrue("SOAP 1.2 :- Text in detail mismatch",
                    detail.getText().equals("Details of error"));

            iteratorInDetail = detail.getChildren();

            iteratorInDetail.next();
            element1 = (OMElement) iteratorInDetail.next();
            assertTrue("SOAP 1.1 :- MaxTime element mismatch",
                    element1.getLocalName().equals("MaxTime"));
            assertTrue("SOAP 1.1 :- MaxTime element namespace mismatch",
                    element1.getNamespace().getName().equals(
                            "http:www.sample.org"));
            assertTrue("SOAP 1.1 :- Text value in MaxTime element mismatch",
                    element1.getText().equals("P5M"));

            attributeIterator = element1.getAttributes();
            attributeInMaxTime = (OMAttribute) attributeIterator.next();
            assertTrue("SOAP 1.1 :- Attribute local name mismatch",
                    attributeInMaxTime.getLocalName().equals("detail"));
            assertTrue("SOAP 1.1 :- Attribute namespace mismatch",
                    attributeInMaxTime.getNamespace().getName().equals(
                            "http:www.sample.org"));
            assertTrue("SOAP 1.1 :- Attribute value mismatch",
                    attributeInMaxTime.getValue().equals("This is only a test"));

            iteratorInDetail.next();
            element2 = (OMElement) iteratorInDetail.next();
            assertTrue("SOAP 1.1 :- AveTime element mismatch",
                    element2.getLocalName().equals("AveTime"));
            assertTrue("SOAP 1.1 :- AveTime element namespace mismatch",
                    element2.getNamespace().getName().equals(
                            "http:www.sample.org"));

            iteratorInAveTimeElement = element2.getChildren();

            iteratorInAveTimeElement.next();
            element21 = (OMElement) iteratorInAveTimeElement.next();
            assertTrue("SOAP 1.1 :- Time element mismatch",
                    element21.getLocalName().equals("Time"));
            assertTrue("SOAP 1.1 :- Time element namespace mismatch",
                    element21.getNamespace().getName().equals(
                            "http:www.sample.org"));
            assertTrue("SOAP 1.1 :- Text value in Time element mismatch",
                    element21.getText().equals("P3M"));

            iteratorInFault.next();
            OMElement testElement = (OMElement) iteratorInFault.next();
            assertTrue("SOAP 1.1 :- Test element mismatch",
                    testElement.getLocalName().equals("Test"));
            assertTrue("SOAP 1.1 :- Test element namespace mismatch",
                    testElement.getNamespace().getName().equals(
                            "http:www.Test.org"));

            OMElement childOfTestElement = testElement.getFirstElement();
            assertTrue("SOAP 1.1 :- Test element child local name mismatch",
                    childOfTestElement.getLocalName().equals("TestElement"));
            assertTrue("SOAP 1.1 :- Test element child namespace mismatch",
                    childOfTestElement.getNamespace().getName().equals(
                            "http:www.Test.org"));
            assertTrue("SOAP 1.1 :- Test element child value mismatch",
                    childOfTestElement.getText().equals("This is only a test"));

        } catch (XMLStreamException e) {
            e.printStackTrace();
            fail("Test failed. Reason -> " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed. Reason -> " + e.getMessage());

        }
    }
}
