package org.apache.axis.om;

import java.io.FileReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.axis.impl.llom.OMNavigator;
import org.apache.axis.impl.llom.builder.OMStAXBuilder;
import org.apache.axis.impl.llom.factory.OMLinkedListImplFactory;
import org.apache.axis.impl.llom.serialize.SimpleOMSerializer;

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
public class OMNavigatorTest extends TestCase{
    private static final String IN_FILE_NAME2 = "src/test-resources/soap/soapmessage1.xml";
    private OMEnvelope envelope = null;
    private SimpleOMSerializer serilizer;
    private OMStAXBuilder builder;

    protected void setUp() throws Exception {
        XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().
                createXMLStreamReader(new FileReader(IN_FILE_NAME2));
        OMFactory factory = new OMLinkedListImplFactory();
        builder = new OMStAXBuilder(factory,xmlStreamReader);
        envelope = builder.getOMEnvelope();
        serilizer = new SimpleOMSerializer();
    }


    public void testnavigatorFullyBuilt(){
        System.out.println(" #######  Testing fully built OM tree ########");
        assertNotNull(envelope);
        serilizer.serialize(envelope,System.out);

        //now the OM is fully created
        OMNavigator navigator = new OMNavigator(envelope);
        OMNode node=null;
        while(navigator.isNavigable()){
            node = navigator.next();

            assertNotNull(node);

            System.out.println("node = " + node);
            System.out.println("node.getValue() = " + node.getValue());

        }

    }

    public void testnavigatorHalfBuilt(){
        System.out.println(" #######  Testing partially built OM tree ########");
        assertNotNull(envelope);

        //now the OM is not fully created
        OMNavigator navigator = new OMNavigator(envelope);
        OMNode node=null;

        while(navigator.isNavigable()){
            node = navigator.next();

            assertNotNull(node);

            System.out.println("node = " + node);
            System.out.println("node.getValue() = " + node.getValue());

        }

    }
    public void testnavigatorHalfBuiltStep(){
        System.out.println(" #######  Testing partially built OM tree With Stepping########");
        assertNotNull(envelope);

        //now the OM is not fully created
        OMNavigator navigator = new OMNavigator(envelope);
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

            System.out.println("node = " + node);
            System.out.println("node.getValue() = " + node.getValue());

        }

    }


}
