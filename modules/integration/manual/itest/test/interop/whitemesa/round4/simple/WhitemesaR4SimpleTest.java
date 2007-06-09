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

package test.interop.whitemesa.round4.simple;

import org.apache.axiom.soap.SOAPEnvelope;
import test.interop.whitemesa.SunClient;
import test.interop.whitemesa.SunClientUtil;
import test.interop.whitemesa.WhiteMesaIneterop;
import test.interop.whitemesa.round4.simple.utils.EchoEmptyFaultClientUtil;
import test.interop.whitemesa.round4.simple.utils.EchoIntArrayFaultClientUtil;
import test.interop.whitemesa.round4.simple.utils.EchoMultipleFaults1ClientUtil;
import test.interop.whitemesa.round4.simple.utils.EchoMultipleFaults2ClientUtil;
import test.interop.whitemesa.round4.simple.utils.EchoMultipleFaults3Clientutil;
import test.interop.whitemesa.round4.simple.utils.EchoMultipleFaults4ClientUtil;
import test.interop.whitemesa.round4.simple.utils.EchoStringFaultClientUtil;

/**
 * Class WhitemesaR4SimpleTest
 * Group H - simple-rpc-encoded
 * WSDL:-
 * http://soapinterop.java.sun.com/round4/grouph/simplerpcenc?WSDL
 */

public class WhitemesaR4SimpleTest extends WhiteMesaIneterop {

    SunClient client = new SunClient();
    SOAPEnvelope retEnv = null;
    SunClientUtil util = null;
    String soapAction = "";
    String url = "http://soapinterop.java.sun.com:80/round4/grouph/simplerpcenc";

    //Operation - echoEmptyFault
    public void testEchoEmptyFault() {
        try {
            util = new EchoEmptyFaultClientUtil();
            retEnv = client.sendMsg(util, url, soapAction);
            fail("Internal Server Error");
        } catch (Exception e) {
        }
    }

    //Operation - echoStringFault
    public void testEchoStringFault() {
        try {
            util = new EchoStringFaultClientUtil();
            retEnv = client.sendMsg(util, url, soapAction);
            fail("Internal Server Error");
        } catch (Exception e) {
        }
    }

    //Operation - echoIntArrayFault
    public void testEchoIntArrayFault() {
        try {
            util = new EchoIntArrayFaultClientUtil();
            retEnv = client.sendMsg(util, url, soapAction);
            fail("Internal Server Error");
        } catch (Exception e) {
        }
    }

    //Operation - echoMultipleFaults1
    public void testEchoMultipleFaults1() {
        try {
            util = new EchoMultipleFaults1ClientUtil();
            retEnv = client.sendMsg(util, url, soapAction);
            fail("Internal Server Error");
        } catch (Exception e) {
        }
    }

    //Operation - echoMultipleFaults2
    public void testEchoMultipleFaults2() {
        try {
            util = new EchoMultipleFaults2ClientUtil();
            retEnv = client.sendMsg(util, url, soapAction);
            fail("Internal Server Error");
        } catch (Exception e) {
        }
    }

    //Operation - echoMultipleFaults3
    public void testEchoMultipleFaults3() {
        try {
            util = new EchoMultipleFaults3Clientutil();
            retEnv = client.sendMsg(util, url, soapAction);
            fail("Internal Server Error");
        } catch (Exception e) {
        }
    }

    //Operation - echoMultipleFaults4
    public void testEchoMultipleFaults4() {
        try {
            util = new EchoMultipleFaults4ClientUtil();
            retEnv = client.sendMsg(util, url, soapAction);
            fail("Internal Server Error");
        } catch (Exception e) {
        }
    }
}