package org.apache.axis.addressing;

import javax.xml.namespace.QName;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 */
public class EndpointReferenceType {

    /**
     * this can be one of the followings
     * AddressingConstants.WSA_FROM
     * AddressingConstants.WSA_REPLY_TO
     * AddressingConstants.WSA_FAULT_TO
     */
    private String messageInformationHeaderType;

    private String address;
    private QName portType;
    private AnyContentType referenceProperties;
    private AnyContentType referenceParameters;
    private ServiceName serviceName;

    /**
     *
     * @param messageInformationHeaderType
     * this can be one of the followings
     * AddressingConstants.WSA_FROM
     * AddressingConstants.WSA_REPLY_TO
     * AddressingConstants.WSA_FAULT_TO
     * 
     * @param address
     */
    public EndpointReferenceType(String messageInformationHeaderType, String address) {
        this.messageInformationHeaderType = messageInformationHeaderType;
        this.address = address;
    }

    public String getMessageInformationHeaderType() {
        return messageInformationHeaderType;
    }

    public void setMessageInformationHeaderType(String messageInformationHeaderType) {
        this.messageInformationHeaderType = messageInformationHeaderType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public QName getPortType() {
        return portType;
    }

    public void setPortType(QName portType) {
        this.portType = portType;
    }

    public AnyContentType getReferenceProperties() {
        return referenceProperties;
    }

    public void setReferenceProperties(AnyContentType referenceProperties) {
        this.referenceProperties = referenceProperties;
    }

    public AnyContentType getReferenceParameters() {
        return referenceParameters;
    }

    public void setReferenceParameters(AnyContentType referenceParameters) {
        this.referenceParameters = referenceParameters;
    }

    public ServiceName getServiceName() {
        return serviceName;
    }

    public void setServiceName(ServiceName serviceName) {
        this.serviceName = serviceName;
    }

}
