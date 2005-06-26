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

package org.apache.axis.soap;

import junit.framework.TestCase;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.AbstractTestCase;
import org.apache.axis.om.OMFactory;
import org.apache.axis.soap.impl.llom.builder.StAXSOAPModelBuilder;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class SOAPTestCase extends AbstractTestCase {
    protected SOAPFactory soap11Factory;
    protected SOAPFactory soap12Factory;
    protected OMFactory omFactory;

    protected SOAPEnvelope soap11Envelope;
    protected SOAPEnvelope soap12Envelope;

    protected SOAPEnvelope soap11EnvelopeWithParser;
    protected SOAPEnvelope soap12EnvelopeWithParser;

    protected static final String SOAP11_FILE_NAME = "soap/soap11/soap11message.xml";
    protected static final String SOAP12_FILE_NAME = "soap/soap12message.xml";

    /**
     * @param testName
     */
    public SOAPTestCase(String testName) {
        super(testName);
        soap11Factory = OMAbstractFactory.getSOAP11Factory();
        soap12Factory = OMAbstractFactory.getSOAP12Factory();
        omFactory = OMAbstractFactory.getOMFactory();
    }

    protected void setUp() throws Exception {
        super.setUp();

        soap11Envelope = soap11Factory.createSOAPEnvelope();
        soap12Envelope = soap12Factory.createSOAPEnvelope();

        soap11EnvelopeWithParser = (SOAPEnvelope)this.getSOAPBuilder(SOAP11_FILE_NAME).getDocumentElement();
        soap12EnvelopeWithParser = (SOAPEnvelope)this.getSOAPBuilder(SOAP12_FILE_NAME).getDocumentElement();
    }

    protected StAXSOAPModelBuilder getSOAPBuilder(String fileName) {
        XMLStreamReader parser = null;
        try {
            parser = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(getTestResourceFile(fileName)));
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new StAXSOAPModelBuilder(parser);
    }

}
