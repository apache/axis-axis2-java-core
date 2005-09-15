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

package org.apache.axis2.om;

import org.apache.axis2.soap.SOAPEnvelope;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * @version $Rev: $ $Date: $
 */
public class CompareOMWithDOMTest extends AbstractTestCase {
    /**
     * @param testName
     */
    public CompareOMWithDOMTest(String testName) {
        super(testName);
    }

    public void testAllMessagesInSOAP() throws OMException, Exception {
        File dir = new File(testResourceDir, "soap");
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                System.out.println("Processing files:" + files[i].getAbsolutePath());
                if (files[i].isFile() && files[i].getName().endsWith(".xml") && !files[i].getName().startsWith("wrong")) {
                    SOAPEnvelope soapEnvelope = (SOAPEnvelope) OMTestUtils.getOMBuilder(
                            files[i])
                            .getDocumentElement();
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(true);
                    DocumentBuilder builder = dbf.newDocumentBuilder();
                    Document doc = builder.parse(files[i].getAbsolutePath());
                    OMTestUtils.compare(doc.getDocumentElement(),
                            soapEnvelope);
                }
            }

        }
    }
}
