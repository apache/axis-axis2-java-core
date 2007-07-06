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

package test.interop.whitemesa.round2;

import org.apache.axis2.AxisFault;
import org.apache.axiom.soap.SOAPEnvelope;
import test.interop.whitemesa.SunClient;
import test.interop.whitemesa.SunClientUtil;
import test.interop.whitemesa.WhiteMesaIneterop;
import test.interop.whitemesa.round2.util.GroupbEcho2DStringArrayUtil;
import test.interop.whitemesa.round2.util.GroupbEchoNestedArrayUtil;
import test.interop.whitemesa.round2.util.GroupbEchoNestedStructUtil;
import test.interop.whitemesa.round2.util.GroupbEchoSimpleTypesAsStructUtil;
import test.interop.whitemesa.round2.util.GroupbEchoStructAsSimpleTypesUtil;
import test.interop.whitemesa.round2.util.GroupcVoidUtil;

import java.io.File;

/**
 * class  MStk2Round2InteropTest
 * To test Interoperability Axis2 clients vs MS SOAP ToolKit 2.0 Server, Round2
 * WSDLs:-
 * Group b         http://mssoapinterop.org/stk/InteropB.wsdl
 * group b(Typed)  http://mssoapinterop.org/stk/InteropBtyped.wsdl
 * Group c         http://mssoapinterop.org/stk/InteropC.wsdl
 */

public class MStk2Round2InteropTest extends WhiteMesaIneterop {

    SOAPEnvelope retEnv = null;
    File file = null;
    String url = "";
    String soapAction = "";
    String resFilePath = "interop/whitemesa/round2/";
    String tempPath = "";
    SunClientUtil util;
    SunClient client = new SunClient();

    /**
     * Round2
     * Group B
     * operation echoStructAsSimpleTypes
     */
    public void testR2GBEchoStructAsSimpleTypes() throws AxisFault {
        url = "http://mssoapinterop.org/stk/interopB.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoStructAsSimpleTypesUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk2GroupbStructAsSimpleTypesRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B
     * operation echoSimpleTypesAsStruct
     */
    public void testR2GBEchoSimpleTypesAsStruct() throws AxisFault {
        url = "http://mssoapinterop.org/stk/interopB.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoSimpleTypesAsStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk2GroupbSimpletypesAsStructRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B
     * operation echo2DStringArray
     */
    public void testR2GBEcho2DStringArray() throws AxisFault {
        url = "http://mssoapinterop.org/stk/interopB.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEcho2DStringArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk2Groupb2DStringArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B typed
     * operation echoNestedStruct
     */
    public void testR2GBEchoNestedStruct() throws AxisFault {
        url = "http://mssoapinterop.org/stk/interopB.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoNestedStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk2GroupbNestedStructRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B
     * operation echoNestedArray
     */
    public void testR2GBEchoNestedArray() throws AxisFault {
        url = "http://mssoapinterop.org/stk/interopB.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoNestedArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk2GroupbNestedArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B Typed
     * operation echoStructAsSimpleTypes
     */
    public void testR2GBTypedEchoStructAsSimpleTypes() throws AxisFault {
        url = "http://mssoapinterop.org/stk/interopBTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoStructAsSimpleTypesUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk2GroupbTypedStructAsSimpleTypesRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B Typed
     * operation echoSimpleTypesAsStruct
     */
    public void testR2GBTypedEchoSimpleTypesAsStruct() throws AxisFault {
        url = "http://mssoapinterop.org/stk/interopBTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoSimpleTypesAsStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk2GroupbTypedSimpletypesAsStructRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B Typed
     * operation echo2DStringArray
     */
    public void testR2GBTypedEcho2DStringArray() throws AxisFault {
        url = "http://mssoapinterop.org/stk/interopBTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEcho2DStringArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk2GroupbTyped2DStringArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B Typed
     * operation echoNestedStruct
     */
    public void testR2GBTypedEchoNestedStruct() throws AxisFault {
        url = "http://mssoapinterop.org/stk/interopBTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoNestedStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk2GroupbTypedNestedStructRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B Typed
     * operation echoNestedArray
     */
    public void testR2GBTypedEchoNestedArray() throws AxisFault {
        url = "http://mssoapinterop.org/stk/interopBTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoNestedArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk2GroupTypedbNestedArrayRes.xml";
        compareXML(retEnv, tempPath);
    }


    /**
     * Round2
     * Group C
     * operation echoVoid
     */
    public void testR2GCEchoVoid() throws AxisFault {
        url = "http://mssoapinterop.org/stk/InteropC.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupcVoidUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk2GroupcVoidRes.xml";
        compareXML(retEnv, tempPath);
    }
}

