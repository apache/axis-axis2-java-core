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

import junit.framework.TestCase;
import org.apache.axis.om.impl.SOAPMessageImpl;
import org.apache.axis.om.impl.streamwrapper.OMXPPWrapper;
import org.apache.axis.om.soap.SOAPMessage;
import org.apache.axis.AbstractTestCase;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * This test case tests the basic expectations of the engine from the OM.
 * @author Srinath Perera (hemapani@opensource.lk)
 */
public class OMTest extends AbstractTestCase{
    SOAPMessage message;

    public OMTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        File file = getTestResourceFile("soap/sample1.xml");
        FileInputStream in = new FileInputStream(file);
        
        XmlPullParserFactory pf = XmlPullParserFactory.newInstance();
        pf.setNamespaceAware(true);
        XmlPullParser  parser = pf.newPullParser();
        parser.setInput(new InputStreamReader(in));
        
        OMXMLParserWrapper parserWrapper = new OMXPPWrapper(parser);
        message = parserWrapper.getSOAPMessage();
    }

    
    /**
     * Sometime the hasNext() in the childeren iterator is true yet the next() is null
     */

    public void testNullInChilderen(){
        isNullChildrenThere(message.getEnvelope());
    }
    
    /**
     * the message is completly namesapce qulified so all the OMElements got to have namespace values not null
     *
     */
    public void test4MissingNamespaces(){
        isNameSpacesMissing(message.getEnvelope());
    }
    
    public void isNullChildrenThere(OMElement omeleent){
        Iterator it = omeleent.getChildren();
        while(it.hasNext()){
            OMNode node = (OMNode)it.next();
            assertNotNull(node);
            if(node.getType() == OMNode.ELEMENT_NODE){
                isNullChildrenThere((OMElement)node);
            }
        }
    }

    public void isNameSpacesMissing(OMElement omeleent){
        OMNamespace omns = omeleent.getNamespace();
        assertNotNull(omns);
        assertNotNull(omns.getValue());
        Iterator it = omeleent.getChildren();
        while(it.hasNext()){
            OMNode node = (OMNode)it.next();
            
            if(node != null && node.getType() == OMNode.ELEMENT_NODE){
                isNameSpacesMissing((OMElement)node);
            }
        }
    }
}
