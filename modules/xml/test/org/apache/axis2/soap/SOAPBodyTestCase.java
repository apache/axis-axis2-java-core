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

public class SOAPBodyTestCase extends SOAPTestCase {
    protected SOAPBody soap11Body;
    protected SOAPBody soap12Body;

    protected SOAPBody soap11BodyWithParser;
    protected SOAPBody soap12BodyWithParser;

    public SOAPBodyTestCase(String testName) {
        super(testName);

    }

    protected void setUp() throws Exception {
        super.setUp();
        soap11Body = soap11Factory.createSOAPBody(soap11Envelope);
        soap12Body = soap12Factory.createSOAPBody(soap12Envelope);

        soap11BodyWithParser = soap11EnvelopeWithParser.getBody();
        soap12BodyWithParser = soap12EnvelopeWithParser.getBody();
    }

}
