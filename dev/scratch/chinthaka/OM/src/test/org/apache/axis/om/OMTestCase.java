package org.apache.axis.om;

import junit.framework.TestCase;
import org.apache.axis.om.impl.OMXMLPullParserWrapper;
import org.apache.axis.om.soap.SOAPEnvelope;
import org.apache.axis.om.soap.SOAPMessage;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

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
 * <p/>
 * User: Eran Chinthaka - Lanka Software Foundation
 * Date: Nov 2, 2004
 * Time: 2:15:28 PM
 */
public class OMTestCase extends TestCase  {

    protected static final String IN_FILE_NAME = "resources/soapmessage.xml";
    protected OMXMLPullParserWrapper omXmlPullParserWrapper;

    protected SOAPMessage soapMessage;
    protected SOAPEnvelope soapEnvelope;


    protected void setUp() throws Exception {
        super.setUp();
        soapMessage = getOMBuilder().getSOAPMessage();
        soapEnvelope = soapMessage.getEnvelope();
    }

    protected OMXMLPullParserWrapper getOMBuilder() throws Exception {
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        parser.setInput(new FileReader(IN_FILE_NAME));
        omXmlPullParserWrapper = new OMXMLPullParserWrapper(parser);
        return omXmlPullParserWrapper;
    }


}
