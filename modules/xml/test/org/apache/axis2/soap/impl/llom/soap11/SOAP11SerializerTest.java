package org.apache.axis2.soap.impl.llom.soap11;

import org.apache.axis2.om.OMOutput;
import org.apache.axis2.om.OMTestCase;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

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
 * author : Eran Chinthaka (chinthaka@apache.org)
 */

public class SOAP11SerializerTest extends OMTestCase {
    private OMOutput omOutput;

    public SOAP11SerializerTest(String testName) {
        super(testName);
    }

    protected StAXSOAPModelBuilder getOMBuilder(String fileName) throws Exception {
        return super.getOMBuilder(fileName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        soapEnvelope = (SOAPEnvelope) getOMBuilder("soap/soap11/soap11fault.xml").getDocumentElement();
        omOutput = new OMOutput(XMLOutputFactory.newInstance().
                                createXMLStreamWriter(System.out));
    }

    public void testSerialize() {
        try {
            soapEnvelope.serializeWithCache(omOutput);
            omOutput.flush();

            System.out.println("");
            System.out.println("=======================");
            soapEnvelope.serialize(omOutput);
            omOutput.flush();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

    }
}
