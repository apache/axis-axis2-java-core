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
package org.apache.axis2.databinding.utils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import junit.framework.TestCase;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;

/**
 * Multiref resolution deep-clones the referenced element and memoises the result
 * only after resolving finishes, so a reference leading back on itself recurses
 * until the stack ends. A reference graph with no cycle can still double at every
 * level, which is the same shape as an entity-expansion bomb on XML no parser limit
 * objects to. Both arrive on the anonymous RPC/POJO receiver path.
 */
public class MultirefCycleTest extends TestCase {

    private OMElement parse(String xml) {
        return OMXMLBuilderFactory.createOMBuilder(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))
                .getDocumentElement();
    }

    /** A refers to B, B back to A. Previously a StackOverflowError. */
    public void testASelfReferencingCycleIsRefused() throws Exception {
        OMElement body = parse(
                "<body>"
                + "  <multiref id='a'><next href='#b'/></multiref>"
                + "  <multiref id='b'><next href='#a'/></multiref>"
                + "</body>");
        MultirefHelper helper = new MultirefHelper(body);
        try {
            helper.processOMElementRef("a");
            fail("a cyclic multiref must be refused, not recursed");
        } catch (AxisFault expected) {
            assertTrue("should name the cycle, was: " + expected.getMessage(),
                    expected.getMessage().contains("Cyclic multiref"));
        }
    }

    /** A reference straight back to its own id. */
    public void testADirectSelfReferenceIsRefused() throws Exception {
        OMElement body = parse(
                "<body><multiref id='a'><next href='#a'/></multiref></body>");
        MultirefHelper helper = new MultirefHelper(body);
        try {
            helper.processOMElementRef("a");
            fail("a self-reference must be refused");
        } catch (AxisFault expected) {
            assertTrue(expected.getMessage().contains("Cyclic multiref"));
        }
    }

    /** An ordinary chain still resolves: the guard must not break multiref. */
    public void testAnAcyclicChainStillResolves() throws Exception {
        OMElement body = parse(
                "<body>"
                + "  <multiref id='a'><next href='#b'/></multiref>"
                + "  <multiref id='b'><leaf>value</leaf></multiref>"
                + "</body>");
        OMElement resolved = new MultirefHelper(body).processOMElementRef("a");
        assertNotNull(resolved);
        assertTrue("the referenced content should have been inlined",
                resolved.toString().contains("value"));
    }

    /**
     * The doubling shape: no cycle anywhere, so cycle detection alone would let it
     * through. Each level references the next twice, so expansion is exponential in
     * the depth and the budget is what stops it.
     */
    public void testADoublingReferenceGraphIsRefused() throws Exception {
        StringBuilder xml = new StringBuilder("<body>");
        int levels = 40;
        for (int i = 0; i < levels; i++) {
            xml.append("<multiref id='n").append(i).append("'>")
               .append("<a href='#n").append(i + 1).append("'/>")
               .append("<b href='#n").append(i + 1).append("'/>")
               .append("</multiref>");
        }
        xml.append("<multiref id='n").append(levels).append("'><leaf>x</leaf></multiref>");
        xml.append("</body>");

        MultirefHelper helper = new MultirefHelper(parse(xml.toString()));
        try {
            helper.processOMElementRef("n0");
            fail("an exponentially expanding reference graph must be refused");
        } catch (AxisFault expected) {
            assertTrue("should name the budget, was: " + expected.getMessage(),
                    expected.getMessage().contains("multiref references"));
        }
    }

    /** The static href path expands in place and then walks what it added. */
    public void testTheStaticHrefPathRefusesACycle() throws Exception {
        String envelope =
                "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'>"
                + "<soapenv:Body>"
                + "  <op><arg href='#a'/></op>"
                + "  <multiref id='a'><next href='#a'/></multiref>"
                + "</soapenv:Body></soapenv:Envelope>";
        SOAPEnvelope soapEnvelope = (SOAPEnvelope) OMXMLBuilderFactory.createSOAPModelBuilder(
                new ByteArrayInputStream(envelope.getBytes(StandardCharsets.UTF_8)), null)
                .getDocumentElement();
        try {
            MultirefHelper.processHrefAttributes(soapEnvelope);
            fail("unbounded href expansion must be refused");
        } catch (AxisFault expected) {
            assertTrue("should name depth or the budget, was: " + expected.getMessage(),
                    expected.getMessage().contains("href references"));
        } catch (StackOverflowError e) {
            fail("expansion should be refused by budget, not end in a stack overflow");
        }
    }

    /** Keeps the factory import honest and the ordinary static path working. */
    public void testTheStaticHrefPathStillResolvesAnOrdinaryReference() throws Exception {
        String envelope =
                "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'>"
                + "<soapenv:Body>"
                + "  <op><arg href='#a'/></op>"
                + "  <multiref id='a'><name>real</name></multiref>"
                + "</soapenv:Body></soapenv:Envelope>";
        SOAPEnvelope soapEnvelope = (SOAPEnvelope) OMXMLBuilderFactory.createSOAPModelBuilder(
                new ByteArrayInputStream(envelope.getBytes(StandardCharsets.UTF_8)), null)
                .getDocumentElement();
        MultirefHelper.processHrefAttributes(soapEnvelope);
        assertTrue("the reference should have been inlined",
                soapEnvelope.getBody().toString().contains("real"));
        assertNotNull(OMAbstractFactory.getOMFactory());
    }
}
