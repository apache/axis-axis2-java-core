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
package org.apache.axis.addressing;

import javax.xml.namespace.QName;

/**
 * Class EndpointReference
 */
public class EndpointReference {
    /**
     * this can be one of the followings
     * AddressingConstants.WSA_FROM
     * AddressingConstants.WSA_REPLY_TO
     * AddressingConstants.WSA_FAULT_TO
     */
    private String messageInformationHeaderType;

    /**
     * Field address
     */
    private String address;

    /**
     * Field portType
     */
    private QName portType;

    /**
     * Field referenceProperties
     */
    private AnyContentType referenceProperties;

    /**
     * Field referenceParameters
     */
    private AnyContentType referenceParameters;

    /**
     * Field serviceName
     */
    private ServiceName serviceName;

    /**
     * @param messageInformationHeaderType this can be one of the followings
     *                                     AddressingConstants.WSA_FROM
     *                                     AddressingConstants.WSA_REPLY_TO
     *                                     AddressingConstants.WSA_FAULT_TO
     * @param address
     */
    public EndpointReference(String messageInformationHeaderType,
                             String address) {
        this.messageInformationHeaderType = messageInformationHeaderType;
        this.address = address;
    }

    /**
     * Method getMessageInformationHeaderType
     *
     * @return
     */
    public String getMessageInformationHeaderType() {
        return messageInformationHeaderType;
    }

    /**
     * Method setMessageInformationHeaderType
     *
     * @param messageInformationHeaderType
     */
    public void setMessageInformationHeaderType(
            String messageInformationHeaderType) {
        this.messageInformationHeaderType = messageInformationHeaderType;
    }

    /**
     * Method getAddress
     *
     * @return
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
     * Method getPortType
     *
     * @return
     */
    public QName getPortType() {
        return portType;
    }

    /**
     * Method setPortType
     *
     * @param portType
     */
    public void setPortType(QName portType) {
        this.portType = portType;
    }

    /**
     * Method getReferenceProperties
     *
     * @return
     */
    public AnyContentType getReferenceProperties() {
        return referenceProperties;
    }

    /**
     * Method setReferenceProperties
     *
     * @param referenceProperties
     */
    public void setReferenceProperties(AnyContentType referenceProperties) {
        this.referenceProperties = referenceProperties;
    }

    /**
     * Method getReferenceParameters
     *
     * @return
     */
    public AnyContentType getReferenceParameters() {
        return referenceParameters;
    }

    /**
     * Method setReferenceParameters
     *
     * @param referenceParameters
     */
    public void setReferenceParameters(AnyContentType referenceParameters) {
        this.referenceParameters = referenceParameters;
    }

    /**
     * Method getServiceName
     *
     * @return
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
}
