/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.om;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axis.AbstractTestCase;
import org.w3c.dom.Document;

/**
 * @version $Rev: $ $Date: $
 */

public class CompareOMWithDOMTest extends AbstractTestCase{
    /**
     * @param testName
     */
    public CompareOMWithDOMTest(String testName) {
        super(testName);
    }
    
    public void testSecuritySample2() throws OMException, Exception{
        File dir = new File(testResourceDir,"soap");
        File[] files = dir.listFiles();
        if(files != null){
            for(int i = 0;i<files.length;i++){
                if(files[i].isFile() && files[i].getName().endsWith(".xml")){
                    SOAPEnvelope soapEnvelope = (SOAPEnvelope) OMTestUtils.getOMBuilder(
                            files[i]).getDocumentElement();
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(true);
                    DocumentBuilder builder = dbf.newDocumentBuilder();
                    Document doc = builder.parse(files[i].getAbsolutePath());
                    OMTestUtils.compare(doc.getDocumentElement(),soapEnvelope);
                }
            }
        
        }
    }
}
