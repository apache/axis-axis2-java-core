package org.apache.axis.om.impl.serializer;

import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axis.om.impl.llom.builder.StAXSOAPModelBuilder;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

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
public class NoNamespaceSerializerTest extends TestCase{

    private String xmlText = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<soapenv:Body>\n" +
            "   <ns1:getBalance xmlns:ns1=\"http://localhost:8081/axis/services/BankPort/\">\n" +
            "      <accountNo href=\"#id0\"/>\n" +
            "   </ns1:getBalance>\n" +
            " </soapenv:Body></soapenv:Envelope>";

    private XMLStreamReader reader;
    private XMLStreamWriter writer;
    private OMXMLParserWrapper builder;
   // private File tempFile;


    protected void setUp() throws Exception {
        reader = XMLInputFactory.newInstance().
                createXMLStreamReader(new InputStreamReader(new ByteArrayInputStream(xmlText.getBytes())));
        writer = XMLOutputFactory.newInstance().
                createXMLStreamWriter(System.out);
        builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(OMFactory.newInstance(), reader);
    }

    public void testSerilizationWithCacheOff() throws Exception{
       SOAPEnvelope env = (SOAPEnvelope) builder.getDocumentElement();
       env.serialize(writer,false);
       writer.flush();


    }

    public void testSerilizationWithCacheOn() throws Exception{
       SOAPEnvelope env = (SOAPEnvelope) builder.getDocumentElement();
       env.serialize(writer,true);
       writer.flush();


    }


}
