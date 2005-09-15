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

package org.apache.axis2.soap;

import org.apache.axis2.om.OMAbstractFactory;

public class SOAPFaultTestCase extends SOAPBodyTestCase {
    protected SOAPFault soap11Fault;
    protected SOAPFault soap12Fault;
    protected SOAPFault soap11FaultWithParser;
    protected SOAPFault soap12FaultWithParser;

    public SOAPFaultTestCase(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        soap11Fault =
                OMAbstractFactory.getSOAP11Factory().createSOAPFault(
                        soap11Body);
        soap12Fault =
                OMAbstractFactory.getSOAP12Factory().createSOAPFault(
                        soap12Body);
        soap11FaultWithParser = soap11BodyWithParser.getFault();
        soap12FaultWithParser = soap12BodyWithParser.getFault();
    }
}
