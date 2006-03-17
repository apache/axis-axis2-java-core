/**
 * RequestSecurityTokenType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: #axisVersion# #today#
 */

package org.apache.axis2.security.trust.types;

/**
 * RequestSecurityTokenType bean class
 */

public class RequestSecurityTokenType implements
        org.apache.axis2.databinding.ADBBean {
    /*
     * This type was generated from the piece of schema that had name =
     * RequestSecurityTokenType Namespace URI =
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
     * field for RequestType
     */

    protected org.apache.axis2.databinding.types.URI localRequestType;

    /*
     * This tracker boolean wil be used to detect whether the user called the
     * set method for this attribute. It will be used to determine whether to
     * include this field in the serialized XML
     */
    protected boolean localRequestTypeTracker = false;

    /**
     * Auto generated getter method
     * 
     * @return org.apache.axis2.databinding.types.URI
     */
    public org.apache.axis2.databinding.types.URI getRequestType() {
        return localRequestType;
    }

    /**
     * Auto generated setter method
     * 
     * @param param
     *            RequestType
     */
    public void setRequestType(org.apache.axis2.databinding.types.URI param) {

        // update the setting tracker
        localRequestTypeTracker = true;

        this.localRequestType = param;
    }

    /**
     * field for ExtraElement* This was an Array!
     */

    protected org.apache.ws.commons.om.OMElement[] localExtraElement;

    /*
     * This tracker boolean wil be used to detect whether the user called the
     * set method for this attribute. It will be used to determine whether to
     * include this field in the serialized XML
     */
    protected boolean localExtraElementTracker = false;

    /**
     * Auto generated getter method
     * 
     * @return org.apache.ws.commons.om.OMElement[]
     */
    public org.apache.ws.commons.om.OMElement[] getExtraElement() {
        return localExtraElement;
    }

    /**
     * validate the array for ExtraElement
     */
    protected void validateExtraElement(
            org.apache.ws.commons.om.OMElement[] param) {

    }

    /**
     * Auto generated setter method
     * 
     * @param param
     *            ExtraElement
     */
    public void setExtraElement(org.apache.ws.commons.om.OMElement[] param) {

        validateExtraElement(param);

        // update the setting tracker
        localExtraElementTracker = true;

        this.localExtraElement = param;
    }

    /**
     * Auto generated add method for the array for convenience
     * 
     * @param param
     *            org.apache.ws.commons.om.OMElement
     */
    public void addExtraElement(org.apache.ws.commons.om.OMElement param) {
        if (localExtraElement == null) {
            localExtraElement = new org.apache.ws.commons.om.OMElement[] {};
        }
        java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil
                .toList(localExtraElement);
        list.add(param);
        this.localExtraElement = (org.apache.ws.commons.om.OMElement[]) list
                .toArray(new org.apache.ws.commons.om.OMElement[list.size()]);

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
        if (localRequestTypeTracker) {
            elementList.add(new javax.xml.namespace.QName(
                    "http://schemas.xmlsoap.org/ws/2005/02/trust",
                    "RequestType"));

            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
                    .convertToString(localRequestType));
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

        return org.apache.axis2.databinding.utils.ADBPullParser
                .createPullParser(qName, elementList.toArray(), attribList
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
        public static RequestSecurityTokenType parse(
                javax.xml.stream.XMLStreamReader reader)
                throws java.lang.Exception {
            RequestSecurityTokenType object = new RequestSecurityTokenType();
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
                                    .convertToanyURI(tempAttribContext));
                }

                org.apache.axis2.databinding.utils.SimpleElementReaderStateMachine stateMachine1 = new org.apache.axis2.databinding.utils.SimpleElementReaderStateMachine();
                javax.xml.namespace.QName startQname1 = new javax.xml.namespace.QName(
                        "http://schemas.xmlsoap.org/ws/2005/02/trust",
                        "TokenType");
                stateMachine1.setElementNameToTest(startQname1);
                stateMachine1.read(reader);
                object.setTokenType(

                org.apache.axis2.databinding.utils.ConverterUtil
                        .convertToanyURI(stateMachine1.getText()));

                // Move to a start element
                event = reader.getEventType();
                while (event != javax.xml.stream.XMLStreamReader.START_ELEMENT) {
                    event = reader.next();
                }

                org.apache.axis2.databinding.utils.SimpleElementReaderStateMachine stateMachine2 = new org.apache.axis2.databinding.utils.SimpleElementReaderStateMachine();
                javax.xml.namespace.QName startQname2 = new javax.xml.namespace.QName(
                        "http://schemas.xmlsoap.org/ws/2005/02/trust",
                        "RequestType");
                stateMachine2.setElementNameToTest(startQname2);
                stateMachine2.read(reader);
                object.setRequestType(

                org.apache.axis2.databinding.utils.ConverterUtil
                        .convertToanyURI(stateMachine2.getText()));

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
                        .setExtraElement((org.apache.ws.commons.om.OMElement[]) org.apache.axis2.databinding.utils.ConverterUtil
                                .convertToArray(
                                        org.apache.ws.commons.om.OMElement.class,
                                        list3));

            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }
    }// end of factory class

}
