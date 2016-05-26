/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.fastinfoset;

import com.sun.xml.fastinfoset.stax.StAXDocumentParser;
import com.sun.xml.fastinfoset.stax.StAXDocumentSerializer;

import org.apache.axiom.blob.Blobs;
import org.apache.axiom.blob.MemoryBlob;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.junit.Test;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import static com.google.common.truth.Truth.assertAbout;
import static org.apache.axiom.truth.xml.XMLTruth.xml;

import java.io.FileInputStream;
import java.io.OutputStream;

public class FastInfosetInputOutputTest {

    /**
     * This is to test how fast infoset interoperate with Axiom.
     * This is how this test is organized.
     * <pre>
     *      de-ser(wstx)        ser(fast-info)             de-ser(fast-info)       ser(wstx)
     * XML  -------->     Axiom     ------>    binary file -------------->   Axiom ---------> XML
     * </pre>
     * <p/>
     * Then the initial XML file and the last XML will be compared to see whether they are the same.
     */
    @Test
    public void testInputOutput() throws Exception {
        String inputFile = "pom.xml";

        // first let's read the xml document in to Axiom
        OMElement element = OMXMLBuilderFactory.createOMBuilder(
                new FileInputStream(inputFile)).getDocumentElement();

        // output it using binary xml outputter
        MemoryBlob blob = Blobs.createMemoryBlob();
        OutputStream out = blob.getOutputStream();
        XMLStreamWriter streamWriter = new StAXDocumentSerializer(out);
        streamWriter.writeStartDocument();
        element.serialize(streamWriter);
        streamWriter.writeEndDocument();
        out.close();

        // now let's read the binary file in to Axiom
        XMLStreamReader streamReader = new StAXDocumentParser(blob.getInputStream());
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createStAXOMBuilder(streamReader);

        assertAbout(xml()).that(builder.getDocumentElement()).hasSameContentAs(element);
    }
}
