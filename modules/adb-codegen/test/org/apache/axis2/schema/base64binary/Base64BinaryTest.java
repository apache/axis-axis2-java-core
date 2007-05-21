/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.apache.axis2.schema.base64binary;

import org.w3.www._2005._05.xmlmime.*;
import org.w3.www._2005._05.xmlmime.HexBinary;
import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.databinding.types.*;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;


/**
 * Author: amila
 * Date: May 19, 2007
 */
public class Base64BinaryTest extends TestCase {

    public void testBase64Binary(){
        TestBase64Binary testBase64Binary = new TestBase64Binary();
        Base64Binary base64Binary = new Base64Binary();
        testBase64Binary.setTestBase64Binary(base64Binary);

        String testString = "new test string";

        DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(testString.getBytes()));
        base64Binary.setBase64Binary(dataHandler);
        ContentType_type0 contentType_type0 = new ContentType_type0();
        contentType_type0.setContentType_type0("test content type");
        base64Binary.setContentType(contentType_type0);

        OMElement omElement = testBase64Binary.getOMElement(TestBase64Binary.MY_QNAME, OMAbstractFactory.getOMFactory());

        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestBase64Binary result = TestBase64Binary.Factory.parse(xmlReader);
            DataHandler resultDataHandler = result.getTestBase64Binary().getBase64Binary();
            byte[] bytes = new byte[128];
            int length = resultDataHandler.getInputStream().read(bytes);
            String resultString = new String(bytes,0,length);
            assertEquals(resultString,testString);
            assertEquals(result.getTestBase64Binary().getContentType().getContentType_type0(),"test content type");
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    public void testHexBinary(){
        TestHexBinary testHexBinary = new TestHexBinary();
        HexBinary hexBinary = new HexBinary();
        testHexBinary.setTestHexBinary(hexBinary);

        String testString = "ab";

        org.apache.axis2.databinding.types.HexBinary adbHexBinary =
                new  org.apache.axis2.databinding.types.HexBinary(testString);

        hexBinary.setHexBinary(adbHexBinary);
        ContentType_type0 contentType_type0 = new ContentType_type0();
        contentType_type0.setContentType_type0("test content type");
        hexBinary.setContentType(contentType_type0);

        OMElement omElement = testHexBinary.getOMElement(TestBase64Binary.MY_QNAME, OMAbstractFactory.getOMFactory());

        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestHexBinary result = TestHexBinary.Factory.parse(xmlReader);
            assertEquals(result.getTestHexBinary().getHexBinary().toString(),testString);
            assertEquals(result.getTestHexBinary().getContentType().getContentType_type0(),"test content type");
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }


}
