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
package org.apache.axis2.addressing;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AttributeHelper;
import org.apache.axiom.om.util.ElementHelper;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The methods in this class are used to process {@link EndpointReference} objects
 * according to the rules of the 2005/08 (Final) and 2004/08 (submission) WS-Addressing
 * specifications.
 */
public class EndpointReferenceHelper {

    private static final Log log = LogFactory.getLog(EndpointReferenceHelper.class);

    private final static Map finalQNames = new IdentityHashMap();
    private final static Map submissionQNames = new IdentityHashMap();

    /**
     * Populates an endpoint reference based on the <code>OMElement</code> and
     * WS-Addressing namespace that is passed in. If the string passed in is not
     * recognized as a valid WS-Addressing namespace then this method behaves as
     * if http://www.w3.org/2005/08/addressing has been passed in.
     *
     * @param epr                 an endpoint reference instance to hold the info.
     * @param eprOMElement        an element of endpoint reference type
     * @param addressingNamespace the namespace of the WS-Addressing spec to comply with.
     * @throws AxisFault if unable to locate an address element
     * @see #fromOM(OMElement)
     */
    public static void fromOM(EndpointReference epr, OMElement eprOMElement,
                              String addressingNamespace) throws AxisFault {
        boolean isFinalAddressingNamespace = false;
        Map map = null;

        //First pass, identify the addressing namespace.
        if (AddressingConstants.Submission.WSA_NAMESPACE.equals(addressingNamespace)) {
            OMElement address = eprOMElement.getFirstChildWithName(
                    (QName) submissionQNames.get(AddressingConstants.EPR_ADDRESS));

            if (address != null) {
                map = submissionQNames;
                isFinalAddressingNamespace = false;

                if (log.isDebugEnabled()) {
                    log.debug("fromOM: Found address element for namespace, " +
                            AddressingConstants.Submission.WSA_NAMESPACE);
                }
            } else {
                throw new AxisFault(
                        "Unable to locate an address element for the endpoint reference type.");
            }
        } else {
            OMElement address = eprOMElement.getFirstChildWithName(
                    (QName) finalQNames.get(AddressingConstants.EPR_ADDRESS));

            if (address != null) {
                map = finalQNames;
                isFinalAddressingNamespace = true;

                if (log.isDebugEnabled()) {
                    log.debug("fromOM: Found address element for namespace, " +
                            AddressingConstants.Final.WSA_NAMESPACE);
                }
            } else {
                throw new AxisFault(
                        "Unable to locate an address element for the endpoint reference type.");
            }
        }

        //Second pass, identify the properties.
        fromOM(epr, eprOMElement, map, isFinalAddressingNamespace);
    }

    /**
     * Populates an endpoint reference based on the <code>String</code> that is
     * passed in. If the http://schemas.xmlsoap.org/ws/2004/08/addressing namespace
     * is in effect then any reference properties will be saved as reference parameters.
     * Regardless of the addressing namespace in effect, any elements present in the
     * <code>String</code> that are not recognised are saved as extensibility elements.
     *
     * @param eprString string from the element of endpoint reference type
     * @throws AxisFault if unable to locate an address element
     */
    public static EndpointReference fromOM(String eprString) throws AxisFault {
        try {
            return fromOM(new StAXOMBuilder(
                    new ByteArrayInputStream(eprString.getBytes())).getDocumentElement());
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Populates an endpoint reference based on the <code>OMElement</code> that is
     * passed in. If the http://schemas.xmlsoap.org/ws/2004/08/addressing namespace
     * is in effect then any reference properties will be saved as reference parameters.
     * Regardless of the addressing namespace in effect, any elements present in the
     * <code>OMElement</code> that are not recognised are saved as extensibility elements.
     *
     * @param eprOMElement an element of endpoint reference type
     * @throws AxisFault if unable to locate an address element
     */
    public static EndpointReference fromOM(OMElement eprOMElement) throws AxisFault {
        EndpointReference epr = new EndpointReference("");
        boolean isFinalAddressingNamespace = false;
        Map map = null;

        //First pass, identify the addressing namespace.
        OMElement address = eprOMElement
                .getFirstChildWithName((QName) finalQNames.get(AddressingConstants.EPR_ADDRESS));

        if (address != null) {
            map = finalQNames;
            isFinalAddressingNamespace = true;

            if (log.isDebugEnabled()) {
                log.debug("fromOM: Found address element for namespace, " +
                        AddressingConstants.Final.WSA_NAMESPACE);
            }
        } else {
            address = eprOMElement.getFirstChildWithName(
                    (QName) submissionQNames.get(AddressingConstants.EPR_ADDRESS));

            if (address != null) {
                map = submissionQNames;
                isFinalAddressingNamespace = false;

                if (log.isDebugEnabled()) {
                    log.debug("fromOM: Found address element for namespace, " +
                            AddressingConstants.Submission.WSA_NAMESPACE);
                }
            } else {
                throw new AxisFault(
                        "Unable to locate an address element for the endpoint reference type.");
            }
        }

        //Second pass, identify the properties.
        fromOM(epr, eprOMElement, map, isFinalAddressingNamespace);

        return epr;
    }

    /**
     * Creates an <code>OMElement</code> based on the properties of the endpoint
     * reference. The output may differ based on the addressing namespace that is
     * in effect when this method is called. If the http://www.w3.org/2005/08/addressing
     * namespace is in effect, and a metadata property has been defined for the
     * endpoint reference, then there will be a metadata element to contain the
     * property in the output. If the http://schemas.xmlsoap.org/ws/2004/08/addressing
     * namespace is in effect, however, then no metadata element will be included
     * in the output, even if a metadata property element has been defined.
     *
     * @param factory
     * @param epr
     * @param qname
     * @param addressingNamespace
     * @return
     * @throws AxisFault
     */
    public static OMElement toOM(OMFactory factory, EndpointReference epr, QName qname,
                                 String addressingNamespace) throws AxisFault {
        OMElement eprElement = null;

        if (log.isDebugEnabled()) {
            log.debug("toOM: Factory, " + factory);
            log.debug("toOM: Endpoint reference, " + epr);
            log.debug("toOM: Element qname, " + qname);
            log.debug("toOM: Addressing namespace, " + addressingNamespace);
        }

        if (addressingNamespace == null) {
            throw new AxisFault("Addressing namespace cannot be null.");
        }

        if (qname.getPrefix() != null) {
            OMNamespace wrapNs =
                    factory.createOMNamespace(qname.getNamespaceURI(), qname.getPrefix());
            if (factory instanceof SOAPFactory) {
                eprElement =
                        ((SOAPFactory) factory).createSOAPHeaderBlock(qname.getLocalPart(), wrapNs);
            } else {
                eprElement = factory.createOMElement(qname.getLocalPart(), wrapNs);
            }

            OMNamespace wsaNS = factory.createOMNamespace(addressingNamespace,
                                                          AddressingConstants.WSA_DEFAULT_PREFIX);
            OMElement addressE =
                    factory.createOMElement(AddressingConstants.EPR_ADDRESS, wsaNS, eprElement);
            String address = epr.getAddress();
            addressE.setText(address);

            ArrayList addressAttributes = epr.getAddressAttributes();
            if (addressAttributes != null) {
                Iterator attrIter = addressAttributes.iterator();
                while (attrIter.hasNext()) {
                    OMAttribute omAttributes = (OMAttribute) attrIter.next();
                    addressE.addAttribute(omAttributes);
                }
            }

            List metaData = epr.getMetaData();
            if (metaData != null &&
                    AddressingConstants.Final.WSA_NAMESPACE.equals(addressingNamespace)) {
                OMElement metadataE = factory.createOMElement(
                        AddressingConstants.Final.WSA_METADATA, wsaNS, eprElement);
                for (int i = 0, size = metaData.size(); i < size; i++) {
                    OMElement omElement = (OMElement) metaData.get(i);
                    metadataE.addChild(ElementHelper.importOMElement(omElement, factory));
                }

                ArrayList metadataAttributes = epr.getMetadataAttributes();
                if (metadataAttributes != null) {
                    Iterator attrIter = metadataAttributes.iterator();
                    while (attrIter.hasNext()) {
                        OMAttribute omAttributes = (OMAttribute) attrIter.next();
                        metadataE.addAttribute(omAttributes);
                    }
                }
            }


            Map referenceParameters = epr.getAllReferenceParameters();
            if (referenceParameters != null) {
                OMElement refParameterElement = factory.createOMElement(
                        AddressingConstants.EPR_REFERENCE_PARAMETERS, wsaNS, eprElement);
                Iterator iterator = referenceParameters.values().iterator();
                while (iterator.hasNext()) {
                    OMElement omElement = (OMElement) iterator.next();
                    refParameterElement.addChild(ElementHelper.importOMElement(omElement, factory));
                }
            }

            List attributes = epr.getAttributes();
            if (attributes != null) {
                for (int i = 0, size = attributes.size(); i < size; i++) {
                    OMAttribute omAttribute = (OMAttribute) attributes.get(i);
                    AttributeHelper.importOMAttribute(omAttribute, eprElement);
                }
            }

            // add xs:any
            List extensibleElements = epr.getExtensibleElements();
            if (extensibleElements != null) {
                for (int i = 0, size = extensibleElements.size(); i < size; i++) {
                    OMElement omElement = (OMElement) extensibleElements.get(i);
                    eprElement.addChild(ElementHelper.importOMElement(omElement, factory));
                }
            }
        } else {
            throw new AxisFault("prefix must be specified");
        }

        return eprElement;
    }

    private static void fromOM(EndpointReference epr, OMElement eprOMElement, Map map,
                               boolean isFinalAddressingNamespace) {
        Iterator childElements = eprOMElement.getChildElements();

        while (childElements.hasNext()) {
            OMElement eprChildElement = (OMElement) childElements.next();
            QName qname = eprChildElement.getQName();

            if (map.get(AddressingConstants.EPR_ADDRESS).equals(qname)) {
                //We need to identify the address element again in order to ensure
                //that it is not included with the extensibility elements.
                epr.setAddress(eprChildElement.getText());
                Iterator allAddrAttributes = eprChildElement.getAllAttributes();
                ArrayList addressAttributes = new ArrayList();
                while (allAddrAttributes.hasNext()) {
                    OMAttribute attribute = (OMAttribute) allAddrAttributes.next();
                    addressAttributes.add(attribute);
                }
                epr.setAddressAttributes(addressAttributes);
            } else if (map.get(AddressingConstants.EPR_REFERENCE_PARAMETERS).equals(qname)) {
                Iterator iterator = eprChildElement.getChildElements();
                while (iterator.hasNext()) {
                    OMElement element = (OMElement) iterator.next();
                    epr.addReferenceParameter(element);
                }
            } else if (isFinalAddressingNamespace &&
                    map.get(AddressingConstants.Final.WSA_METADATA).equals(qname)) {
                Iterator iterator = eprChildElement.getChildElements();
                while (iterator.hasNext()) {
                    OMNode node = (OMNode) iterator.next();
                    epr.addMetaData(node);
                }
                Iterator allMDAttributes = eprChildElement.getAllAttributes();
                ArrayList metadataAttributes = new ArrayList();
                while (allMDAttributes.hasNext()) {
                    OMAttribute attribute = (OMAttribute) allMDAttributes.next();
                    metadataAttributes.add(attribute);
                }
                epr.setMetadataAttributes(metadataAttributes);
            } else if (!isFinalAddressingNamespace &&
                    map.get(AddressingConstants.Submission.EPR_REFERENCE_PROPERTIES).equals(qname))
            {
                // since we have the model for WS-Final, we don't have a place to keep this reference properties.
                // The only compatible place is reference properties
                Iterator iterator = eprChildElement.getChildElements();
                while (iterator.hasNext()) {
                    OMElement element = (OMElement) iterator.next();
                    epr.addReferenceParameter(element);
                }
            } else {
                epr.addExtensibleElement(eprChildElement);
            }
        }

        Iterator attributes = eprOMElement.getAllAttributes();
        while (attributes.hasNext()) {
            OMAttribute attribute = (OMAttribute) attributes.next();
            epr.addAttribute(attribute);
        }

        if (log.isDebugEnabled()) {
            log.debug("fromOM: Endpoint reference, " + epr);
        }
    }

    static {
        finalQNames.put(AddressingConstants.EPR_ADDRESS, new QName(
                AddressingConstants.Final.WSA_NAMESPACE, AddressingConstants.EPR_ADDRESS));
        finalQNames.put(AddressingConstants.EPR_REFERENCE_PARAMETERS, new QName(
                AddressingConstants.Final.WSA_NAMESPACE,
                AddressingConstants.EPR_REFERENCE_PARAMETERS));
        finalQNames.put(AddressingConstants.Final.WSA_METADATA, new QName(
                AddressingConstants.Final.WSA_NAMESPACE, AddressingConstants.Final.WSA_METADATA));

        submissionQNames.put(AddressingConstants.EPR_ADDRESS, new QName(
                AddressingConstants.Submission.WSA_NAMESPACE, AddressingConstants.EPR_ADDRESS));
        submissionQNames.put(AddressingConstants.EPR_REFERENCE_PARAMETERS, new QName(
                AddressingConstants.Submission.WSA_NAMESPACE,
                AddressingConstants.EPR_REFERENCE_PARAMETERS));
        submissionQNames.put(AddressingConstants.Submission.EPR_REFERENCE_PROPERTIES, new QName(
                AddressingConstants.Submission.WSA_NAMESPACE,
                AddressingConstants.Submission.EPR_REFERENCE_PROPERTIES));
    }
}
