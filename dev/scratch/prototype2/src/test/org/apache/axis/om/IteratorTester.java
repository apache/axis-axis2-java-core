package org.apache.axis.om;

import junit.framework.TestCase;
import org.apache.axis.impl.llom.builder.OMStAXBuilder;

import javax.xml.stream.XMLInputFactory;
import java.io.FileReader;
import java.util.Iterator;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Axis team
 * Date: Oct 11, 2004
 * Time: 12:34:15 PM
 * 
 */
public class IteratorTester extends TestCase{
    private static final String IN_FILE_NAME = "resources/soapmessage.xml";
    private static final String IN_FILE_NAME2 = "src/test-resources/soap/sample1.xml";
    OMEnvelope envelope = null;

    protected void setUp() throws Exception {
//        XmlPullParser parser= XmlPullParserFactory.newInstance().newPullParser();
//		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
//		parser.setInput();
//        ;
		envelope = new OMStAXBuilder(XMLInputFactory.newInstance().
                createXMLStreamReader(new FileReader(IN_FILE_NAME2))).getOMEnvelope();
    }

    public void testIterator(){
        OMElement elt = envelope;
        Iterator iter = elt.getChildren();

        while (iter.hasNext()) {
            OMNode o = (OMNode) iter.next();
            System.out.println("o = " + o);
            if (o!=null)
            System.out.println("value o " + o.getValue());
        }

    }















}
