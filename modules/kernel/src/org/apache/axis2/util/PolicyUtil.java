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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.PolicyInclude;
import org.apache.neethi.Constants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.PolicyReference;
import org.apache.woden.internal.util.dom.DOM2Writer;
import org.w3c.dom.Element;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class PolicyUtil {

    public static String getSafeString(String unsafeString) {
        StringBuffer sbuf = new StringBuffer();

        char[] chars = unsafeString.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            switch (c) {
                case'\\':
                    sbuf.append('\\');
                    sbuf.append('\\');
                    break;
                case'"':
                    sbuf.append('\\');
                    sbuf.append('"');
                    break;
                case'\n':
                    sbuf.append('\\');
                    sbuf.append('n');
                    break;
                case'\r':
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
        return (OMElement) XMLUtils.toOM(bais);

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
        return (OMElement) XMLUtils.toOM(bais);
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

    public static String generateId(AxisDescription description) {
        PolicyInclude policyInclude = description.getPolicyInclude();
        String identifier = "-policy-1";

        if (description instanceof AxisMessage) {
            identifier = "msg-" + ((AxisMessage) description).getName() + identifier;
            description = description.getParent();
        }

        if (description instanceof AxisOperation) {
            identifier = "op-" + ((AxisOperation) description).getName() + identifier;
            description = description.getParent();
        }

        if (description instanceof AxisService) {
            identifier = "service-" + ((AxisService) description).getName() + identifier;
        }

        /*
        *  Int 49 is the value of the Character '1'. Here we want to change '1' to '2' or
        *  '2' to '3' .. etc. to construct a unique identifier.
        */
        for (int index = 49; policyInclude.getPolicy(identifier) != null; index++) {
            identifier = identifier.replace((char) index, (char) (index + 1));
        }

        return identifier;
    }
}
