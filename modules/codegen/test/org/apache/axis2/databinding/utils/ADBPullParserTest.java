package org.apache.axis2.databinding.utils;

import junit.framework.TestCase;
import org.apache.axis2.databinding.ADBBean;
import org.apache.axis2.databinding.ADBNameValuePair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;

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
 *
 * @author : Eran Chinthaka (chinthaka@apache.org)
 */

public class ADBPullParserTest extends TestCase {

    protected Log log = LogFactory.getLog(getClass());


    protected void setUp() throws Exception {

    }

    public void testSimpleArrayList() {
        try {

            String exptectedXML = "<Project><Name>Axis2</Name><Releases><FirstRelease>0.90</FirstRelease>" +
                    "<SecondRelease>0.91</SecondRelease><ThirdRelease>0.92</ThirdRelease></Releases>" +
                    "<Releases><FirstRelease>0.90</FirstRelease><SecondRelease>0.91</SecondRelease>" +
                    "<ThirdRelease>0.92</ThirdRelease></Releases><Organization>Apache</Organization>" +
                    "</Project>";

            ADBNameValuePair adbNameValuePair;
            ArrayList propertyList = new ArrayList();
            propertyList.add(new ADBNameValuePair("Name", "Axis2"));
            propertyList.add(new DummyADBBean());
            propertyList.add(new DummyADBBean());
            propertyList.add(new ADBNameValuePair("Organization", "Apache"));

            QName projectQName = new QName("Project");
            XMLStreamReader pullParser = ADBPullParser.createPullParser(propertyList, projectQName);
//            while (pullParser.hasNext()) {
//                int eventCode = pullParser.next();
//                System.out.println(eventCode + ":" + getEventString(eventCode));
//            }

            StringBuffer buff = new StringBuffer();
            while (pullParser.hasNext()) {
                int eventCode = pullParser.next();

                switch (eventCode) {
                    case XMLStreamConstants.START_ELEMENT :
                        buff.append("<");
                        buff.append(pullParser.getLocalName());
                        buff.append(">");
                        break;
                    case XMLStreamConstants.CHARACTERS :
                        buff.append(pullParser.getText());
                        break;
                    case XMLStreamConstants.END_ELEMENT :
                        buff.append("</");
                        buff.append(pullParser.getLocalName());
                        buff.append(">");
                        break;
                    default:
                        System.out.println("No Other event can be trown here");
                }
            }

            assertEquals(exptectedXML, buff.toString());
        } catch (XMLStreamException e) {
            log.error("Parser Error " + e);
        }

    }

    private String getEventString(int eventCode) {
        String event = "";

        switch (eventCode) {
            case 1 :
                event = "START_ELEMENT";
                break;
            case 2 :
                event = "END_ELEMENT";
                break;
            case 3 :
                event = "PROCESSING_INSTRUCTION";
                break;
            case 4 :
                event = "CHARACTERS";
                break;
            case 5 :
                event = "COMMENT";
                break;
            case 6 :
                event = "SPACE";
                break;
            case 7 :
                event = "START_DOCUMENT";
                break;
            case 8 :
                event = "END_DOCUMENT";
                break;
            case 9 :
                event = "ENTITY_REFERENCE";
                break;
            case 10 :
                event = "ATTRIBUTE";
                break;
            case 11 :
                event = "DTD";
                break;
            case 12 :
                event = "CDATA";
                break;
            case 13 :
                event = "NAMESPACE";
                break;
            case 14 :
                event = "NOTATION_DECLARATION";
                break;
            case 15 :
                event = "ENTITY_DECLARATION";
                break;
        }
        return event;
    }

    public class DummyADBBean implements ADBBean {

        public XMLStreamReader getPullParser() {
            ADBNameValuePair adbNameValuePair;
            ArrayList propertyList = new ArrayList();
            propertyList.add(new ADBNameValuePair("FirstRelease", "0.90"));
            propertyList.add(new ADBNameValuePair("SecondRelease", "0.91"));
            propertyList.add(new ADBNameValuePair("ThirdRelease", "0.92"));

            QName releasesQName = new QName("Releases");
            return ADBPullParser.createPullParser(propertyList, releasesQName);
        }
    }
}
