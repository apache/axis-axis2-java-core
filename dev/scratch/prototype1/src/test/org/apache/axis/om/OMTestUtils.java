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
import java.util.Iterator;


import junit.framework.TestCase;

import org.apache.axis.om.impl.streamwrapper.OMXPPWrapper;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class OMTestUtils {
    public static OMXPPWrapper getOMBuilder(String file) throws Exception {
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        parser.setInput(new FileReader(file));
        OMXPPWrapper omXmlPullParserWrapper = new OMXPPWrapper(parser);
        return omXmlPullParserWrapper;
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
            TestCase.assertNotNull("once the has next is not null, the " +                "eleemnt should not be null",ele);
            
            if(ele instanceof OMElement){
                walkThrough((OMElement)ele);
            }    
        }
    
    } 

}
