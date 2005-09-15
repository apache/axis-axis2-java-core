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

package org.apache.axis2.om.impl.llom;

import org.apache.axis2.attachments.ByteArrayDataSource;
import org.apache.axis2.om.AbstractTestCase;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;

import javax.activation.DataHandler;
import java.io.File;
import java.io.FileOutputStream;

/**
 * @author <a href="mailto:thilina@opensource.lk">Thilina Gunarathne </a>
 */

public class OMOutputTest extends AbstractTestCase {

    /**
     * @param testName
     */
    public OMOutputTest(String testName) {
        super(testName);
    }

    String outFileName;

    String outBase64FileName;

    OMElement envelope;

    File outMTOMFile;

    File outBase64File;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        Object object;
        DataHandler dataHandler;

        outFileName = "mtom/OMSerializeMTOMOut.txt";
        outBase64FileName = "mtom/OMSerializeBase64Out.xml";
        outMTOMFile = getTestResourceFile(outFileName);
        outBase64File = getTestResourceFile(outBase64FileName);

        OMNamespaceImpl soap = new OMNamespaceImpl(
                "http://schemas.xmlsoap.org/soap/envelope/", "soap");
        envelope = new OMElementImpl("Envelope", soap);
        OMElement body = new OMElementImpl("Body", soap);

        OMNamespaceImpl dataName = new OMNamespaceImpl(
                "http://www.example.org/stuff", "m");
        OMElement data = new OMElementImpl("data", dataName);

        OMNamespaceImpl mime = new OMNamespaceImpl(
                "http://www.w3.org/2003/06/xmlmime", "m");

        OMElement text = new OMElementImpl("name", dataName);
        OMAttribute cType1 = new OMAttributeImpl("contentType", mime,
                "text/plain");
        text.addAttribute(cType1);
        byte[] byteArray = new byte[]{13, 56, 65, 32, 12, 12, 7, -3, -2, -1,
                                      98};
        dataHandler = new DataHandler(new ByteArrayDataSource(byteArray));
        OMTextImpl textData = new OMTextImpl(dataHandler, false);

        envelope.addChild(body);
        body.addChild(data);
        data.addChild(text);
        text.addChild(textData);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        if (this.outMTOMFile.exists()) {
            this.outMTOMFile.delete();
        }
        if (this.outBase64File.exists()) {
            this.outBase64File.delete();
        }
    }

    public void testComplete() throws Exception {

        org.apache.axis2.om.impl.OMOutputImpl mtomOutput = new org.apache.axis2.om.impl.OMOutputImpl(new FileOutputStream(outMTOMFile),
                true);
        org.apache.axis2.om.impl.OMOutputImpl baseOutput = new org.apache.axis2.om.impl.OMOutputImpl(new FileOutputStream(outBase64File),
                false);

        envelope.serialize(baseOutput);
        baseOutput.flush();

        envelope.serialize(mtomOutput);
        mtomOutput.flush();
    }
}