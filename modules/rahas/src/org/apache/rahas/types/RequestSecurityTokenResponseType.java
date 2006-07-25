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

/**
 * RequestSecurityTokenResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: #axisVersion# #today#
 */

package org.apache.rahas.types;

/**
 * RequestSecurityTokenResponseType bean class
 */

public class RequestSecurityTokenResponseType implements
        org.apache.axis2.databinding.ADBBean {
    /*
     * This type was generated from the piece of schema that had name =
     * RequestSecurityTokenResponseType Namespace URI =
     * http://schemas.xmlsoap.org/ws/2005/02/trust Namespace Prefix = ns2
     */

    /**
     * field for TokenType
     */

    protected org.apache.axis2.databinding.types.URI localTokenType;

    /*
     * This tracker boolean wil be used to detect whether the user called the
     * set method for this attribute. It will be used to determine whether to
     * include this field in the serialized XML
     */
    protected boolean localTokenTypeTracker = false;

    /**
     * Auto generated getter method
     * 
     * @return org.apache.axis2.databinding.types.URI
     */
    public org.apache.axis2.databinding.types.URI getTokenType() {
        return localTokenType;
    }

    /**
     * Auto generated setter method
     * 
     * @param param
     *            TokenType
     */
    public void setTokenType(org.apache.axis2.databinding.types.URI param) {

        // update the setting tracker
        localTokenTypeTracker = true;

        this.localTokenType = param;
    }

    /**
     * field for RequestedSecurityToken
     */

    protected org.apache.rahas.types.RequestedSecurityTokenType localRequestedSecurityToken;

    /*
     * This tracker boolean wil be used to detect whether the user called the
     * set method for this attribute. It will be used to determine whether to
     * include this field in the serialized XML
     */
    protected boolean localRequestedSecurityTokenTracker = false;

    /**
     * Auto generated getter method
     * 
     * @return org.xmlsoap.schemas.ws._2005._02.trust.RequestedSecurityTokenType
     */
    public org.apache.rahas.types.RequestedSecurityTokenType getRequestedSecurityToken() {
        return localRequestedSecurityToken;
    }

    /**
     * Auto generated setter method
     * 
     * @param param
     *            RequestedSecurityToken
     */
    public void setRequestedSecurityToken(
            org.apache.rahas.types.RequestedSecurityTokenType param) {

        // update the setting tracker
        localRequestedSecurityTokenTracker = true;

        this.localRequestedSecurityToken = param;
    }

    /**
     * field for ExtraElement* This was an Array!
     */

    protected org.apache.axiom.om.OMElement[] localExtraElement;

    /*
     * This tracker boolean wil be used to detect whether the user called the
     * set method for this attribute. It will be used to determine whether to
     * include this field in the serialized XML
     */
    protected boolean localExtraElementTracker = false;

    /**
     * Auto generated getter method
     * 
     * @return org.apache.axiom.om.OMElement[]
     */
    public org.apache.axiom.om.OMElement[] getExtraElement() {
        return localExtraElement;
    }

    /**
     * validate the array for ExtraElement
     */
    protected void validateExtraElement(
            org.apache.axiom.om.OMElement[] param) {

    }

    /**
     * Auto generated setter method
     * 
     * @param param
     *            ExtraElement
     */
    public void setExtraElement(org.apache.axiom.om.OMElement[] param) {

        validateExtraElement(param);

        // update the setting tracker
        localExtraElementTracker = true;

        this.localExtraElement = param;
    }

    /**
     * Auto generated add method for the array for convenience
     * 
     * @param param
     *            org.apache.axiom.om.OMElement
     */
    public void addExtraElement(org.apache.axiom.om.OMElement param) {
        if (localExtraElement == null) {
            localExtraElement = new org.apache.axiom.om.OMElement[] {};
        }
        java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil
                .toList(localExtraElement);
        list.add(param);
        this.localExtraElement = (org.apache.axiom.om.OMElement[]) list
                .toArray(new org.apache.axiom.om.OMElement[list.size()]);

    }

    /**
     * field for Context* This was an Attribute!
     */

    protected org.apache.axis2.databinding.types.URI localContext;

    /**
     * Auto generated getter method
     * 
     * @return org.apache.axis2.databinding.types.URI
     */
    public org.apache.axis2.databinding.types.URI getContext() {
        return localContext;
    }

    /**
     * Auto generated setter method
     * 
     * @param param
     *            Context
     */
    public void setContext(org.apache.axis2.databinding.types.URI param) {

        this.localContext = param;
    }

    /**
     * databinding method to get an XML representation of this object
     * 
     */
    public javax.xml.stream.XMLStreamReader getPullParser(
            javax.xml.namespace.QName qName) {

        java.util.ArrayList elementList = new java.util.ArrayList();
        java.util.ArrayList attribList = new java.util.ArrayList();

        if (localTokenTypeTracker) {
            elementList
                    .add(new javax.xml.namespace.QName(
                            "http://schemas.xmlsoap.org/ws/2005/02/trust",
                            "TokenType"));

            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
                    .convertToString(localTokenType));
        }
        if (localRequestedSecurityTokenTracker) {
            elementList.add(new javax.xml.namespace.QName(
                    "http://schemas.xmlsoap.org/ws/2005/02/trust",
                    "RequestedSecurityToken"));

            if (localRequestedSecurityToken == null) {
                throw new RuntimeException(
                        "RequestedSecurityToken cannot be null!!");
            }
            elementList.add(localRequestedSecurityToken);
        }
        if (localExtraElementTracker) {
            elementList.add(new javax.xml.namespace.QName("", "extraElement"));

            if (localExtraElement == null) {
                throw new RuntimeException("extraElement cannot be null!!");
            }
            elementList.add(localExtraElement);
        }
        attribList.add(new javax.xml.namespace.QName(
                "http://schemas.xmlsoap.org/ws/2005/02/trust", "Context"));
        attribList.add(org.apache.axis2.databinding.utils.ConverterUtil
                .convertToString(localContext));

        return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl
                (qName, elementList.toArray(), attribList
                        .toArray());

    }

    /**
     * utility method to http://www.w3.org/2001/XMLSchema-instance
     */

    /**
     * Factory class that keeps the parse method
     */
    public static class Factory {
        /**
         * static method to create the object
         */
        public static RequestSecurityTokenResponseType parse(
                javax.xml.stream.XMLStreamReader reader)
                throws java.lang.Exception {
            RequestSecurityTokenResponseType object = new RequestSecurityTokenResponseType();
            try {
                int event = reader.getEventType();

                // event better be a START_ELEMENT. if not we should go up to
                // the start element here
                while (event != javax.xml.stream.XMLStreamReader.START_ELEMENT) {
                    event = reader.next();
                }

                java.lang.String tempAttribContext = reader.getAttributeValue(
                        "http://schemas.xmlsoap.org/ws/2005/02/trust",
                        "Context");
                if (tempAttribContext != null) {
                    object
                            .setContext(org.apache.axis2.databinding.utils.ConverterUtil
                                    .convertToAnyURI(tempAttribContext));
                }

                org.apache.axis2.databinding.utils.SimpleElementReaderStateMachine stateMachine1 = new org.apache.axis2.databinding.utils.SimpleElementReaderStateMachine();
                javax.xml.namespace.QName startQname1 = new javax.xml.namespace.QName(
                        "http://schemas.xmlsoap.org/ws/2005/02/trust",
                        "TokenType");
                stateMachine1.setElementNameToTest(startQname1);
                stateMachine1.read(reader);
                object.setTokenType(

                org.apache.axis2.databinding.utils.ConverterUtil
                        .convertToAnyURI(stateMachine1.getText()));

                object
                        .setRequestedSecurityToken(org.apache.rahas.types.RequestedSecurityTokenType.Factory
                                .parse(reader));

                java.util.ArrayList list3 = new java.util.ArrayList();
                boolean loopDone3 = false;
                javax.xml.namespace.QName startQname3 = new javax.xml.namespace.QName(
                        "", "extraElement");

                boolean loopDone3_internal = false;

                while (!loopDone3_internal) {
                    if (reader.isStartElement()
                            && startQname3.equals(reader.getName())) {
                        loopDone3_internal = true;
                    } else {
                        reader.next();
                    }
                }

                while (!loopDone3) {
                    event = reader.getEventType();
                    if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event
                            && startQname3.equals(reader.getName())) {

                        // We need to wrap the reader so that it produces a fake
                        // START_DOCUEMENT event
                        org.apache.axis2.databinding.utils.NamedStaxOMBuilder builder3 = new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
                                new org.apache.axis2.util.StreamWrapper(reader),
                                startQname3);

                        list3.add(builder3.getOMElement());

                    } else if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event
                            && !startQname3.equals(reader.getName())) {
                        loopDone3 = true;
                    } else if (javax.xml.stream.XMLStreamConstants.END_ELEMENT == event
                            && !startQname3.equals(reader.getName())) {
                        loopDone3 = true;
                    } else if (javax.xml.stream.XMLStreamConstants.END_DOCUMENT == event) {
                        loopDone3 = true;
                    } else {
                        reader.next();
                    }

                }

                object
                        .setExtraElement((org.apache.axiom.om.OMElement[]) org.apache.axis2.databinding.utils.ConverterUtil
                                .convertToArray(
                                        org.apache.axiom.om.OMElement.class,
                                        list3));

            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }
    }// end of factory class

}
