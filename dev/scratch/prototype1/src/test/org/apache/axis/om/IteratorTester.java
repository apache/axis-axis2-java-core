package org.apache.axis.om;

import junit.framework.TestCase;
import org.apache.axis.om.impl.streamwrapper.OMStAXBuilder;
import org.apache.axis.om.soap.SOAPMessage;
import org.apache.axis.AbstractTestCase;

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
public class IteratorTester extends AbstractTestCase{

    SOAPMessage message = null;

    public IteratorTester(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
		message = new OMStAXBuilder(XMLInputFactory.newInstance().
                createXMLStreamReader(new FileReader(getTestResourceFile("soap/sample1.xml")))).getSOAPMessage();
    }

    public void testIterator(){
        OMElement elt = message.getEnvelope();
        Iterator iter = elt.getChildren();

        while (iter.hasNext()) {
            OMNode o = (OMNode) iter.next();
            //Todo need to improve this test
            assertNotNull(o);

        }

    }















}
