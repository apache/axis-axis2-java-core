package org.apache.axis.om.builder;

import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.axis.impl.llom.builder.ObjectToOMBuilder;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OutObject;
import org.apache.axis.om.builder.dummy.DummyOutObject;

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
        omFactory = OMFactory.newInstance();

        element = omFactory.createOMElement("Body", null);
        objectToOMBuilder = new ObjectToOMBuilder(element, outObject);

    }

    public void testBuilding(){


        objectToOMBuilder.next();

        Iterator children = element.getChildren();
        while (children.hasNext()) {
            OMNode omNode = (OMNode) children.next();
            assertNotNull(omNode);
        }


    }


}
