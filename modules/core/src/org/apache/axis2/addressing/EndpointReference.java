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
package org.apache.axis2.addressing;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Class EndpointReference
 * Contents of this class differs between WS-A Submission and WS-Final. Without having a
 * inheritance hierarchy for this small difference, lets have all the properties in the same class.
 */
public class EndpointReference implements Serializable {

    // Commons properties
    private String address;
    private Map referenceParameters;

    // Properties from WS-A Submission version
    private Map referenceProperties;
    private QName portType;
    private ServiceName serviceName;
    private OMElement policy;

    // Properties from WS-A Final
    private OMElement metaData;

    /**
     * @param address
     */
    public EndpointReference(String address) {
        this.address = address;
    }

    /**
     * Method getAddress
     */
    public String getAddress() {
        return address;
    }

    /**
     * Method setAddress
     *
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Method getServiceName
     */
    public ServiceName getServiceName() {
        return serviceName;
    }

    /**
     * Method setServiceName
     *
     * @param serviceName
     */
    public void setServiceName(ServiceName serviceName) {
        this.serviceName = serviceName;
    }

    public OMElement getPolicy() {
        return policy;
    }

    public void setPolicy(OMElement policy) {
        this.policy = policy;
    }

    /**
     * This will return a Map of reference parameters with QName as the key and an OMElement
     * as the value
     *
     * @return - map of the reference parameters, where the key is the QName of the reference parameter
     *         and the value is an OMElement
     */
    public Map getAllReferenceParameters() {
        return referenceParameters;
    }

    /**
     * Set a Map with QName as the key and an OMElement
     * as the value
     *
     * @param referenceParameters
     */
    public void setReferenceParameters(Map referenceParameters) {
        this.referenceParameters = referenceParameters;
    }

    /**
     * This will return a Map of reference properties with QName as the key and an OMElement
     * as the value
     *
     * @return - map of the reference parameters, where the key is the QName of the reference parameter
     *         and the value is an OMElement
     */
    public Map getAllReferenceProperties() {
        return referenceProperties;
    }

    /**
     * Set a Map with QName as the key and an OMElement
     * as the value
     *
     * @param referenceProperties
     */
    public void setReferenceProperties(HashMap referenceProperties) {
        this.referenceProperties = referenceProperties;
    }

    public QName getPortType() {
        return portType;
    }

    public void setPortType(QName portType) {
        this.portType = portType;
    }

    public OMElement getMetaData() {
        return metaData;
    }

    public void setMetaData(OMElement metaData) {
        this.metaData = metaData;
    }


    /**
     * @param qname
     * @param value - the text of the OMElement. Remember that this is a convenient method for the user,
     *              which has limited capability. If you want more power use @See EndpointReference#addReferenceParameter(OMElement)
     */
    public void addReferenceParameter(QName qname, String value) {
        if (qname == null) {
            return;
        }
        OMElement omElement = OMAbstractFactory.getOMFactory().createOMElement(qname, null);
        omElement.setText(value);
        addReferenceParameter(omElement);
    }

    /**
     * @param omElement
     */
    public void addReferenceParameter(OMElement omElement) {
        if (omElement == null) {
            return;
        }
        if (referenceParameters == null) {
            referenceParameters = new HashMap();
        }
        referenceParameters.put(omElement.getQName(), omElement);
    }

    /**
     * Remember that reference properties are only supported in WS-A Submission version.
     *
     * @param qname
     * @param value
     */
    public void addReferenceProperty(QName qname, String value) {
        if (qname == null) {
            return;
        }
        OMElement omElement = OMAbstractFactory.getOMFactory().createOMElement(qname, null);
        omElement.setText(value);
        addReferenceProperty(omElement);
    }

    /**
     * Remember that reference properties are only supported in WS-A Submission version.
     *
     * @param omElement
     */
    public void addReferenceProperty(OMElement omElement) {
        if (omElement == null) {
            return;
        }
        if (referenceProperties == null) {
            referenceProperties = new HashMap();
        }
        referenceProperties.put(omElement.getQName(), omElement);
    }


}
