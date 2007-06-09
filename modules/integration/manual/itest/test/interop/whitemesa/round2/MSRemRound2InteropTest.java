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
 * class MSRemRound2InteropTest
 * To test Interoperability Axis2 clients vs MS NET Remoting Server, Round2
 * WSDLs:-
 * "base"    http://www.mssoapinterop.org/remoting/ServiceA.soap?wsdl
 * "Group B" http://www.mssoapinterop.org/remoting/ServiceB.soap?wsdl
 * <p/>
 * All Tests failed due to a server error. The endpoint might have removed or deprecated.
 * Todo - Shall we remove this Test Case?
 */

/**
 * EchoDate testcase is failing since the result is sent in a diferent date format
 * request :2006-10-18T22:20:00-07:00
 * response:2006-10-18T22:20:00.0000000-07:00
 */

public class MSRemRound2InteropTest extends WhiteMesaIneterop {

    SOAPEnvelope retEnv = null;
    File file = null;
    String url = "";
    String soapAction = "";
    String resFilePath = "interop/whitemesa/round2/";
    String tempPath = "";
    SunClientUtil util;
    SunClient client = new SunClient();
    
	private static final Log log = LogFactory.getLog(MSRemRound2InteropTest.class);

    /**
     * Round2
     * Group Base
     * operation echoString
     */
    public void testR2BaseEchoString() throws AxisFault {
        url = "http://www.mssoapinterop.org:80/Remoting/ServiceA.soap";
        try {
			soapAction = "http://soapinterop.org/";

			util = new Round2EchoStringclientUtil();
			retEnv = client.sendMsg(util, url, soapAction);
			tempPath = resFilePath + "MSRemBaseStringRes.xml";

			assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRING);
		} catch (AxisFault e) {
			e.printStackTrace();
		}
    }

    /**
     * Round2
     * Group Base
     * operation echoStringArray
     */
    public void testR2BaseEchoStringArray() throws AxisFault {
        url = "http://www.mssoapinterop.org:80/Remoting/ServiceA.soap";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStringArrayClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSRemBaseStringArrayRes.xml";

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
        url = "http://www.mssoapinterop.org:80/Remoting/ServiceA.soap";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoIntegerClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSRemBaseIntegerRes.xml";

        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_INTEGER);
    }

    /**
     * Round2
     * Group Base
     * operation echoIntegerArray
     */
    public void testR2BaseEchoIntegerArray() throws AxisFault {
        url = "http://www.mssoapinterop.org:80/Remoting/ServiceA.soap";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoIntegerArrayclientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSRemBaseIntegerArrayRes.xml";

    }

    /**
     * Round2
     * Group Base
     * operation echoFloat
     */
    public void testR2BaseEchoFloat() throws AxisFault {
        url = "http://www.mssoapinterop.org:80/Remoting/ServiceA.soap";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoFloatClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSRemBaseFloatRes.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_FLOAT);
    }

    /**
     * Round2
     * Group Base
     * operation echoFloatArray
     */
    public void testR2BaseEchoFloatArray() throws AxisFault {
        url = "http://www.mssoapinterop.org:80/Remoting/ServiceA.soap";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoFloatArrayClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSRemBaseFloatArrayRes.xml";
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
        url = "http://www.mssoapinterop.org:80/Remoting/ServiceA.soap";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStructClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSRemBaseStructRes.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_INT);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_FLOAT);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_STRING);
    }

    /**
     * Round2
     * Group Base
     * operation echoStructArray
     */
    public void testR2BaseEchoStructArray() throws AxisFault {
        url = "http://www.mssoapinterop.org:80/Remoting/ServiceA.soap";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStructArrayClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSRemBaseStructArrayRes.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_ARRAY_STR_1);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_ARRAY_STR_2);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_ARRAY_STR_3);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_ARRAY_FLOAT_1);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_ARRAY_FLOAT_2);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_ARRAY_FLOAT_3);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_ARRAY_INT_1);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_ARRAY_INT_2);
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_STRUCT_ARRAY_INT_3);
    }

    /**
     * Round2
     * Group Base
     * operation echoVoid
     */
    public void testR2BaseEchoVoid() throws AxisFault {
        url = "http://www.mssoapinterop.org:80/Remoting/ServiceA.soap";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoVoidClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSRemBaseVoidRes.xml";

    }

    /**
     * Round2
     * Group Base
     * operation echoBase64
     */
    public void testR2BaseEchoBase64() throws AxisFault {
        url = "http://www.mssoapinterop.org:80/Remoting/ServiceA.soap";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoBase64ClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSRemBaseBase64Res.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_BASE_64);
    }

    /**
     * Round2
     * Group Base
     * operation echoBase64
     */
    public void testR2BaseEchoDate() throws AxisFault {


        log.info("This may fail if the echoed date format is different");    

    	url = "http://www.mssoapinterop.org:80/Remoting/ServiceA.soap";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoDateClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSRemBaseDateRes.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_DATE);
    }


    /**
     * Round2
     * Group Base
     * operation echoHexBinary
     */
    public void testR2BaseEchoHexBinary() throws AxisFault {
        url = "http://www.mssoapinterop.org:80/Remoting/ServiceA.soap";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoHexBinaryClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSRemBaseHexBinaryRes.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_HEX_BINARY);
    }

    /**
     * Round2
     * Group Base
     * operation echoDecimal
     */
    public void testR2BaseEchoDecimal() throws AxisFault {
        url = "http://www.mssoapinterop.org:80/Remoting/ServiceA.soap";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoDecimalClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSRemBaseDecimalRes.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_DECIMAL);
    }

    /**
     * Round2
     * Group Base
     * operation echoBoolean
     */
    public void testR2BaseEchoBoolean() throws AxisFault {
        url = "http://www.mssoapinterop.org:80/Remoting/ServiceA.soap";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoBooleanClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSRemBaseBooleanRes.xml";
        assertValueIsInThePayload(retEnv,WhiteMesaConstants.ECHO_BOOLEAN);
    }

    /**
     * Round2
     * Group B
     * operation echoStructAsSimpleTypes
     */
    public void testR2GBEchoStructAsSimpleTypes() throws AxisFault {
        url = "http://www.mssoapinterop.org:80/Remoting/ServiceB.soap";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoStructAsSimpleTypesUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSRemGroupbStructAsSimpleTypesRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B
     * operation echoSimpleTypesAsStruct
     */
    public void testR2GBEchoSimpleTypesAsStruct() throws AxisFault {
        url = "http://www.mssoapinterop.org:80/Remoting/ServiceB.soap";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoSimpleTypesAsStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSRemGroupbSimpletypesAsStructRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B
     * operation echo2DStringArray
     */
    public void testR2GBEcho2DStringArray() throws AxisFault {
        url = "http://www.mssoapinterop.org:80/Remoting/ServiceB.soap";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEcho2DStringArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSRemGroupb2DStringArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B
     * operation echoNestedStruct
     */
    public void testR2GBEchoNestedStruct() throws AxisFault {
        url = "http://www.mssoapinterop.org:80/Remoting/ServiceB.soap";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoNestedStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSRemGroupbNestedStructRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B
     * operation echoNestedArray
     */
    public void testR2GBEchoNestedArray() throws AxisFault {
        url = "http://www.mssoapinterop.org:80/Remoting/ServiceB.soap";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoNestedArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSRemGroupbNestedArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

}

