package org.apache.axis.om;

import junit.framework.TestCase;
import org.apache.axis.om.impl.OMNavigator;
import org.apache.axis.om.impl.factory.OMLinkedListImplFactory;
import org.apache.axis.om.impl.serialize.SimpleOMSerializer;
import org.apache.axis.om.impl.streamwrapper.OMStAXBuilder;
import org.apache.axis.om.soap.SOAPMessage;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.FileReader;

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
 */
public class OMnavigatorTest extends TestCase{
    private static final String IN_FILE_NAME2 = "resources/soapmessage1.xml";
    private SOAPMessage document = null;
    private SimpleOMSerializer serilizer;
    private OMStAXBuilder builder;

    protected void setUp() throws Exception {
        XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().
                createXMLStreamReader(new FileReader(IN_FILE_NAME2));
        OMFactory factory = new OMLinkedListImplFactory();
        builder = new OMStAXBuilder(factory,xmlStreamReader);
        document = builder.getSOAPMessage();
        serilizer = new SimpleOMSerializer();
    }


    public void testnavigatorFullyBuilt(){
        System.out.println(" #######  Testing fully built OM tree ########");
        assertNotNull(document);
        serilizer.serialize(document.getEnvelope(),System.out);

        //now the OM is fully created
        OMNavigator navigator = new OMNavigator(document.getEnvelope());
        OMNode node=null;
        while(navigator.isNavigable()){
            node = navigator.next();
            System.out.println("node = " + node);
            System.out.println("node.getValue() = " + node.getValue());

        }

    }

    public void testnavigatorHalfBuilt(){
        System.out.println(" #######  Testing partially built OM tree ########");
        assertNotNull(document);

        //now the OM is not fully created
        OMNavigator navigator = new OMNavigator(document.getEnvelope());
        OMNode node=null;

        while(navigator.isNavigable()){
            node = navigator.next();
            System.out.println("node = " + node);
            System.out.println("node.getValue() = " + node.getValue());

        }

    }
    public void testnavigatorHalfBuiltStep(){
        System.out.println(" #######  Testing partially built OM tree With Stepping########");
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

            System.out.println("node = " + node);
            System.out.println("node.getValue() = " + node.getValue());

        }

    }


}
