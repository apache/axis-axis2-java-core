package org.apache.axis.om;

import junit.framework.TestCase;
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
 * Date: Sep 30, 2004
 * Time: 10:05:56 PM
 */
public class PullGeneratorTester extends TestCase{

    OMModel model;

    protected void setUp() throws Exception {
        super.setUp();
        XmlPullParser parser = MXParserFactory.newInstance().newPullParser();
        parser.setInput(new FileReader("test-resources/soapmessage1.xml"));
        StreamingOMBuilder builder = new StreamingOMBuilder(parser);
        model = builder.getTableModel();
    }


    /**
     * Generate the pull events directly from the stream with no caching
     */
    public void testFullStream(){
        System.out.println("------------ Testing full stream ------------------");
        System.out.println("------------ Model dump ------------------");
        ((OMModelImpl)model).dump();
        System.out.println("------------------- end of model dump----------------------------");
        PullEventGenerator  pg = new PullEventGenerator(model);
        int event =XmlPullParser.START_DOCUMENT;
        while(event!=XmlPullParser.END_DOCUMENT){
            event=pg.generatePullEvent();
            System.out.println("Event = "+event);
        }
    }

    /**
     * parse the document fully and generate the pull events
     */
    public void testFullObjectModel(){

        while(!model.isComplete())
            model.proceed();
        System.out.println("------------ Testing full model ------------------");
        System.out.println("------------ Model dump ------------------");
        ((OMModelImpl)model).dump();
        System.out.println("------------------- end of model dump----------------------------");
        PullEventGenerator  pg = new PullEventGenerator(model);

        int event =XmlPullParser.START_DOCUMENT;
        while(event!=XmlPullParser.END_DOCUMENT){
            event=pg.generatePullEvent();
            System.out.println("Event = "+event);
        }
    }


    /**
     * Parse the document halfway an generate the pull events
     */
     public void testPartialObjectModel(){

         //this forces the document to partially build
        Iterator it = model.getDocument().getDocumentElement().getChildren();
         it.next();
         it.next();

         int i=0;

        System.out.println("------------ Testing full model ------------------");
        System.out.println("------------ Model dump ------------------");
        ((OMModelImpl)model).dump();
        System.out.println("------------------- end of model dump----------------------------");
        PullEventGenerator  pg = new PullEventGenerator(model);
        int event =XmlPullParser.START_DOCUMENT;
        while(event!=XmlPullParser.END_DOCUMENT){
            event=pg.generatePullEvent();
            System.out.println(i++ +" Event = "+event);
        }
    }
}
