package org.apache.axis.om.impl.streamwrapper;

import junit.framework.TestCase;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.impl.factory.OMLinkedListImplFactory;
import org.apache.axis.om.impl.serialize.SimpleOMSerializer;
import org.apache.axis.om.soap.SOAPMessage;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
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
 *
 * @author Axis team
 * Date: Nov 19, 2004
 * Time: 5:23:01 PM
 * 
 */
public class OMStaxStreamingWrapperTest extends TestCase {

    private static final String IN_FILE_NAME2 = "src/test-resources/soap/soapmessage1.xml";
    private SOAPMessage document = null;
    private SimpleOMSerializer serilizer;
    private OMStAXBuilder omStAXBuilder;

    protected void setUp() throws Exception {
        XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().
                createXMLStreamReader(new FileReader(IN_FILE_NAME2));
        OMFactory factory = new OMLinkedListImplFactory();
        omStAXBuilder = new OMStAXBuilder(factory, xmlStreamReader);
        document = omStAXBuilder.getSOAPMessage();
        serilizer = new SimpleOMSerializer();
    }


    public void testWrapperFullOM() {
        System.out.println(" # -----------------------Full OM------------------------------- #");
        assertNotNull(document);
        //this serializing will cause the OM to fully build!
        serilizer.serialize(document.getEnvelope(), System.out);

        //now the OM is fully created. Create the wrapper and see
        OMStAXWrapper wrapper = new OMStAXWrapper(omStAXBuilder, document.getEnvelope());

        while (wrapper.hasNext()) {
            int event = wrapper.next();
            assertTrue(event>0);
        }

        System.out.println(" # -----------------------Full OM end ------------------------------- #");
    }

    public void testWrapperHalfOM() {
        System.out.println(" # -----------------------Half OM------------------------------- #");
        assertNotNull(document);

        //now the OM is not fully created. Create the wrapper and see
        OMStAXWrapper wrapper = new OMStAXWrapper(omStAXBuilder, document.getEnvelope());

        while (wrapper.hasNext()) {
            int event = wrapper.next();
            assertTrue(event>0);
//            System.out.println("returnEvent = " + getEventString(event));
        }

        System.out.println(" # -----------------------Half OM end------------------------------- #");
    }
    public void testWrapperHalfOMWithCacheOff() {
        System.out.println(" # -----------------------Half OM with cache off------------------------------- #");
        assertNotNull(document);

        //now the OM is not fully created. Create the wrapper and see
        OMStAXWrapper wrapper = new OMStAXWrapper(omStAXBuilder, document.getEnvelope());
        //set the switching allowed flag
        wrapper.setAllowSwitching(true);
        while (wrapper.hasNext()) {
            int event = wrapper.next();
            assertTrue(event>0);
//            System.out.println("returnEvent = " + getEventString(event));
        }
        System.out.println(" # -----------------------Half OM with cache off end------------------------------- #");
    }

    private String getEventString(int event){
        String outStr = "";
        switch (event){
            case XMLStreamConstants.START_ELEMENT:
                outStr = "START_ELEMENT";
                break;
            case XMLStreamConstants.END_ELEMENT:
                outStr = "END_ELEMENT";
                break;
            case XMLStreamConstants.CHARACTERS:
                outStr = "char";
                break;
            default:outStr = event+"";

        }
        return outStr;
    }

}
