/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.io.FileReader;
import java.io.File;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.axis.impl.llom.wrapper.OMXPPWrapper;
import org.apache.axis.impl.llom.builder.OMStAXBuilder;
import org.apache.axis.impl.llom.factory.OMLinkedListImplFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;

/**
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class OMTestUtils {

    public static OMXMLParserWrapper getOMBuilder(File file) throws Exception {

        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(file));
        OMStAXBuilder builder =   new OMStAXBuilder(new OMLinkedListImplFactory(),parser);

        return builder;
    }
    
    public static void walkThrough(OMElement omEle){
        Iterator attibIt = omEle.getAttributes();
        while(attibIt.hasNext()){
            TestCase.assertNotNull("once the has next is not null, the " +
                            "eleemnt should not be null",attibIt.next());
        }
        Iterator it = omEle.getChildren();
        while(it.hasNext()){
            OMNode ele = (OMNode)it.next();
            TestCase.assertNotNull("once the has next is not null, the " +
                "eleemnt should not be null",ele);
            
            if(ele instanceof OMElement){
                walkThrough((OMElement)ele);
            }    
        }
    
    }
    
    public static void compare(Element ele,OMElement omele) throws Exception{
        if(ele == null && omele == null){
            return;
        }else if(ele != null && omele != null){

            TestCase.assertTrue(ele.getLocalName().equals(omele.getLocalName()));
            TestCase.assertTrue(ele.getNamespaceURI().equals(omele.getNamespace().getValue()));
            
            Iterator it = omele.getChildren();
            
            NodeList list = ele.getChildNodes();
            for(int i = 0;i<list.getLength();i++){
                Node node = list.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE){
                    TestCase.assertTrue(it.hasNext());
                    OMNode tempOmNode = (OMNode)it.next();
                    while(tempOmNode.getType() != OMNode.ELEMENT_NODE){
                        TestCase.assertTrue(it.hasNext());
                        tempOmNode = (OMNode)it.next();
                    }
                    compare((Element)node,(OMElement)tempOmNode);
                }
            }
        }else{
               throw new Exception("One is null");
        }
    }

}
