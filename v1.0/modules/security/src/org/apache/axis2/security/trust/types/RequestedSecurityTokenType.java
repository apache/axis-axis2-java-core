/**
 * RequestedSecurityTokenType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: #axisVersion# #today#
 */

package org.apache.axis2.security.trust.types;

/**
 * RequestedSecurityTokenType bean class
 */

public class RequestedSecurityTokenType implements
        org.apache.axis2.databinding.ADBBean {
    /*
     * This type was generated from the piece of schema that had name =
     * RequestedSecurityTokenType Namespace URI =
     * http://schemas.xmlsoap.org/ws/2005/02/trust Namespace Prefix = ns2
     */

    /**
     * field for ExtraElement
     */

    protected org.apache.axiom.om.OMElement localExtraElement;

    /**
     * Auto generated getter method
     * 
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMElement getExtraElement() {
        return localExtraElement;
    }

    /**
     * Auto generated setter method
     * 
     * @param param
     *            ExtraElement
     */
    public void setExtraElement(org.apache.axiom.om.OMElement param) {

        this.localExtraElement = param;
    }

    /**
     * databinding method to get an XML representation of this object
     * 
     */
    public javax.xml.stream.XMLStreamReader getPullParser(
            javax.xml.namespace.QName qName) {

        java.util.ArrayList elementList = new java.util.ArrayList();
        java.util.ArrayList attribList = new java.util.ArrayList();

        elementList.add(new javax.xml.namespace.QName("", "extraElement"));

        if (localExtraElement == null) {
            throw new RuntimeException("extraElement cannot be null!!");
        }
        elementList.add(localExtraElement);

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
        public static RequestedSecurityTokenType parse(
                javax.xml.stream.XMLStreamReader reader)
                throws java.lang.Exception {
            RequestedSecurityTokenType object = new RequestedSecurityTokenType();
            try {
                int event = reader.getEventType();

                // event better be a START_ELEMENT. if not we should go up to
                // the start element here
                while (event != javax.xml.stream.XMLStreamReader.START_ELEMENT) {
                    event = reader.next();
                }

                boolean loopDone1 = false;
                // move to the start element
                while (!loopDone1) {
                    if (reader.isStartElement()) {
                        loopDone1 = true;
                    } else {
                        reader.next();
                    }
                }

                // use the QName from the parser as the name for the builder
                javax.xml.namespace.QName startQname1 = reader.getName();

                // We need to wrap the reader so that it produces a fake
                // START_DOCUEMENT event
                // this is needed by the builder classes
                org.apache.axis2.databinding.utils.NamedStaxOMBuilder builder1 = new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
                        new org.apache.axis2.util.StreamWrapper(reader),
                        startQname1);
                object.setExtraElement(builder1.getOMElement());

            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }
    }// end of factory class

}
