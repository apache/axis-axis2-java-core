/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package javax.xml.ws.wsaddressing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;

/**
 * <p>Java class for EndpointReferenceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EndpointReferenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Address" type="{http://www.w3.org/2005/08/addressing}AttributedURIType"/>
 *         &lt;element ref="{http://www.w3.org/2005/08/addressing}ReferenceParameters" minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2005/08/addressing}Metadata" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EndpointReferenceType", propOrder = {
    "address",
    "referenceParameters",
    "metadata",
    "any"
})
public final class W3CEndpointReference extends EndpointReference {
    @XmlTransient
    protected static volatile JAXBContext jaxbContext;
    @XmlTransient
    protected static final String NS = "http://www.w3.org/2005/08/addressing";
    @XmlTransient
    protected static final QName NAME = new QName(NS, "EndpointReference", "wsa");
    
    @XmlElement(name = "Address", required = true)
    protected AttributedURIType address;
    @XmlElement(name = "ReferenceParameters")
    protected ReferenceParametersType referenceParameters;
    @XmlElement(name = "Metadata")
    protected MetadataType metadata;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    protected W3CEndpointReference() {
    }
    
    public W3CEndpointReference(Source eprInfoset) {
        super();
        
        try {
            JAXBContext jaxbContext = getJAXBContext();
            Unmarshaller um = jaxbContext.createUnmarshaller();
            JAXBElement<W3CEndpointReference> element =
                um.unmarshal(eprInfoset, W3CEndpointReference.class);
            W3CEndpointReference w3cEPR = element.getValue();
            
            address = w3cEPR.address;
            referenceParameters = w3cEPR.referenceParameters;
            metadata = w3cEPR.metadata;
            any = w3cEPR.any;
            otherAttributes.putAll(w3cEPR.otherAttributes);
        }
        catch (Exception e) {
            throw new WebServiceException("Unable to create W3C endpoint reference.", e);
        }
    }
    
    @Override
    public void writeTo(Result result) {
        if (result == null) {
            //TODO NLS enable
            throw new IllegalArgumentException("Null is not allowed.");
        }
        
        try {
            JAXBContext jaxbContext = getJAXBContext();
            Marshaller m = jaxbContext.createMarshaller();
            m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            JAXBElement<W3CEndpointReference> element =
                new JAXBElement<W3CEndpointReference>(NAME, W3CEndpointReference.class, this);
            m.marshal(element, result);
        }
        catch (Exception e) {
            //TODO NLS enable
            throw new WebServiceException("writeTo failure.", e);
        }
    }

    private JAXBContext getJAXBContext() throws JAXBException {
        //This is an implementation of double-checked locking.
        //It works because jaxbContext is volatile.
        if (jaxbContext == null) {
            synchronized (W3CEndpointReference.class) {
                if (jaxbContext == null)
                    jaxbContext = JAXBContext.newInstance(W3CEndpointReference.class);
            }
        }
        
        return jaxbContext;
    }

    /**
     * <p>Java class for AttributedURIType complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType name="AttributedURIType">
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>anyURI">
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "AttributedURIType", propOrder = {
        "value"
    })
    private static class AttributedURIType {

        @XmlValue
        @XmlSchemaType(name = "anyURI")
        protected String value;
        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();
        
        public AttributedURIType() {
        }
    }

    /**
     * <p>Java class for ReferenceParametersType complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType name="ReferenceParametersType">
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;any/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "ReferenceParametersType", propOrder = {
        "any"
    })
    private static class ReferenceParametersType {

        @XmlAnyElement(lax = true)
        protected List<Object> any;
        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();
        
        public ReferenceParametersType() {
        }
    }

    /**
     * <p>Java class for MetadataType complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType name="MetadataType">
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;any/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "MetadataType", propOrder = {
        "any"
    })
    private static class MetadataType {

        @XmlAnyElement(lax = true)
        protected List<Object> any;
        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();
        
        public MetadataType() {
        }
    }
}
