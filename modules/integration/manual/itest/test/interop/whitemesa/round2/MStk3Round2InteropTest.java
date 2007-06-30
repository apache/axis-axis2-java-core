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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.interop.whitemesa.SunClient;
import test.interop.whitemesa.SunClientUtil;
import test.interop.whitemesa.WhiteMesaIneterop;
import test.interop.whitemesa.WhiteMesaConstants;
import test.interop.whitemesa.round2.util.GroupbEcho2DStringArrayUtil;
import test.interop.whitemesa.round2.util.GroupbEchoNestedArrayUtil;
import test.interop.whitemesa.round2.util.GroupbEchoNestedStructUtil;
import test.interop.whitemesa.round2.util.GroupbEchoSimpleTypesAsStructUtil;
import test.interop.whitemesa.round2.util.GroupbEchoStructAsSimpleTypesUtil;
import test.interop.whitemesa.round2.util.GroupcVoidUtil;
import test.interop.whitemesa.round2.util.Round2EchoBase64ClientUtil;
import test.interop.whitemesa.round2.util.Round2EchoBooleanClientUtil;
import test.interop.whitemesa.round2.util.Round2EchoDateClientUtil;
import test.interop.whitemesa.round2.util.Round2EchoDecimalClientUtil;
import test.interop.whitemesa.round2.util.Round2EchoFloatArrayClientUtil;
import test.interop.whitemesa.round2.util.Round2EchoFloatClientUtil;
import test.interop.whitemesa.round2.util.Round2EchoHexBinaryClientUtil;
import test.interop.whitemesa.round2.util.Round2EchoIntegerArrayclientUtil;
import test.interop.whitemesa.round2.util.Round2EchoIntegerClientUtil;
import test.interop.whitemesa.round2.util.Round2EchoStringArrayClientUtil;
import test.interop.whitemesa.round2.util.Round2EchoStringclientUtil;
import test.interop.whitemesa.round2.util.Round2EchoStructArrayClientUtil;
import test.interop.whitemesa.round2.util.Round2EchoStructClientUtil;
import test.interop.whitemesa.round2.util.Round2EchoVoidClientUtil;

import java.io.File;

/**
 * class  MStk3Round2InteropTest
 * To test Interoperability Axis2 clients vs MS SOAP ToolKit 3.0 Server, Round2
 * WSDLs:-
 * base            http://mssoapinterop.org/stkV3/Interop.wsdl
 * base (Typed)    http://mssoapinterop.org/stkV3/InteropTyped.wsdl
 * Group b         http://mssoapinterop.org/stkV3/InteropB.wsdl
 * Group b (Typed) http://mssoapinterop.org/stkV3/InteropBtyped.wsdl
 * Group c         http://mssoapinterop.org/stkV3/InteropC.wsdl
 */

/**
 * Some of the test cases that work on float values may fail since the endpoint seems to be sending
 * approximated values for e.g.: 45.7599983215332 as the echo of 45.76
 */

/**
 * EchoDate testcase is failing since the result is sent in a diferent date format
 * request :2006-10-18T22:20:00-07:00
 * response:2006-10-19T05:20:00Z
 */

public class MStk3Round2InteropTest extends WhiteMesaIneterop {

    SOAPEnvelope retEnv = null;
    File file = null;
    String url = "";
    String soapAction = "";
    String resFilePath = "interop/whitemesa/round2/";
    String tempPath = "";
    SunClientUtil util;
    SunClient client = new SunClient();
    
	private static final Log log = LogFactory.getLog(MStk3Round2InteropTest.class);

    /**
     * Round2
     * Group Base
     * operation echoString
     */
    public void testR2BaseEchoString() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/Interop.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStringclientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseStringRes.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRING);

    }

    /**
     * Round2
     * Group Base
     * operation echoStringArray
     */
    public void testR2BaseEchoStringArray() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/Interop.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStringArrayClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseStringArrayRes.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRING_ARR_1);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRING_ARR_2);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRING_ARR_3);


    }

    /**
     * Round2
     * Group Base
     * operation echoInteger
     */
    public void testR2BaseEchoInteger() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/Interop.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoIntegerClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseIntegerRes.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_INTEGER);

    }

    /**
     * Round2
     * Group Base
     * operation echoIntegerArray
     */
    public void testR2BaseEchoIntegerArray() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/Interop.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoIntegerArrayclientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseIntegerArrayRes.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_INTEGER_ARR_1);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_INTEGER_ARR_2);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_INTEGER_ARR_3);

    }

    /**
     * Round2
     * Group Base
     * operation echoFloat
     */
    public void testR2BaseEchoFloat() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/Interop.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoFloatClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseFloatRes.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_FLOAT);

    }

    /**
     * Round2
     * Group Base
     * operation echoFloatArray
     */
    public void testR2BaseEchoFloatArray() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/Interop.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoFloatArrayClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseFloatArrayRes.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_FLOAT_ARR_1);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_FLOAT_ARR_2);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_FLOAT_ARR_3);

    }

    /**
     * Round2
     * Group Base
     * operation echoStruct
     */
    public void testRBaseEchoStruct() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/Interop.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStructClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseStructRes.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_INT);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_STRING);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_FLOAT);

    }

    /**
     * Round2
     * Group Base
     * operation echoStructArray
     */
    public void testR2BaseEchoStructArray() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/Interop.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStructArrayClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseStructArrayRes.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_ARRAY_FLOAT_1);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_ARRAY_FLOAT_2);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_ARRAY_FLOAT_3);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_ARRAY_INT_1);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_ARRAY_INT_2);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_ARRAY_INT_3);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_ARRAY_STR_1);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_ARRAY_STR_2);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_ARRAY_STR_3);

    }

    /**
     * Round2
     * Group Base
     * operation echoVoid
     */
    public void testR2BaseEchoVoid() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/Interop.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoVoidClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseVoidRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base
     * operation echoBase64
     */
    public void testR2BaseEchoBase64() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/Interop.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoBase64ClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseBase64Res.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_BASE_64);
}

    /**
     * Round2
     * Group Base
     * operation echoBase64
     */
    public void testR2BaseEchoDate() throws AxisFault {

        log.info("This may fail if the echoed date format is different");    

    	url = "http://mssoapinterop.org/stkV3/Interop.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoDateClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseDateRes.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_DATE);

    }


    /**
     * Round2
     * Group Base
     * operation echoHexBinary
     */
    public void testR2BaseEchoHexBinary() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/Interop.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoHexBinaryClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseHexBinaryRes.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_HEX_BINARY);

    }

    /**
     * Round2
     * Group Base
     * operation echoDecimal
     */
    public void testR2BaseEchoDecimal() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/Interop.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoDecimalClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseDecimalRes.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_DECIMAL);

    }

    /**
     * Round2
     * Group Base
     * operation echoBoolean
     */
    public void testR2BaseEchoBoolean() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/Interop.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoBooleanClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseBooleanRes.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_BOOLEAN);

    }

    /**
     * Round2
     * Group Base Typed
     * operation echoString
     */
    public void testR2BaseTypedEchoString() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/InteropTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStringclientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3TypedBaseStringRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base Typed
     * operation echoStringArray
     */
    public void testR2BaseTypedEchoStringArray() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/InteropTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStringArrayClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseTypedStringArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base Typed
     * operation echoInteger
     */
    public void testR2BaseTypedEchoInteger() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/InteropTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoIntegerClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseTypedIntegerRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base Typed
     * operation echoIntegerArray
     */
    public void testR2BaseTypedEchoIntegerArray() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/InteropTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoIntegerArrayclientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseTypedIntegerArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base Typed
     * operation echoFloat
     */
    public void testR2BaseTypedEchoFloat() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/InteropTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoFloatClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseTypedFloatRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base Typed
     * operation echoFloatArray
     */
    public void testR2BaseTypedEchoFloatArray() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/InteropTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoFloatArrayClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseTypedFloatArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base Typed
     * operation echoStruct
     */
    public void testRBaseTypedEchoStruct() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/InteropTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStructClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseTypedStructRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base Typed
     * operation echoStructArray
     */
    public void testR2BaseTypedEchoStructArray() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/InteropTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStructArrayClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseTypedStructArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base Typed
     * operation echoVoid
     */
    public void testR2BaseTypedEchoVoid() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/InteropTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoVoidClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseTypedVoidRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base Typed
     * operation echoBase64
     */
    public void testR2BaseTypedEchoBase64() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/InteropTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoBase64ClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseTypedBase64Res.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base Typed
     * operation echoBase64
     */
    public void testR2BaseTypedEchoDate() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/InteropTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoDateClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseTypedDateRes.xml";
        compareXML(retEnv, tempPath);
    }


    /**
     * Round2
     * Group Base Typed
     * operation echoHexBinary
     */
    public void testR2BaseTypedEchoHexBinary() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/InteropTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoHexBinaryClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseTypedHexBinaryRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base Typed
     * operation echoDecimal
     */
    public void testR2BaseTypedEchoDecimal() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/InteropTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoDecimalClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseTypedDecimalRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base Typed
     * operation echoBoolean
     */
    public void testR2BaseTypedEchoBoolean() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/InteropTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoBooleanClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3BaseTypedBooleanRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B
     * operation echoStructAsSimpleTypes
     */
    public void testR2GBEchoStructAsSimpleTypes() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/interopB.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoStructAsSimpleTypesUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3GroupbStructAsSimpleTypesRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B
     * operation echoSimpleTypesAsStruct
     */
    public void testR2GBEchoSimpleTypesAsStruct() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/interopB.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoSimpleTypesAsStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3GroupbSimpletypesAsStructRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B
     * operation echo2DStringArray
     */
    public void testR2GBEcho2DStringArray() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/interopB.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEcho2DStringArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3Groupb2DStringArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B
     * operation echoNestedStruct
     */
    public void testR2GBEchoNestedStruct() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/interopB.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoNestedStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3GroupbNestedStructRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B
     * operation echoNestedArray
     */
    public void testR2GBEchoNestedArray() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/interopB.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoNestedArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3GroupbNestedArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B Typed
     * operation echoStructAsSimpleTypes
     */
    public void testR2GBTypedEchoStructAsSimpleTypes() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/interopBTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoStructAsSimpleTypesUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3GroupbTypedStructAsSimpleTypesRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B Typed
     * operation echoSimpleTypesAsStruct
     */
    public void testR2GBTypedEchoSimpleTypesAsStruct() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/interopBTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoSimpleTypesAsStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3GroupbTypedSimpletypesAsStructRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B Typed
     * operation echo2DStringArray
     */
    public void testR2GBTypedEcho2DStringArray() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/interopBTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEcho2DStringArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3GroupbTyped2DStringArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B Typed
     * operation echoNestedStruct
     */
    public void testR2GBTypedEchoNestedStruct() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/interopBTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoNestedStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3GroupbTypedNestedStructRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B Typed
     * operation echoNestedArray
     */
    public void testR2GBTypedEchoNestedArray() throws AxisFault {
        url = "http://mssoapinterop.org/stkV3/interopBTyped.wsdl";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoNestedArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MStk3GroupbTypedNestedArrayRes.xml";
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
        tempPath = resFilePath + "MStk3GroupcVoidRes.xml";
        compareXML(retEnv, tempPath);
    }
}

