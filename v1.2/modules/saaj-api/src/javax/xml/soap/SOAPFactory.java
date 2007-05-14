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
package javax.xml.soap;


/**
 * <code>SOAPFactory</code> is a factory for creating various objects that exist in the SOAP XML
 * tree.
 * <p/>
 * <code>SOAPFactory</code> can be used to create XML fragments that will eventually end up in the
 * SOAP part. These fragments can be inserted as children of the <code>SOAPHeaderElement</code> or
 * <code>SOAPBodyElement</code> or <code>SOAPEnvelope</code>.
 * <p/>
 * <code>SOAPFactory</code> also has methods to create <code>javax.xml.soap.Detail</code> objects as
 * well as <code>java.xml.soap.Name</code> objects.
 */
public abstract class SOAPFactory {

    public SOAPFactory() {
    }

    /**
     * Create a <code>SOAPElement</code> object initialized with the given <code>Name</code>
     * object.
     *
     * @param name a <code>Name</code> object with the XML name for the new element
     * @return the new <code>SOAPElement</code> object that was created
     * @throws SOAPException if there is an error in creating the <code>SOAPElement</code> object
     */
    public abstract SOAPElement createElement(Name name) throws SOAPException;

    /**
     * Create a <code>SOAPElement</code> object initialized with the given local name.
     *
     * @param localName a <code>String</code> giving the local name for the new element
     * @return the new <code>SOAPElement</code> object that was created
     * @throws SOAPException if there is an error in creating the <code>SOAPElement</code> object
     */
    public abstract SOAPElement createElement(String localName) throws SOAPException;

    /**
     * Create a new <code>SOAPElement</code> object with the given local name, prefix and uri.
     *
     * @param localName a <code>String</code> giving the local name for the new element
     * @param prefix    the prefix for this <code>SOAPElement</code>
     * @param uri       a <code>String</code> giving the URI of the namespace to which the new
     *                  element belongs
     * @return the new <code>SOAPElement</code> object that was created
     * @throws SOAPException if there is an error in creating the <code>SOAPElement</code> object
     */
    public abstract SOAPElement createElement(String localName,
                                              String prefix,
                                              String uri)
            throws SOAPException;

    /**
     * Creates a new <code>Detail</code> object which serves as a container for
     * <code>DetailEntry</code> objects.
     * <p/>
     * This factory method creates <code>Detail</code> objects for use in situations where it is not
     * practical to use the <code>SOAPFault</code> abstraction.
     *
     * @return a <code>Detail</code> object
     * @throws SOAPException if there is a SOAP error
     */
    public abstract Detail createDetail() throws SOAPException;

    /**
     * Creates a new <code>Name</code> object initialized with the given local name, namespace
     * prefix, and namespace URI.
     * <p/>
     * This factory method creates <code>Name</code> objects for use in situations where it is not
     * practical to use the <code>SOAPEnvelope</code> abstraction.
     *
     * @param localName a <code>String</code> giving the local name
     * @param prefix    a <code>String</code> giving the prefix of the namespace
     * @param uri       a <code>String</code> giving the URI of the namespace
     * @return a <code>Name</code> object initialized with the given local name, namespace prefix,
     *         and namespace URI
     * @throws SOAPException if there is a SOAP error
     */
    public abstract Name createName(String localName,
                                    String prefix,
                                    String uri)
            throws SOAPException;

    /**
     * Creates a new <code>Name</code> object initialized with the given local name.
     * <p/>
     * This factory method creates <code>Name</code> objects for use in situations where it is not
     * practical to use the <code>SOAPEnvelope</code> abstraction.
     *
     * @param localName a <code>String</code> giving the local name
     * @return a <code>Name</code> object initialized with the given local name
     * @throws SOAPException if there is a SOAP error
     */
    public abstract Name createName(String localName) throws SOAPException;

    /**
     * Creates a new instance of <code>SOAPFactory</code>.
     *
     * @return a new instance of a <code>SOAPFactory</code>
     * @throws SOAPException if there was an error creating the default <code>SOAPFactory</code>
     */
    public static SOAPFactory newInstance() throws SOAPException {

        try {
            return (SOAPFactory)FactoryFinder.find(SF_PROPERTY, DEFAULT_SF);
        } catch (Exception exception) {
            throw new SOAPException("Unable to create SOAP Factory: "
                    + exception.getMessage());
        }
    }


    /**
     * Creates a new SOAPFactory object that is an instance of the specified implementation, this
     * method uses the SAAJMetaFactory to locate the implementation class and create the SOAPFactory
     * instance.
     *
     * @param protocol - a string constant representing the protocol of the specified SOAP factory
     *                 implementation. May be either DYNAMIC_SOAP_PROTOCOL, DEFAULT_SOAP_PROTOCOL
     *                 (which is the same as) SOAP_1_1_PROTOCOL, or SOAP_1_2_PROTOCOL. Returns: a
     *                 new instance of a SOAPFactory
     * @return javax.xml.soap.SOAPFactory
     * @throws SOAPException - if there is an error creating the specified SOAPFactory
     * @see <CODE>SAAJMetaFactory</CODE>
     */
    public static SOAPFactory newInstance(String s) throws SOAPException {
        return SAAJMetaFactory.getInstance().newSOAPFactory(s);
    }


    /**
     * Creates a SOAPElement object from an existing DOM Element. If the DOM Element that is passed
     * in as an argument is already a SOAPElement then this method must return it unmodified without
     * any further work. Otherwise, a new SOAPElement is created and a deep copy is made of the
     * domElement argument. The concrete type of the return value will depend on the name of the
     * domElement argument. If any part of the tree rooted in domElement violates SOAP rules, a
     * SOAPException will be thrown.
     *
     * @param domElement - the Element to be copied.
     * @return a new SOAPElement that is a copy of domElement.
     * @throws SOAPException - if there is an error in creating the SOAPElement object
     * @see SOAPFactoryImpl
     * @since SAAJ 1.3
     */
    public SOAPElement createElement(org.w3c.dom.Element element)
            throws SOAPException {
        //see SOAPFactoryImpl
        return null;
    }


    /**
     * Creates a SOAPElement object initialized with the given QName object. The concrete type of
     * the return value will depend on the name given to the new SOAPElement. For instance, a new
     * SOAPElement with the name {http://www.w3.org/2003/05/soap-envelope}Envelope} Envelope would
     * cause a SOAPEnvelope that supports SOAP 1.2 behavior to be created.
     *
     * @param qname - a QName object with the XML name for the new element
     * @return the new SOAPElement object that was created
     * @throws SOAPException - if there is an error in creating the SOAPElement object
     * @see SOAPFactoryImpl
     */
    public SOAPElement createElement(javax.xml.namespace.QName qname)
            throws SOAPException {
        return null;
    }

    public abstract SOAPFault createFault()
            throws SOAPException;

    public abstract SOAPFault createFault(java.lang.String reasonText,
                                          javax.xml.namespace.QName faultCode)
            throws SOAPException;

    private static final String SF_PROPERTY = "javax.xml.soap.SOAPFactory";

    private static final String DEFAULT_SF =
            "org.apache.axis2.saaj.SOAPFactoryImpl";
}
