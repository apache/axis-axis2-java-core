package org.apache.axis.om;

import junit.framework.TestCase;
import org.apache.axis.om.impl.OMNavigator;
import org.apache.axis.om.impl.factory.OMLinkedListImplFactory;
import org.apache.axis.om.impl.serialize.SimpleOMSerializer;
import org.apache.axis.om.impl.streamwrapper.OMStAXBuilder;
import org.apache.axis.om.soap.SOAPMessage;
import org.apache.axis.AbstractTestCase;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;

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
 * Date: Nov 19, 2004
 * Time: 4:35:04 PM
 *
 * Todo - These test classes are supposed to be modified further to support exact XML's
 */
public class OMnavigatorTest extends AbstractTestCase{

    private SOAPMessage document = null;
    private SimpleOMSerializer serilizer;
    private OMStAXBuilder builder;

    public OMnavigatorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().
                createXMLStreamReader(new FileReader(getTestResourceFile("soap/soapmessage1.xml")));
        OMFactory factory = new OMLinkedListImplFactory();
        builder = new OMStAXBuilder(factory,xmlStreamReader);
        document = builder.getSOAPMessage();
        serilizer = new SimpleOMSerializer();
    }


    public void testnavigatorFullyBuilt(){

        assertNotNull(document);
        try {
            serilizer.serialize(document.getEnvelope(), new FileOutputStream(new File("temp.xml")));
        } catch (FileNotFoundException e) {
            assertFalse(true);
        }

        //now the OM is fully created
        OMNavigator navigator = new OMNavigator(document.getEnvelope());
        OMNode node=null;
        while(navigator.isNavigable()){
            node = navigator.next();
            assertNotNull(node);

        }

    }

    public void testnavigatorHalfBuilt(){

        assertNotNull(document);
         //now the OM is not fully created
        OMNavigator navigator = new OMNavigator(document.getEnvelope());
        OMNode node=null;

        while(navigator.isNavigable()){
            node = navigator.next();
            assertNotNull(node);

        }

    }
    public void testnavigatorHalfBuiltStep(){
        assertNotNull(document);

        //now the OM is not fully created
        OMNavigator navigator = new OMNavigator(document.getEnvelope());
        OMNode node=null;

        while(!navigator.isCompleted()){
            if (navigator.isNavigable()){
                node = navigator.next();
            }else{
                builder.next();
                navigator.step();
                node=navigator.next();
            }

            assertNotNull(node);

        }

    }


}
