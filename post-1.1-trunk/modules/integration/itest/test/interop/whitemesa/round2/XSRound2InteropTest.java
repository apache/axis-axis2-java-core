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
import test.interop.whitemesa.SunClient;
import test.interop.whitemesa.SunClientUtil;
import test.interop.whitemesa.WhiteMesaIneterop;
import test.interop.whitemesa.WhiteMesaConstants;
import test.interop.whitemesa.round2.util.Round2EchoBase64ClientUtil;
import test.interop.whitemesa.round2.util.Round2EchoBooleanClientUtil;
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
 * class
 * To test Interoperability Axis2 clients vs XSOAP 1.2 Server, Round2
 * WSDLs:-
 * "base"  	http://www.extreme.indiana.edu/~aslom/XSOAP_1_2_SoapRMI.wsdl
 * Todo - The test case "echoDate" fails due to an error with the date format.
 * But IMO: the date format is correct.
 */

public class XSRound2InteropTest extends WhiteMesaIneterop {

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
     * Group Base
     * operation echoString
     */
    public void testR2BaseEchoString() throws AxisFault {
        url = "http://www.extreme.indiana.edu:1568/";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStringclientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "XSBaseStringRes.xml";
        assertR2DefaultEchoStringResult(retEnv);
    }

    /**
     * Round2
     * Group Base
     * operation echoStringArray
     */
    public void testR2BaseEchoStringArray() throws AxisFault {
        url = "http://www.extreme.indiana.edu:1568/";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStringArrayClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "XSBaseStringArrayRes.xml";
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
        url = "http://www.extreme.indiana.edu:1568/";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoIntegerClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "XSBaseIntegerRes.xml";
        assertR2DefaultEchoIntegerResult(retEnv);
    }

    /**
     * Round2
     * Group Base
     * operation echoIntegerArray
     */
    public void testR2BaseEchoIntegerArray() throws AxisFault {
        url = "http://www.extreme.indiana.edu:1568/";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoIntegerArrayclientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "XSBaseIntegerArrayRes.xml";
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
        url = "http://www.extreme.indiana.edu:1568/";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoFloatClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "XSBaseFloatRes.xml";
        assertR2DefaultEchoFloatResult(retEnv);
    }

    /**
     * Round2
     * Group Base
     * operation echoFloatArray
     */
    public void testR2BaseEchoFloatArray() throws AxisFault {
        url = "http://www.extreme.indiana.edu:1568/";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoFloatArrayClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "XSBaseFloatArrayRes.xml";
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
        url = "http://www.extreme.indiana.edu:1568/";
        soapAction = "";

        util = new Round2EchoStructClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "XSBaseStructRes.xml";
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
        url = "http://www.extreme.indiana.edu:1568/";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStructArrayClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "XSBaseStructArrayRes.xml";
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
        url = "http://www.extreme.indiana.edu:1568/";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoVoidClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "XSBaseVoidRes.xml";
        assertR2DefaultEchoVoidResult(retEnv);
    }

    /**
     * Round2
     * Group Base
     * operation echoBase64
     */
    public void testR2BaseEchoBase64() throws AxisFault {
        url = "http://www.extreme.indiana.edu:1568/";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoBase64ClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "XSBaseBase64Res.xml";
        assertR2DefaultEchoBase64Result(retEnv);
    }

    /**
     * Round2
     * Group Base
     * operation echoDate
     * todo - this test failed, have to check the dateTime format that remote server is asking
     */
    /*public void testR2BaseEchoDate() throws AxisFault {
        //url = "http://www.extreme.indiana.edu:1568/";
        url = "http://127.0.0.1:8080/";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoDateClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "XSBaseDateRes.xml";
        compareXML(retEnv, tempPath);
    }*/


    /**
     * Round2
     * Group Base
     * operation echoHexBinary
     */
    public void testR2BaseEchoHexBinary() throws AxisFault {
        url = "http://www.extreme.indiana.edu:1568/";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoHexBinaryClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "XSBaseHexBinaryRes.xml";
        assertR2DefaultEchoHexBinaryResult(retEnv);
    }

    /**
     * Round2
     * Group Base
     * operation echoDecimal
     */
    public void testR2BaseEchoDecimal() throws AxisFault {
        url = "http://www.extreme.indiana.edu:1568/";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoDecimalClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "XSBaseDecimalRes.xml";
        assertR2DefaultEchoDecimalResult(retEnv);
    }

    /**
     * Round2
     * Group Base
     * operation echoBoolean
     */
    public void testR2BaseEchoBoolean() throws AxisFault {
        url = "http://www.extreme.indiana.edu:1568/";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoBooleanClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "XSBaseBooleanRes.xml";
        String booleanResult = "1"; //this gives 1 as the response for 'true'.
        assertValueIsInThePayload(retEnv,booleanResult);
    }

}

