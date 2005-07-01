package org.apache.axis.om.impl.serializer;

import junit.framework.TestCase;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMOutput;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.SOAPFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

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
*
*
*/

public class NoNamespaceSerializerTest extends TestCase {

    private String xmlTextOne = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<soapenv:Body>\n" +
            "   <ns1:getBalance xmlns:ns1=\"http://localhost:8081/axis/services/BankPort/\">\n" +
            "      <accountNo href=\"#id0\"/>\n" +
            "   </ns1:getBalance>\n" +
            " </soapenv:Body></soapenv:Envelope>";
                                                                     
    private String xmlText2 = "<purchase-order xmlns=\"http://openuri.org/easypo\">\n" +
            "  <customer>\n" +
            "    <name>Gladys Kravitz</name>\n" +
            "    <address>Anytown, PA</address>\n" +
            "  </customer>\n" +
            "  <date>2005-03-06T14:06:12.697+06:00</date>\n" +
            "</purchase-order>";

    private String xmlTextTwo = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<soapenv:Body>\n" +
            "   <getBalance xmlns=\"http://localhost:8081/axis/services/BankPort/\">\n" +
            "      <accountNo href=\"#id0\"/>\n" +
            "   </getBalance>\n" +
            " </soapenv:Body></soapenv:Envelope>";

    private XMLStreamReader readerOne;
    private XMLStreamReader readerTwo;
    private OMOutput omOutput;

   // private OMXMLParserWrapper builder;
    // private File tempFile;

    private OMXMLParserWrapper builderOne;
    private OMXMLParserWrapper builderTwo;
    // private File tempFile;



    protected void setUp() throws Exception {
        readerOne = XMLInputFactory.newInstance().
                createXMLStreamReader(new InputStreamReader(new ByteArrayInputStream(xmlTextOne.getBytes())));
        readerTwo = XMLInputFactory.newInstance().
                createXMLStreamReader(new InputStreamReader(new ByteArrayInputStream(xmlTextTwo.getBytes())));
        omOutput = new OMOutput(XMLOutputFactory.newInstance().
                createXMLStreamWriter(System.out));
        builderOne = OMXMLBuilderFactory.createStAXSOAPModelBuilder(OMAbstractFactory.getSOAP11Factory(), readerOne);
        builderTwo = OMXMLBuilderFactory.createStAXSOAPModelBuilder(OMAbstractFactory.getSOAP11Factory(), readerTwo);
    }


//    public void testSerilizationWithCacheOff() throws Exception {
//        SOAPEnvelope env = (SOAPEnvelope) builderOne.getDocumentElement();
//        env.serializeWithCache(writer, false);
//        writer.flush();
//
//
//    }
//
//    public void testSerilizationWithCacheOn() throws Exception {
//        SOAPEnvelope env = (SOAPEnvelope) builderOne.getDocumentElement();
//        env.serializeWithCache(writer, true);
//        writer.flush();
//    }


    public void testSerilizationWithDefaultNamespaces() throws Exception {
        SOAPEnvelope env = (SOAPEnvelope) builderTwo.getDocumentElement();
        env.serializeWithCache(omOutput);
        OMElement balanceElement = env.getBody().getFirstElement();
        assertEquals("Deafualt namespace has not been set properly", balanceElement.getNamespace().getName(), "http://localhost:8081/axis/services/BankPort/");

        OMElement accountNo = balanceElement.getFirstElement();
        assertEquals("Deafualt namespace of children has not been set properly", accountNo.getNamespace().getName(), "http://localhost:8081/axis/services/BankPort/");

    }

    public void submitPurchaseOrderTest()
            throws Exception {
        SOAPFactory omFactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope env = omFactory.getDefaultEnvelope();
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createStAXOMBuilder(omFactory,XMLInputFactory.newInstance().
                createXMLStreamReader(new InputStreamReader(new ByteArrayInputStream(xmlText2.getBytes()))));
        env.getBody().addChild(builder.getDocumentElement());

        OMOutput omOutput =  new OMOutput(System.out,false);
        //env.getBody().addChild(builder.getDocumentElement());
        
        env.serializeWithCache(omOutput);
       // env.serializeWithCache(xmlStreamWriter, true);

        omOutput.flush();

    }
    public void testSerilizationWithCacheOn() throws Exception{
       SOAPEnvelope env = (SOAPEnvelope) builderOne.getDocumentElement();
       env.serializeWithCache(omOutput);
       omOutput.flush();
    }

     public void testSerilizationWithCacheOff() throws Exception{
       SOAPEnvelope env = (SOAPEnvelope) builderOne.getDocumentElement();
       env.serializeWithCache(omOutput);
       omOutput.flush();
    }
    }
