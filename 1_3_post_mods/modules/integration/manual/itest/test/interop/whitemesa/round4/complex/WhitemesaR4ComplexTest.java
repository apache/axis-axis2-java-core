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

package test.interop.whitemesa.round4.complex;

import org.apache.axiom.soap.SOAPEnvelope;
import test.interop.whitemesa.SunClient;
import test.interop.whitemesa.SunClientUtil;
import test.interop.whitemesa.WhiteMesaIneterop;
import test.interop.whitemesa.round4.complex.utils.EchoBaseStructFaultClientutil;
import test.interop.whitemesa.round4.complex.utils.EchoExtendedStructFaultClientUtil;
import test.interop.whitemesa.round4.complex.utils.EchoMultipleFaults1ClientUtil;
import test.interop.whitemesa.round4.complex.utils.EchoMultipleFaults2ClientUtil;
import test.interop.whitemesa.round4.complex.utils.EchoSOAPStructFaultClientUtil;

/**
 * Class WhitemesaR4ComplexTest
 * Group H - complex-rpc-encoded
 * WSDL:-
 * http://soapinterop.java.sun.com/round4/grouph/complexrpcenc?WSDL
 */

public class WhitemesaR4ComplexTest extends WhiteMesaIneterop {
    SunClient client = new SunClient();
    SOAPEnvelope retEnv;
    SunClientUtil util = null;
    String soapAction = "";
    String url = "http://soapinterop.java.sun.com:80/round4/grouph/complexrpcenc";

    //echoSoapStructFault
    public void testSoapStructFault() {
        try {
            util = new EchoSOAPStructFaultClientUtil();
            retEnv = client.sendMsg(util, url, soapAction);
            fail("Internal Server Error");
        } catch (Exception e) {
        }
    }

    //echoBaseSoapStructFault
    public void testBaseStructFault() {
        try {
            util = new EchoBaseStructFaultClientutil();
            retEnv = client.sendMsg(util, url, soapAction);
            fail("Internal Server Error");
        } catch (Exception e) {
        }

    }

    //echoExtendedStructFault
    public void testExtendedStructFault() {
        try {
            util = new EchoExtendedStructFaultClientUtil();
            retEnv = client.sendMsg(util, url, soapAction);
            fail("Internal Server Error");
        } catch (Exception e) {
        }

    }

    //echomultiplefaults1
    public void testMultiplefaults1() {
        try {
            util = new EchoMultipleFaults1ClientUtil();
            retEnv = client.sendMsg(util, url, soapAction);
            fail("Internal Server Error");
        } catch (Exception e) {
        }
    }

    //echomultiplefaults2
    public void testMultiplefaults2() {
        try {
            util = new EchoMultipleFaults2ClientUtil();
            retEnv = client.sendMsg(util, url, soapAction);
            fail("Internal Server Error");
        } catch (Exception e) {
        }

    }

}


