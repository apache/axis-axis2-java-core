/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.interop.whitemesa;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.custommonkey.xmlunit.XMLTestCase;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

public class WhiteMesaIneterop extends XMLTestCase implements WhiteMesaConstants {

    protected void compareXML(SOAPEnvelope retEnv, String filePath)
            throws AxisFault {

        try {
            if (retEnv != null) {
                SOAPBody body = retEnv.getBody();
                if (!body.hasFault()) {
                    InputStream stream = Thread.currentThread()
                            .getContextClassLoader().getResourceAsStream(
                            filePath);

                    XMLStreamReader parser = StAXUtils
                            .createXMLStreamReader(stream);
                    OMXMLParserWrapper builder = new StAXSOAPModelBuilder(
                            parser, null);
                    SOAPEnvelope refEnv = (SOAPEnvelope)builder
                            .getDocumentElement();
                    String refXML = refEnv.toString();
                    String retXML = retEnv.toString();

                    assertXMLEqual(refXML, retXML);
                }
            }
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Validation for the echoString operaion according the the default wsdl A subclass should
     * overrid this if if works with a different wsdl.
     *
     * @param resultEnv
     */
    protected void assertR2DefaultEchoStringResult(SOAPEnvelope resultEnv) throws AxisFault {
        SOAPBody body = resultEnv.getBody();
        OMElement payload = body.getFirstElement();
        assertNotNull(payload);
        try {
            String xPathExpr = seperator + seperator + nsPrefix + colon + echoStringResponse +
                    seperator + ret + seperator + textNodeSelector;
            AXIOMXPath xpath = new AXIOMXPath(xPathExpr);
            addNamespaces(xpath);
            OMText textElem = (OMText)xpath.selectSingleNode(payload);
            assertNotNull(textElem);

            assertEquals(textElem.getText(), WhiteMesaConstants.ECHO_STRING);
        } catch (JaxenException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Validation for the EchoStringArray operaion according the the default wsdl A subclass should
     * overrid this if if works with a different wsdl.
     *
     * @param resultEnv
     */
    protected void assertR2DefaultEchoStringArrayResult(SOAPEnvelope resultEnv) throws AxisFault {
        SOAPBody body = resultEnv.getBody();
        OMElement payload = body.getFirstElement();
        assertNotNull(payload);
        try {
            String xPathExpr = seperator + seperator + nsPrefix + colon + echoStringArrayResponse +
                    seperator + ret + seperator + item;
            AXIOMXPath xpath = new AXIOMXPath(xPathExpr);
            addNamespaces(xpath);
            List itemElems = xpath.selectNodes(payload);
            assertNotNull(itemElems);
            assertEquals(itemElems.size(), 3);

            Iterator iter = itemElems.iterator();
            OMElement itemElem = (OMElement)iter.next();
            assertNotNull(itemElem);
            assertEquals(itemElem.getText(), WhiteMesaConstants.ECHO_STRING_ARR_1);
            itemElem = (OMElement)iter.next();
            assertNotNull(itemElem);
            assertEquals(itemElem.getText(), WhiteMesaConstants.ECHO_STRING_ARR_2);
            itemElem = (OMElement)iter.next();
            assertNotNull(itemElem);
            assertEquals(itemElem.getText(), WhiteMesaConstants.ECHO_STRING_ARR_3);

        } catch (JaxenException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Validation for the EchoInteger operaion according the the default wsdl A subclass should
     * overrid this if if works with a different wsdl.
     *
     * @param resultEnv
     */
    protected void assertR2DefaultEchoIntegerResult(SOAPEnvelope resultEnv) throws AxisFault {
        SOAPBody body = resultEnv.getBody();
        OMElement payload = body.getFirstElement();
        assertNotNull(payload);
        try {
            String xPathExpr = seperator + seperator + nsPrefix + colon + echoIntegerResponse +
                    seperator + ret + seperator + textNodeSelector;
            AXIOMXPath xpath = new AXIOMXPath(xPathExpr);
            addNamespaces(xpath);
            OMText textElem = (OMText)xpath.selectSingleNode(payload);
            assertNotNull(textElem);

            assertEquals(textElem.getText(), WhiteMesaConstants.ECHO_INTEGER);
        } catch (JaxenException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Validation for the EchoIntegerArray operaion according the the default wsdl A subclass should
     * overrid this if if works with a different wsdl.
     *
     * @param resultEnv
     */
    protected void assertR2DefaultEchoIntegerArrayResult(SOAPEnvelope resultEnv) throws AxisFault {
        SOAPBody body = resultEnv.getBody();
        OMElement payload = body.getFirstElement();
        assertNotNull(payload);
        try {
            String xPathExpr = seperator + seperator + nsPrefix + colon + echoIntegerArrayResponse +
                    seperator + ret + seperator + item;
            AXIOMXPath xpath = new AXIOMXPath(xPathExpr);
            addNamespaces(xpath);
            List itemElems = xpath.selectNodes(payload);
            assertNotNull(itemElems);
            assertEquals(itemElems.size(), 3);

            Iterator iter = itemElems.iterator();
            OMElement itemElem = (OMElement)iter.next();
            assertNotNull(itemElem);
            assertEquals(itemElem.getText(), WhiteMesaConstants.ECHO_INTEGER_ARR_1);
            itemElem = (OMElement)iter.next();
            assertNotNull(itemElem);
            assertEquals(itemElem.getText(), WhiteMesaConstants.ECHO_INTEGER_ARR_2);
            itemElem = (OMElement)iter.next();
            assertNotNull(itemElem);
            assertEquals(itemElem.getText(), WhiteMesaConstants.ECHO_INTEGER_ARR_3);

        } catch (JaxenException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Validation for the EchoFloat operaion according the the default wsdl A subclass should
     * overrid this if if works with a different wsdl.
     *
     * @param resultEnv
     */
    protected void assertR2DefaultEchoFloatResult(SOAPEnvelope resultEnv) throws AxisFault {
        SOAPBody body = resultEnv.getBody();
        OMElement payload = body.getFirstElement();
        assertNotNull(payload);
        try {
            String xPathExpr = seperator + seperator + nsPrefix + colon + echoFloatResponse +
                    seperator + ret + seperator + textNodeSelector;
            AXIOMXPath xpath = new AXIOMXPath(xPathExpr);
            addNamespaces(xpath);
            OMText textElem = (OMText)xpath.selectSingleNode(payload);
            assertNotNull(textElem);

            assertEquals(textElem.getText(), WhiteMesaConstants.ECHO_FLOAT);
        } catch (JaxenException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Validation for the EchoFloatArray operaion according the the default wsdl A subclass should
     * overrid this if if works with a different wsdl.
     *
     * @param resultEnv
     */
    protected void assertR2DefaultEchoFloatArrayResult(SOAPEnvelope resultEnv) throws AxisFault {
        SOAPBody body = resultEnv.getBody();
        OMElement payload = body.getFirstElement();
        assertNotNull(payload);
        try {
            String xPathExpr = seperator + seperator + nsPrefix + colon + echoFloatArrayResponse +
                    seperator + ret + seperator + item;
            AXIOMXPath xpath = new AXIOMXPath(xPathExpr);
            addNamespaces(xpath);
            List itemElems = xpath.selectNodes(payload);
            assertNotNull(itemElems);
            assertEquals(itemElems.size(), 3);

            Iterator iter = itemElems.iterator();
            OMElement itemElem = (OMElement)iter.next();
            assertNotNull(itemElem);
            assertEquals(itemElem.getText(), WhiteMesaConstants.ECHO_FLOAT_ARR_1);
            itemElem = (OMElement)iter.next();
            assertNotNull(itemElem);
            assertEquals(itemElem.getText(), WhiteMesaConstants.ECHO_FLOAT_ARR_2);
            itemElem = (OMElement)iter.next();
            assertNotNull(itemElem);
            assertEquals(itemElem.getText(), WhiteMesaConstants.ECHO_FLOAT_ARR_3);

        } catch (JaxenException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Validation for the EchoStruct operaion according the the default wsdl A subclass should
     * overrid this if if works with a different wsdl.
     *
     * @param resultEnv
     */
    protected void assertR2DefaultEchoStructResult(SOAPEnvelope resultEnv) throws AxisFault {
        SOAPBody body = resultEnv.getBody();
        OMElement payload = body.getFirstElement();
        assertNotNull(payload);
        try {
            String xPathExpr = seperator + seperator + nsPrefix + colon + echoStructResponse +
                    seperator + ret + seperator + varInt;
            AXIOMXPath xpath = new AXIOMXPath(xPathExpr);
            addNamespaces(xpath);
            OMText textElem = (OMText)xpath.selectSingleNode(payload);
            assertNotNull(textElem);
            assertEquals(textElem.getText(), WhiteMesaConstants.ECHO_STRUCT_INT);

            xPathExpr = seperator + seperator + nsPrefix + colon + echoFloatResponse + seperator +
                    ret + seperator + varFloat;
            xpath = new AXIOMXPath(xPathExpr);
            addNamespaces(xpath);
            textElem = (OMText)xpath.selectSingleNode(payload);
            assertNotNull(textElem);
            assertEquals(textElem.getText(), WhiteMesaConstants.ECHO_STRUCT_FLOAT);

            xPathExpr = seperator + seperator + nsPrefix + colon + echoFloatResponse + seperator +
                    ret + seperator + varFloat;
            xpath = new AXIOMXPath(xPathExpr);
            addNamespaces(xpath);
            textElem = (OMText)xpath.selectSingleNode(payload);
            assertNotNull(textElem);
            assertEquals(textElem.getText(), WhiteMesaConstants.ECHO_STRUCT_STRING);

        } catch (JaxenException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Validation for the EchoStructArray operaion according the the default wsdl A subclass should
     * overrid this if if works with a different wsdl.
     *
     * @param resultEnv
     */
    protected void assertR2DefaultEchoStructArrayResult(SOAPEnvelope resultEnv) throws AxisFault {
        SOAPBody body = resultEnv.getBody();
        OMElement payload = body.getFirstElement();
        assertNotNull(payload);
        try {
            String xPathExpr =
                    seperator + seperator + nsPrefix + colon + echoStructArrayResponse + seperator +
                            ret + seperator + item + seperator + varString + seperator +
                            textNodeSelector;

            AXIOMXPath xpath = new AXIOMXPath(xPathExpr);
            addNamespaces(xpath);
            List varStringElems = xpath.selectNodes(payload);
            assertEquals(varStringElems.size(), 3);
            OMText varStringElem = (OMText)varStringElems.get(0);
            assertNotNull(varStringElem);
            assertEquals(varStringElem.getText(), WhiteMesaConstants.ECHO_STRUCT_ARRAY_STR_1);
            varStringElem = (OMText)varStringElems.get(1);
            assertNotNull(varStringElem);
            assertEquals(varStringElem.getText(), WhiteMesaConstants.ECHO_STRUCT_ARRAY_STR_2);
            varStringElem = (OMText)varStringElems.get(2);
            assertNotNull(varStringElem);
            assertEquals(varStringElem.getText(), WhiteMesaConstants.ECHO_STRUCT_ARRAY_STR_3);


            xPathExpr =
                    seperator + seperator + nsPrefix + colon + echoStructArrayResponse + seperator +
                            ret + seperator + item + seperator + varInt + seperator +
                            textNodeSelector;

            xpath = new AXIOMXPath(xPathExpr);
            addNamespaces(xpath);
            varStringElems = xpath.selectNodes(payload);
            assertEquals(varStringElems.size(), 3);
            varStringElem = (OMText)varStringElems.get(0);
            assertNotNull(varStringElem);
            assertEquals(varStringElem.getText(), WhiteMesaConstants.ECHO_STRUCT_ARRAY_INT_1);
            varStringElem = (OMText)varStringElems.get(1);
            assertNotNull(varStringElem);
            assertEquals(varStringElem.getText(), WhiteMesaConstants.ECHO_STRUCT_ARRAY_INT_2);
            varStringElem = (OMText)varStringElems.get(2);
            assertNotNull(varStringElem);
            assertEquals(varStringElem.getText(), WhiteMesaConstants.ECHO_STRUCT_ARRAY_INT_3);

            xPathExpr =
                    seperator + seperator + nsPrefix + colon + echoStructArrayResponse + seperator +
                            ret + seperator + item + seperator + varFloat + seperator +
                            textNodeSelector;

            xpath = new AXIOMXPath(xPathExpr);
            addNamespaces(xpath);
            varStringElems = xpath.selectNodes(payload);
            assertEquals(varStringElems.size(), 3);
            varStringElem = (OMText)varStringElems.get(0);
            assertNotNull(varStringElem);
            assertEquals(varStringElem.getText(), WhiteMesaConstants.ECHO_STRUCT_ARRAY_FLOAT_1);
            varStringElem = (OMText)varStringElems.get(1);
            assertNotNull(varStringElem);
            assertEquals(varStringElem.getText(), WhiteMesaConstants.ECHO_STRUCT_ARRAY_FLOAT_2);
            varStringElem = (OMText)varStringElems.get(2);
            assertNotNull(varStringElem);
            assertEquals(varStringElem.getText(), WhiteMesaConstants.ECHO_STRUCT_ARRAY_FLOAT_3);
        } catch (JaxenException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Validation for the EchoVoid operaion according the the default wsdl A subclass should overrid
     * this if if works with a different wsdl.
     *
     * @param resultEnv
     */
    protected void assertR2DefaultEchoVoidResult(SOAPEnvelope resultEnv) throws AxisFault {
        SOAPBody body = resultEnv.getBody();
        OMElement echoVoidResponseElem =
                body.getFirstChildWithName(new QName(nsValue, echoVoidResponse));
        assertNotNull(echoVoidResponseElem);
    }

    /**
     * Validation for the EchoBase64 operaion according the the default wsdl A subclass should
     * overrid this if if works with a different wsdl.
     *
     * @param resultEnv
     */
    protected void assertR2DefaultEchoBase64Result(SOAPEnvelope resultEnv) throws AxisFault {
        SOAPBody body = resultEnv.getBody();
        OMElement payload = body.getFirstElement();
        assertNotNull(payload);
        try {
            String xPathExpr = seperator + seperator + nsPrefix + colon + echoBase64Response +
                    seperator + ret + seperator + textNodeSelector;
            AXIOMXPath xpath = new AXIOMXPath(xPathExpr);
            addNamespaces(xpath);
            OMText textElem = (OMText)xpath.selectSingleNode(payload);
            assertNotNull(textElem);

            assertEquals(textElem.getText(), WhiteMesaConstants.ECHO_BASE_64);
        } catch (JaxenException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Validation for the EchoHexBinary operaion according the the default wsdl A subclass should
     * overrid this if if works with a different wsdl.
     *
     * @param resultEnv
     */
    protected void assertR2DefaultEchoHexBinaryResult(SOAPEnvelope resultEnv) throws AxisFault {
        SOAPBody body = resultEnv.getBody();
        OMElement payload = body.getFirstElement();
        assertNotNull(payload);
        try {
            String xPathExpr = seperator + seperator + nsPrefix + colon + echoHexBinaryResponse +
                    seperator + ret + seperator + textNodeSelector;
            AXIOMXPath xpath = new AXIOMXPath(xPathExpr);
            addNamespaces(xpath);
            OMText textElem = (OMText)xpath.selectSingleNode(payload);
            assertNotNull(textElem);

            boolean equal = WhiteMesaConstants.ECHO_HEX_BINARY.equalsIgnoreCase(textElem.getText());
            assertTrue(equal);
        } catch (JaxenException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Validation for the EchoDate operaion according the the default wsdl A subclass should overrid
     * this if if works with a different wsdl.
     *
     * @param resultEnv
     */
    protected void assertR2DefaultEchoDateResult(SOAPEnvelope resultEnv) throws AxisFault {
        SOAPBody body = resultEnv.getBody();
        OMElement payload = body.getFirstElement();
        assertNotNull(payload);
        try {
            String xPathExpr = seperator + seperator + nsPrefix + colon + echoDateResponse +
                    seperator + ret + seperator + textNodeSelector;
            AXIOMXPath xpath = new AXIOMXPath(xPathExpr);
            addNamespaces(xpath);
            OMText textElem = (OMText)xpath.selectSingleNode(payload);
            assertNotNull(textElem);

            assertEquals(textElem.getText(), WhiteMesaConstants.ECHO_DATE);
        } catch (JaxenException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Validation for the EchoDecimal operaion according the the default wsdl A subclass should
     * overrid this if if works with a different wsdl.
     *
     * @param resultEnv
     */
    protected void assertR2DefaultEchoDecimalResult(SOAPEnvelope resultEnv) throws AxisFault {
        SOAPBody body = resultEnv.getBody();
        OMElement payload = body.getFirstElement();
        assertNotNull(payload);
        try {
            String xPathExpr = seperator + seperator + nsPrefix + colon + echoDecimalResponse +
                    seperator + ret + seperator + textNodeSelector;
            AXIOMXPath xpath = new AXIOMXPath(xPathExpr);
            addNamespaces(xpath);
            OMText textElem = (OMText)xpath.selectSingleNode(payload);
            assertNotNull(textElem);

            assertEquals(textElem.getText(), WhiteMesaConstants.ECHO_DECIMAL);
        } catch (JaxenException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Validation for the EchoBoolean operaion according the the default wsdl A subclass should
     * overrid this if if works with a different wsdl.
     *
     * @param resultEnv
     */
    protected void assertR2DefaultEchoBooleanResult(SOAPEnvelope resultEnv) throws AxisFault {
        SOAPBody body = resultEnv.getBody();
        OMElement payload = body.getFirstElement();
        assertNotNull(payload);
        try {
            String xPathExpr = seperator + seperator + nsPrefix + colon + echoBooleanResponse +
                    seperator + ret + seperator + textNodeSelector;
            AXIOMXPath xpath = new AXIOMXPath(xPathExpr);
            addNamespaces(xpath);
            OMText textElem = (OMText)xpath.selectSingleNode(payload);
            assertNotNull(textElem);

            assertEquals(textElem.getText(), WhiteMesaConstants.ECHO_BOOLEAN);
        } catch (JaxenException e) {
            throw AxisFault.makeFault(e);
        }
    }

    protected void assertValueIsInThePayload(SOAPEnvelope envelope, String value) {
        SOAPBody body = envelope.getBody();

        assertTrue(body.toString().indexOf(value) != -1);
    }

    private void addNamespaces(XPath xpath) {
        SimpleNamespaceContext nsCtx = new SimpleNamespaceContext();
        nsCtx.addNamespace(nsPrefix, nsValue);
        xpath.setNamespaceContext(nsCtx);
    }

}