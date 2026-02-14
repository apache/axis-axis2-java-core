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
package org.apache.axis2.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.stream.StreamSource;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

public class SchemaValidationHandlerTest {

    @Test
    public void testAppendRefHintForCvcComplexType322() {
        SAXException ex = new SAXException(
                "cvc-complex-type.3.2.2: Attribute 'contentType' is not allowed to appear in element 'foo'");
        String hint = SchemaValidationHandler.appendRefHint(ex);
        assertThat(hint).contains("xs:attribute ref=");
        assertThat(hint).contains("xmime:contentType");
        assertThat(hint).contains("not imported or could not be resolved");
    }

    @Test
    public void testAppendRefHintReturnsEmptyForOtherErrors() {
        SAXException ex = new SAXException("cvc-type.3.1.3: some other validation error");
        String hint = SchemaValidationHandler.appendRefHint(ex);
        assertThat(hint).isEmpty();
    }

    @Test
    public void testAppendRefHintHandlesNullMessage() {
        SAXException ex = new SAXException((String) null);
        String hint = SchemaValidationHandler.appendRefHint(ex);
        assertThat(hint).isEmpty();
    }

    /**
     * Integration test: invoke the handler with a SOAP message containing an
     * attribute not declared in the schema, triggering cvc-complex-type.3.2.2,
     * and verify the AxisFault message includes the diagnostic ref= hint.
     */
    @Test
    public void testInvokeProducesRefHintForUnexpectedAttribute() throws Exception {
        // Schema that defines <note> with child elements only — no attributes allowed
        String xsd =
                "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'"
                + " targetNamespace='http://example.com/test'"
                + " xmlns:tns='http://example.com/test'"
                + " elementFormDefault='qualified'>"
                + "  <xs:element name='note'>"
                + "    <xs:complexType>"
                + "      <xs:sequence>"
                + "        <xs:element name='to' type='xs:string'/>"
                + "      </xs:sequence>"
                + "    </xs:complexType>"
                + "  </xs:element>"
                + "</xs:schema>";

        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(new StreamSource(
                new ByteArrayInputStream(xsd.getBytes(StandardCharsets.UTF_8))));

        // AxisService with the schema
        AxisService service = new AxisService("TestService");
        service.addSchema(schema);

        // MessageContext
        ConfigurationContext configCtx = new ConfigurationContext(new AxisConfiguration());
        MessageContext msgCtx = configCtx.createMessageContext();
        msgCtx.setAxisService(service);

        // SOAP envelope whose body element has an attribute the schema doesn't allow
        SOAPFactory sf = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = sf.createSOAPEnvelope();
        SOAPBody body = sf.createSOAPBody(envelope);

        OMNamespace tns = sf.createOMNamespace("http://example.com/test", "tns");
        OMElement note = sf.createOMElement("note", tns);

        // Add the required child so the only error is the unexpected attribute
        OMElement to = sf.createOMElement("to", tns);
        to.setText("Alice");
        note.addChild(to);

        // Add an attribute that the schema does not declare — triggers cvc-complex-type.3.2.2
        OMNamespace xmimeNs = sf.createOMNamespace("http://www.w3.org/2005/05/xmlmime", "xmime");
        note.addAttribute("contentType", "text/xml", xmimeNs);

        body.addChild(note);
        msgCtx.setEnvelope(envelope);

        // Invoke the handler and assert the AxisFault contains the hint
        SchemaValidationHandler handler = new SchemaValidationHandler();
        assertThatThrownBy(() -> handler.invoke(msgCtx))
                .isInstanceOf(AxisFault.class)
                .hasMessageContaining("cvc-complex-type.3.2.2")
                .hasMessageContaining("xs:attribute ref=")
                .hasMessageContaining("not imported or could not be resolved");
    }
}
