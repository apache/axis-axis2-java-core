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

    private SOAPEnvelope envelope = null;

    public IteratorTester(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        envelope = new StAXSOAPModelBuilder(XMLInputFactory.newInstance().createXMLStreamReader(
                new FileReader(getTestResourceFile("soap/soapmessage1.xml")))).getOMEnvelope();
    }

    protected void tearDown() throws Exception {
        envelope = null;
    }

    public void testIterator(){
        OMElement elt = envelope;
        Iterator iter = elt.getChildren();

        while (iter.hasNext()) {
            assertNotNull(iter.next());//todo make this better. match this against a given XML
        }

    }


    /**
     *test the remove exception behavior
     */
    public void testIteratorRemove1(){

        OMElement elt = envelope;
        Iterator iter = elt.getChildren();

        //this is supposed to throw an illegal state exception
        try {
            iter.remove();
            assertTrue("remove should throw an exception",true);
        } catch (IllegalStateException e) {
            //ok. this is what should happen
        }

    }

    /**
     *test the remove exception behavior, consecutive remove calls
     */
    public void testIteratorRemove2(){

        OMElement elt = envelope;
        Iterator iter = elt.getChildren();

        if (iter.hasNext()){
            iter.next();
        }

        iter.remove();

        //this call must generate an exception
        try {
            iter.remove();
            assertTrue("calling remove twice without a call to next is prohibited",true);
        } catch (IllegalStateException e) {
            //ok if we come here :)
        }

    }

    /**
     * Remove all!
     */
    public void testIteratorRemove3(){

        OMElement elt = envelope;
        Iterator iter = elt.getChildren();

        while (iter.hasNext()){
            iter.next();
            iter.remove();
        }

        iter=elt.getChildren();

        if(iter.hasNext()){
            assertTrue("No children should remain after removing all!",false);
        }


    }
    /**
     *test whether the children count reduces.
     */

    public void testIteratorRemove4(){

        OMElement elt = envelope;
        Iterator iter = elt.getChildren();
        int firstChildrenCount = 0;
        int secondChildrenCount = 0;


        while (iter.hasNext()) {
            assertNotNull( (OMNode) iter.next());
            firstChildrenCount++;
        }

        //this should remove the last node
        iter.remove();

        //reloop and check the count
        //Note- here we should get a fresh iterator since there is no method to
        //reset the iterator
        iter = elt.getChildren(); //reset the iterator
        while (iter.hasNext()) {
            assertNotNull((OMNode)iter.next());
            secondChildrenCount++;
        }


        assertEquals("children count must reduce from 1",firstChildrenCount-1,secondChildrenCount);

    }












}
