package org.apache.axis.om;

import junit.framework.TestCase;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.mxp1.MXParserFactory;

import java.io.FileReader;
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
 *
 * @author Axis team
 * Date: Sep 28, 2004
 * Time: 10:07:33 PM
 * To change this template use Options | File Templates.
 */
public class OMModelTest extends TestCase{


    private StreamingOMBuilder builder = null;
    private OMModel model=null;

    protected void setUp() throws Exception {
        super.setUp();
        XmlPullParserFactory xmlPullParserFactory = MXParserFactory.newInstance();
        xmlPullParserFactory.setNamespaceAware(true);
        XmlPullParser parser = xmlPullParserFactory.newPullParser();
        parser.setInput(new FileReader("test-resources/soapmessage1.xml"));
        builder = new StreamingOMBuilder(parser);
        model = builder.getTableModel();
    }


    /**
     * Test the presence of the root elements
     */
    public void testRootElement(){
        OMDocument doc = model.getDocument();
        OMElement elt = doc.getDocumentElement();
        assertTrue(elt!=null);

    }

    /**
     * test the presence of children
     */
    public void testChildren(){
        OMDocument doc = model.getDocument();
        OMElement elt = doc.getDocumentElement();
        Iterator iter = elt.getChildren();
        Object obj ;

        dumpModel("before all");


        obj=iter.next();
        System.out.println("obj = " + obj);
        if (obj instanceof OMElement)
            System.out.println("name =" + ((OMElement)obj).getLocalName());
        if (obj instanceof OMText)
            System.out.println("text =" + ((OMText)obj).getValue());

        dumpModel("cycle 1");


        obj=iter.next();
        System.out.println("obj = " + obj);
        if (obj instanceof OMElement)
            System.out.println("name =" + ((OMElement)obj).getLocalName());
        if (obj instanceof OMText)
            System.out.println("text =" + ((OMText)obj).getValue());

        dumpModel("cycle 2");


    }

    /**
     * Test the presence of athe attributes
     * Just loop through the attributes to see whether they work properly
     */
    public void testAttributes(){
        OMDocument doc = model.getDocument();
        OMElement elt = doc.getDocumentElement();

        dumpModel("cycle 1");

        Iterator iter = elt.getAttributes();
        while(iter.hasNext()){
            OMAttribute x = (OMAttribute)iter.next();
            System.out.println("object =" + x);
            System.out.println("local name =" + x.getLocalName());
        }



    }
    /**
     * Test removal of attributes
     * Case 1 - the attribute is not the first attribute
     */
     public void testRemoveAttribute1(){
        OMDocument doc = model.getDocument();
        OMElement elt = doc.getDocumentElement();

        dumpModel("cycle 1");

        Iterator iter = elt.getAttributes();
        OMAttribute x = null;
        while(iter.hasNext()){
            x = (OMAttribute)iter.next();
            System.out.println("object =" + x);
            System.out.println("local name =" + x.getLocalName());
        }

        //now remove the attribute
        elt.removeAttribute(x);
        dumpModel("cycle 2");

        iter = elt.getAttributes();
        while(iter.hasNext()){
            x = (OMAttribute)iter.next();
            System.out.println("object =" + x);
            System.out.println("local name =" + x.getLocalName());
        }
    }

    /**
     * Test the removal of attributes
     * Case 2 -  the attribute is the first attribute
     */
    public void testRemoveAttribute2(){
        OMDocument doc = model.getDocument();
        OMElement elt = doc.getDocumentElement();

        dumpModel("cycle 1");

        OMAttribute x = elt.getFirstAttribute();
        elt.removeAttribute(x);

        dumpModel("cycle 2");

        Iterator iter = elt.getAttributes();
        while(iter.hasNext()){
            x = (OMAttribute)iter.next();
            System.out.println("object =" + x);
            System.out.println("local name =" + x.getLocalName());
        }
    }

    public void testProceeed(){

        //make the whole model
        while (!model.isComplete()){
            model.proceed();
        }

        dumpModel(" all ");

    }
    private void dumpModel(String heading) {
        System.out.println();
        System.out.println("############## "+heading+" #########################");
        ((OMModelImpl)model).dump();
        System.out.println("############## end "+heading+" #########################");
    }
}
