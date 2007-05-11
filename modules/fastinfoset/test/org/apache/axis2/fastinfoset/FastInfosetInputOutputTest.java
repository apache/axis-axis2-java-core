/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis2.fastinfoset;

import com.sun.xml.fastinfoset.stax.StAXDocumentParser;
import com.sun.xml.fastinfoset.stax.StAXDocumentSerializer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.custommonkey.xmlunit.XMLTestCase;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author : Eran Chinthaka (echintha@cs.indiana.edu)
 * @Date : May 11, 2007
 */
public class FastInfosetInputOutputTest extends XMLTestCase {

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
//    public void testInputOutput() {
//        String inputFile = "maven.xml";
//        File outputFile = new File("output.xml");
//        File tempFile = new File("test.bin");
//
//        try {
//            // first let's read the xml document in to Axiom
//            OMElement element = new StAXOMBuilder(inputFile).getDocumentElement();
//
//            // output it using binary xml outputter
//            XMLStreamWriter streamWriter = new StAXDocumentSerializer(new FileOutputStream(tempFile));
//            element.serializeAndConsume(streamWriter);
//
//            // now let's read the binary file in to Axiom
//            XMLStreamReader streamReader = new StAXDocumentParser(new FileInputStream(tempFile));
//            StAXBuilder builder = new StAXSOAPModelBuilder(streamReader);
//            builder.getDocumentElement().serialize(new FileWriter(outputFile));
//
//            // let's see this is the same that we fed in to this test initially
//            assertXMLEqual(new FileReader(inputFile), new FileReader(outputFile));
//
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//            fail();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            fail();
//        } catch (IOException e) {
//            e.printStackTrace();
//            fail();
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//            fail();
//        } catch (SAXException e) {
//            e.printStackTrace();
//            fail();
//        } finally {
//            if (outputFile.exists()) outputFile.delete();
//            if (tempFile.exists()) tempFile.delete();
//        }
//    }
}
