package org.apache.axis.om.impl.streamwrapper;

import junit.framework.TestCase;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.impl.factory.OMLinkedListImplFactory;
import org.apache.axis.om.impl.serialize.SimpleOMSerializer;
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
 * Date: Nov 18, 2004
 * Time: 3:54:54 PM
 * 
 */
public class OmStAXBuilderTest extends TestCase{

    private static final String FILE_NAME = "src/test-resources/soap/soapmessage1.xml";
    private OMFactory factory =null;
    private OMStAXBuilder builder;
    private SimpleOMSerializer serilizer;
    protected void setUp() throws Exception {

        factory = new OMLinkedListImplFactory();
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(FILE_NAME));
        builder = new OMStAXBuilder(factory,reader);
        serilizer = new SimpleOMSerializer();
    }

    public void testStaxBuilder(){

        SOAPMessage message = builder.getSOAPMessage();
        assertNotNull(message);
        serilizer.serialize(message.getEnvelope(),System.out);


    }




}
