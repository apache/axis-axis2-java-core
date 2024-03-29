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

package org.apache.axis2.saaj;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SAAJMetaFactory;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFactory;

public class SAAJMetaFactoryImpl extends SAAJMetaFactory {
    protected MessageFactory newMessageFactory(String soapVersion) throws SOAPException {
        if (!(SOAPConstants.SOAP_1_1_PROTOCOL.equals(soapVersion) ||
                SOAPConstants.SOAP_1_2_PROTOCOL.equals(soapVersion) ||
                SOAPConstants.DYNAMIC_SOAP_PROTOCOL.equals(soapVersion))) {
            throw new SOAPException("Invalid SOAP Protocol Version");
        }
        MessageFactoryImpl factory = new MessageFactoryImpl();
        factory.setSOAPVersion(soapVersion);
        return factory;
    }

    protected SOAPFactory newSOAPFactory(String soapVersion) throws SOAPException {
        if (!(SOAPConstants.SOAP_1_1_PROTOCOL.equals(soapVersion) ||
                SOAPConstants.SOAP_1_2_PROTOCOL.equals(soapVersion) ||
                SOAPConstants.DYNAMIC_SOAP_PROTOCOL.equals(soapVersion))) {
            throw new SOAPException("Invalid SOAP Protocol Version");
        }
        SOAPFactoryImpl factory = new SOAPFactoryImpl();
        factory.setSOAPVersion(soapVersion);
        return factory;
    }
}
