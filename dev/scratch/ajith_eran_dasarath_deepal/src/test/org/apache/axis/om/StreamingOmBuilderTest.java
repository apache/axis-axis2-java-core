package org.apache.axis.om;

import org.w3c.dom.Document;
import org.xmlpull.mxp1.MXParserFactory;
import org.xmlpull.v1.XmlPullParser;

import java.io.FileInputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

/**
 *Copyright 2001-2004 The Apache Software Foundation.
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
public class StreamingOmBuilderTest extends TestCase{


    public void testSampleOne(){

        try {
            //Create the builder
            XmlPullParser pullparser = MXParserFactory.newInstance().newPullParser();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            
            pullparser.setInput(new InputStreamReader(cl.getResourceAsStream("testFile.xml")));
            StreamingOMBuilder builder = new StreamingOMBuilder(pullparser);

            Document doc = builder.getDocument();
            doc.getDocumentElement();

            ((OMTableModel)doc).dumpTablesToConsole();


            //no assertions yet :)


        } catch (Exception e) {
            e.printStackTrace();
        }


    }



}
