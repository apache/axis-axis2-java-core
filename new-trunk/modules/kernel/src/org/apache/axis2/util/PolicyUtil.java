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

package org.apache.axis2.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.neethi.Constants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.PolicyReference;
import org.apache.woden.internal.util.dom.DOM2Writer;
import org.w3c.dom.Element;

public class PolicyUtil {

    public static String getSafeString(String unsafeString) {
        StringBuffer sbuf = new StringBuffer();

        char[] chars = unsafeString.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            switch (c) {
            case '\\':
                sbuf.append('\\');
                sbuf.append('\\');
                break;
            case '"':
                sbuf.append('\\');
                sbuf.append('"');
                break;
            case '\n':
                sbuf.append('\\');
                sbuf.append('n');
                break;
            case '\r':
                sbuf.append('\\');
                sbuf.append('r');
                break;
            default:
                sbuf.append(c);
            }
        }

        return sbuf.toString();
    }

    public static OMElement getPolicyComponentAsOMElement(
            PolicyComponent policyComponent,
            ExternalPolicySerializer externalPolicySerializer)
            throws XMLStreamException, FactoryConfigurationError {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (policyComponent instanceof Policy) {
            externalPolicySerializer.serialize((Policy) policyComponent, baos);

        } else {
            XMLStreamWriter writer = XMLOutputFactory.newInstance()
                    .createXMLStreamWriter(baos);
            policyComponent.serialize(writer);
            writer.flush();
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        return OMXMLBuilderFactory.createStAXOMBuilder(
                OMAbstractFactory.getOMFactory(),
                XMLInputFactory.newInstance().createXMLStreamReader(bais))
                .getDocumentElement();

    }

    public static OMElement getPolicyComponentAsOMElement(
            PolicyComponent component) throws XMLStreamException,
            FactoryConfigurationError {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLStreamWriter writer = XMLOutputFactory.newInstance()
                .createXMLStreamWriter(baos);

        component.serialize(writer);
        writer.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        return OMXMLBuilderFactory.createStAXOMBuilder(
                OMAbstractFactory.getOMFactory(),
                XMLInputFactory.newInstance().createXMLStreamReader(bais))
                .getDocumentElement();
    }

    public static PolicyComponent getPolicyComponentFromOMElement(
            OMElement policyComponent) throws IllegalArgumentException {

        if (policyComponent instanceof Policy) {
            return PolicyEngine.getPolicy(policyComponent);

        } else if (policyComponent instanceof PolicyReference) {
            return PolicyEngine.getPolicyReference(policyComponent);

        } else {
            throw new IllegalArgumentException(
                    "Agrument is neither a <wsp:Policy> nor a <wsp:PolicyReference> element");
        }
    }

    public static PolicyComponent getPolicyComponent(Element element) {

        String xmlString;
        ByteArrayInputStream bais;

        if (Constants.URI_POLICY_NS.equals(element.getNamespaceURI())) {

            if (Constants.ELEM_POLICY.equals(element.getLocalName())) {
                xmlString = DOM2Writer.nodeToString(element);
                bais = new ByteArrayInputStream(xmlString.getBytes());

                return PolicyEngine.getPolicy(bais);

            } else if (Constants.ELEM_POLICY_REF.equals(element.getLocalName())) {
                xmlString = DOM2Writer.nodeToString(element);
                bais = new ByteArrayInputStream(xmlString.getBytes());

                return PolicyEngine.getPolicyReferene(bais);
            }
        }

        throw new IllegalArgumentException(
                "Agrument is neither a <wsp:Policy> nor a <wsp:PolicyReference> element");
    }

    public static String policyComponentToString(PolicyComponent policyComponent)
            throws XMLStreamException, FactoryConfigurationError {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLStreamWriter writer = XMLOutputFactory.newInstance()
                .createXMLStreamWriter(baos);

        policyComponent.serialize(writer);
        writer.flush();

        return baos.toString();
    }
}
