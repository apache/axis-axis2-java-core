package org.apache.axis.om;

import junit.framework.TestCase;
import org.apache.axis.om.util.TimeTester;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
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
 * Date: Oct 8, 2004
 * Time: 2:16:26 PM
 * 
 */
public class OMModleTest3 extends TestCase{

    private StreamingOMBuilder builder = null;
    private OMModel model=null;

    protected void setUp() throws Exception {
        super.setUp();
        XmlPullParserFactory xmlPullParserFactory = MXParserFactory.newInstance();
        xmlPullParserFactory.setNamespaceAware(true);
        XmlPullParser parser = xmlPullParserFactory.newPullParser();
        parser.setInput(new FileReader("test-resources/soap2.xml"));
        builder = new StreamingOMBuilder(parser);
        model = builder.getTableModel();
    }

    public void testChildren1(){
        //build the complete model
        while (!model.isComplete())
            model.proceed();

        OMDocument doc = model.getDocument();
        OMElement elt = doc.getDocumentElement();

        Iterator iter = elt.getChildren();
        while (iter.hasNext()){
            System.out.println("iter.next() = " + iter.next());
        }


        //=========================
        //dump the content of the table
        dumpModel("After testing");
    }

     private void dumpModel(String heading) {
        System.out.println();
        System.out.println("############## "+heading+" #########################");
        ((OMModelImpl)model).dump();
        System.out.println("############## end "+heading+" #########################");
    }

}
