package org.apache.axis.om.builder;

import junit.framework.TestCase;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.apache.axis.om.*;
import org.apache.axis.om.impl.factory.OMLinkedListImplFactory;
import org.apache.axis.om.builder.dummy.DummyOutObject;
import org.apache.axis.om.builder.dummy.DummyOutObject;

import java.io.FileReader;
import java.io.IOException;
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
 * <p/>
 * User: Eran Chinthaka - Lanka Software Foundation
 * Date: Nov 19, 2004
 * Time: 3:54:03 PM
 */
public class ObjectToOMBuilderTest extends TestCase{

    OutObject outObject;
    ObjectToOMBuilder objectToOMBuilder;
    OMFactory omFactory;
    private OMElement element;

    protected void setUp() throws Exception {
        super.setUp();
        outObject = new DummyOutObject();
        omFactory = new OMLinkedListImplFactory();

        element = omFactory.createOMElement("Body", null);
        objectToOMBuilder = new ObjectToOMBuilder(element, outObject);

    }

    public void testBuilding(){
        System.out.println("element = " + element.isComplete());

        objectToOMBuilder.next();

        Iterator children = element.getChildren();
        while (children.hasNext()) {
            OMNode omNode = (OMNode) children.next();
            System.out.println("omNode = " + omNode.getValue());
        }

        System.out.println("element = " + element.isComplete());
    }


}
