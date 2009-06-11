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

/**
 * 
 */
package org.apache.axis2.jaxws.misc;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.message.databinding.JAXBUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.TreeSet;

/**
 * Tests Namespace to Package Algorithm
 *
 */
public class JAXBContextTest extends AbstractTestCase {

    public static Test suite() {
        return getTestSetup(new TestSuite(JAXBContextTest.class));
    }

    /**
     * Test basic functionality of JAXBUtils pooling
     * @throws Exception
     */
    public void test01() throws JAXBException {
    	// Get a JAXBContext
    	TreeSet<String> context1 = new TreeSet<String>();
    	context1.add("org.test.addnumbers");
    	context1.add("org.test.anytype");

    	JAXBContext jaxbContext1 = JAXBUtils.getJAXBContext(context1);

    	// Assert that the JAXBContext was found and the context contains the two valid packages
    	assertTrue(jaxbContext1 != null);
    	assertTrue(context1.contains("org.test.addnumbers"));
    	assertTrue(context1.contains("org.test.anytype"));

    	// Repeat with the same packages
    	TreeSet<String> context2 = new TreeSet<String>();
    	context2.add("org.test.addnumbers");
    	context2.add("org.test.anytype");

    	JAXBContext jaxbContext2 = JAXBUtils.getJAXBContext(context2);

    	// The following assertion is probably true,but GC may have wiped out the weak reference
    	//assertTrue(jaxbContext2 == jaxbContext1);
    	assertTrue(jaxbContext2 != null);
    	assertTrue(jaxbContext2.toString().equals(jaxbContext1.toString()));
    	assertTrue(context2.contains("org.test.addnumbers"));
    	assertTrue(context2.contains("org.test.anytype"));

    	// Repeat with the same packages + an invalid package
    	TreeSet<String> context3 = new TreeSet<String>();
    	context3.add("org.test.addnumbers");
    	context3.add("org.test.anytype");
    	context3.add("my.grandma.loves.jaxws");

    	JAXBContext jaxbContext3 = JAXBUtils.getJAXBContext(context3);

    	// The following assertion is probably true,but GC may have wiped out the weak reference
    	//assertTrue(jaxbContext3 == jaxbContext1);
    	assertTrue(jaxbContext3 != null);
    	assertTrue(jaxbContext1.toString().equals(jaxbContext1.toString()));
    	assertTrue(context3.contains("org.test.addnumbers"));
    	assertTrue(context3.contains("org.test.anytype")); 
    	// The invalid package should now be retained...this is due
    	// a minor semantic change to avoid this side effect.
    	assertTrue(context3.contains("my.grandma.loves.jaxws"));  

    	// Repeat with a subset of packages
    	TreeSet<String> context4 = new TreeSet<String>();
    	context4.add("org.test.addnumbers");


    	JAXBContext jaxbContext4 = JAXBUtils.getJAXBContext(context4);

    	assertTrue(jaxbContext4 != null);
    	assertTrue(jaxbContext4 != jaxbContext3);
    	assertTrue(context4.contains("org.test.addnumbers"));
    }
}