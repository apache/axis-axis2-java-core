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

package org.apache.axis2.json;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.custommonkey.xmlunit.XMLTestCase;

import static com.google.common.truth.Truth.assertAbout;
import static org.apache.axiom.truth.xml.XMLTruth.xml;

import java.io.StringReader;

public class JSONDataSourceTest extends XMLTestCase {

    public void testMappedSerialize() throws Exception {
        JSONDataSource source = getMappedDataSource(
                "{\"mapping\":{\"inner\":[{\"first\":\"test string one\"},\"test string two\"],\"name\":\"foo\"}}");
        assertAbout(xml())
                .that(OMAbstractFactory.getOMFactory().createOMElement(source))
                .hasSameContentAs(
                        "<mapping><inner><first>test string one</first></inner><inner>test string two</inner><name>foo</name></mapping>");
    }

    public void testBadgerfishSerialize() throws Exception {
        JSONBadgerfishDataSource source = getBadgerfishDataSource(
                "{\"p\":{\"@xmlns\":{\"bb\":\"http://other.nsb\",\"aa\":\"http://other.ns\",\"$\":\"http://def.ns\"},\"sam\":{\"$\":\"555\", \"@att\":\"lets\"}}}");
        assertAbout(xml())
                .that(OMAbstractFactory.getOMFactory().createOMElement(source))
                .hasSameContentAs(
                        "<p xmlns=\"http://def.ns\" xmlns:bb=\"http://other.nsb\" xmlns:aa=\"http://other.ns\"><sam att=\"lets\">555</sam></p>");
    }

    private JSONBadgerfishDataSource getBadgerfishDataSource(String jsonString) {
        return new JSONBadgerfishDataSource(new StringReader(jsonString));
    }

    private JSONDataSource getMappedDataSource(String jsonString) {
        MessageContext messageContext = new MessageContext();
        messageContext.setAxisService(new AxisService());
        return new JSONDataSource(new StringReader(jsonString), messageContext);
    }
}
