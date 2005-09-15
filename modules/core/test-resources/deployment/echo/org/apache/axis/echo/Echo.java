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

package org.apache.axis2.echo;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.llom.factory.OMXMLBuilderFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;

public class Echo {

    public Echo() {
    }
    public OMElement viewVersion(OMElement omEle) {
        File aFile = new File("E:\\TestCase\\Soap.txt");
        FileInputStream inputFile = null; //Place to store the input stream reference
        try {
            inputFile = new FileInputStream(aFile);
            XMLStreamReader xmlr =
                    null;

            xmlr = XMLInputFactory.newInstance().createXMLStreamReader(inputFile);
            OMXMLParserWrapper builder =
                    OMXMLBuilderFactory.createStAXOMBuilder(OMAbstractFactory.getOMFactory(), xmlr);
            OMElement doc = builder.getDocumentElement();
            doc.build();
            return doc;
        }catch(Exception e)  {
            return null;
        }

    }

}