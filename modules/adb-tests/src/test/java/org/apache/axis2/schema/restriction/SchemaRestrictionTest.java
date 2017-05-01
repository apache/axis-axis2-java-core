/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.schema.restriction;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

import junit.framework.TestCase;

public class SchemaRestrictionTest extends TestCase {

    public static String ERROR_MSG = "Input values do not follow defined XSD restrictions";

    public void testLimitedStringGetOMElement() throws Exception {
        LimitedString limitedString = new LimitedString();
        limitedString.setString("ab");
        OMElement omElement = limitedString.getOMElement(LimitedStringE.MY_QNAME,
                OMAbstractFactory.getSOAP11Factory());
    }

    public void testLimitedStringGetOMElement2() throws Exception {
        Rating rating = new Rating();
        rating.setString("abc");
        OMElement omElement = rating.getOMElement(LimitedStringE.MY_QNAME,
                OMAbstractFactory.getSOAP11Factory());
    }

    public void testLimitedStringParse1() throws Exception {
        LimitedString limitedString = new LimitedString();
        limitedString.setString("ab");
        OMElement omElement = limitedString.getOMElement(LimitedStringE.MY_QNAME,
                OMAbstractFactory.getOMFactory());
        LimitedString.Factory.parse(omElement.getXMLStreamReader());
    }

    public void testLimitedStringParse2() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName(
                "http://apache.org/axis2/schema/restriction", "LimitedString"));
        element.setText("a");
        try {
            LimitedStringE.Factory.parse(element.getXMLStreamReader());
            fail("This should throw RuntimeException");
        } catch (RuntimeException e) {
            assertEquals(toString(e), ERROR_MSG, e.getMessage());

        }
    }

    public void testLimitedStringParse3() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName(
                "http://apache.org/axis2/schema/restriction", "LimitedString"));
        element.setText("abcde");
        try {
            LimitedString.Factory.parse(element.getXMLStreamReader());
            fail("This should throw RuntimeException");
        } catch (RuntimeException e) {
            assertEquals(toString(e), ERROR_MSG, e.getMessage());

        }
    }

    public void testLimitedStringParse4() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName(
                "http://apache.org/axis2/schema/restriction", "LimitedString"));
        element.setText("abx");
        try {
            LimitedString.Factory.parse(element.getXMLStreamReader());
            fail("This should throw RuntimeException");
        } catch (RuntimeException e) {
            assertEquals(toString(e), ERROR_MSG, e.getMessage());

        }
    }

    public void testLimitedStringParse5() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName(
                "http://apache.org/axis2/schema/restriction", "LimitedString"));
        element.setText("");
        try {
            LimitedString.Factory.parse(element.getXMLStreamReader());
            fail("This should throw RuntimeException");
        } catch (RuntimeException e) {
            assertEquals(toString(e), ERROR_MSG, e.getMessage());

        }
    }

    public void testLimitedStringParse6() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName(
                "http://apache.org/axis2/schema/restriction", "LimitedString"));
        element.setText("ab34");
        try {
            LimitedString.Factory.parse(element.getXMLStreamReader());
            fail("This should throw RuntimeException");
        } catch (RuntimeException e) {
            assertEquals(toString(e), ERROR_MSG, e.getMessage());

        }
    }

    public void testRatingParse1() throws Exception {
        Rating rating = new Rating();
        rating.setString("ab");
        OMElement omElement = rating.getOMElement(LimitedStringE.MY_QNAME,
                OMAbstractFactory.getOMFactory());
        LimitedString.Factory.parse(omElement.getXMLStreamReader());
    }

    public void testRatingParse2() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName(
                "http://apache.org/axis2/schema/restriction", "Rating"));
        element.setText("a");
        try {
            Rating.Factory.parse(element.getXMLStreamReader());
            fail("This should throw RuntimeException");
        } catch (RuntimeException e) {
            assertEquals(toString(e), ERROR_MSG, e.getMessage());

        }
    }

    public void testRatingParse3() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName(
                "http://apache.org/axis2/schema/restriction", "Rating"));
        element.setText("abcde$");
        try {
            Rating.Factory.parse(element.getXMLStreamReader());
            fail("This should throw RuntimeException");
        } catch (RuntimeException e) {
            assertEquals(toString(e), ERROR_MSG, e.getMessage());

        }
    }

    public void testRatingParse4() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName(
                "http://apache.org/axis2/schema/restriction", "Rating"));
        element.setText("ab45");
        try {
            Rating.Factory.parse(element.getXMLStreamReader());
            fail("This should throw RuntimeException");
        } catch (RuntimeException e) {
            assertEquals(toString(e), ERROR_MSG, e.getMessage());

        }
    }

    public void testRatingParse5() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName(
                "http://apache.org/axis2/schema/restriction", "Rating"));
        element.setText("");
        try {
            Rating.Factory.parse(element.getXMLStreamReader());
            fail("This should throw RuntimeException");
        } catch (RuntimeException e) {
            assertEquals(toString(e), ERROR_MSG, e.getMessage());

        }
    }

    public void testRatingParse6() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(new QName(
                "http://apache.org/axis2/schema/restriction", "Rating"));
        element.setText("ab34");
        try {
            Rating.Factory.parse(element.getXMLStreamReader());
            fail("This should throw RuntimeException");
        } catch (RuntimeException e) {
            assertEquals(toString(e), ERROR_MSG, e.getMessage());

        }
    }

    private String toString(RuntimeException e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

}
