package org.apache.axis.om;

import java.io.FileReader;
import java.util.Iterator;

import javax.xml.stream.XMLInputFactory;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.impl.llom.builder.StAXSOAPModelBuilder;

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

    SOAPEnvelope envelope = null;

    public IteratorTester(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
		envelope = new StAXSOAPModelBuilder(XMLInputFactory.newInstance().createXMLStreamReader(
                        new FileReader(getTestResourceFile("soap/sample1.xml")))).getOMEnvelope();
    }

    public void testIterator(){
        OMElement elt = envelope;
        Iterator iter = elt.getChildren();

        while (iter.hasNext()) {
            OMNode o = (OMNode) iter.next();
            assertNotNull(o);//todo make this better
        }

    }















}
