package org.apache.axis.om;

import java.io.FileReader;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.impl.llom.wrapper.OMXPPWrapper;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

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
public class OMTestCase extends AbstractTestCase {

    protected static final String IN_FILE_NAME = "soap/soapmessage.xml";
    protected OMXPPWrapper omXmlPullParserWrapper;
    protected OMFactory ombuilderFactory;

    protected SOAPEnvelope soapEnvelope;

    public OMTestCase(String testName) {
        super(testName);
        ombuilderFactory = OMFactory.newInstance();
    }


    protected void setUp() throws Exception {
        super.setUp();
        soapEnvelope = getOMBuilder().getOMEnvelope();
    }

    protected OMXPPWrapper getOMBuilder() throws Exception {
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        parser.setInput(new FileReader(getTestResourceFile(IN_FILE_NAME)));
        omXmlPullParserWrapper = new OMXPPWrapper(parser);
        return omXmlPullParserWrapper;
    }


}
