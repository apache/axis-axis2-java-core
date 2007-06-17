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

package test.interop.whitemesa.round2.Soap12;

import org.apache.axis2.AxisFault;
import org.apache.axiom.soap.SOAPEnvelope;
import test.interop.whitemesa.SunClient;
import test.interop.whitemesa.SunClientUtil;
import test.interop.whitemesa.WhiteMesaIneterop;
import test.interop.whitemesa.round2.util.soap12.GroupbSoap12Echo2DStringArrayUtil;
import test.interop.whitemesa.round2.util.soap12.GroupbSoap12EchoNestedArrayUtil;
import test.interop.whitemesa.round2.util.soap12.GroupbSoap12EchoNestedStructUtil;
import test.interop.whitemesa.round2.util.soap12.GroupbSoap12EchoSimpleTypesAsStructUtil;
import test.interop.whitemesa.round2.util.soap12.GroupbSoap12EchoStructAsSimpleTypesUtil;
import test.interop.whitemesa.round2.util.soap12.Round2SOAP12EchoIntegerArrayclientUtil;
import test.interop.whitemesa.round2.util.soap12.Round2Soap12EchoBase64ClientUtil;
import test.interop.whitemesa.round2.util.soap12.Round2Soap12EchoBooleanClientUtil;
import test.interop.whitemesa.round2.util.soap12.Round2Soap12EchoDateClientUtil;
import test.interop.whitemesa.round2.util.soap12.Round2Soap12EchoDecimalClientUtil;
import test.interop.whitemesa.round2.util.soap12.Round2Soap12EchoFloatArrayClientUtil;
import test.interop.whitemesa.round2.util.soap12.Round2Soap12EchoFloatClientUtil;
import test.interop.whitemesa.round2.util.soap12.Round2Soap12EchoHexBinaryUtil;
import test.interop.whitemesa.round2.util.soap12.Round2Soap12EchoStructArrayClientUtil;
import test.interop.whitemesa.round2.util.soap12.Round2Soap12EchoStructClientUtil;
import test.interop.whitemesa.round2.util.soap12.Round2Soap12EchoVoidClientUtil;
import test.interop.whitemesa.round2.util.soap12.Round2Soap12IntegerUtil;
import test.interop.whitemesa.round2.util.soap12.Round2Soap12StringArrayUtil;
import test.interop.whitemesa.round2.util.soap12.Round2Soap12StringUtil;

import java.io.File;


/**
 * class
 * To test Interoperability Axis2 clients vs sun Server, Round2
 * WSDLs:-
 * "base"     http://soapinterop.java.sun.com/round2/base?WSDL
 * "Group B"  http://soapinterop.java.sun.com/round2/groupb?WSDL
 * "Group C"  http://soapinterop.java.sun.com/round2/groupc?WSDL
 * <p/>
 * Todo - Some test cases fail.
 */

public class GSoapRound2Soap12InteropTest extends WhiteMesaIneterop {

    SOAPEnvelope retEnv = null;
    File file = null;
    String url = "";
    String soapAction = "";
    String resFilePath = "interop/whitemesa/round2/SOAP12/";
    String tempPath = "";
    SunClientUtil util;
    SunClient client = new SunClient();

    /**
     * Round2
     * Group Base
     * operation echoString
     */
    public void testR2BaseEchoString() throws AxisFault {
        url = "http://websrv.cs.fsu.edu/~engelen/interop2.cgi";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12StringUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GSoapR2_S12_StringRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base
     * operation echoStringArray
     */
    public void testR2BaseEchoStringArray() throws AxisFault {
        url = "http://websrv.cs.fsu.edu/~engelen/interop2.cgi";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12StringArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GSoapR2_S12_StringArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base
     * operation echoInteger
     */
    public void testR2BaseEchoInteger() throws AxisFault {
        url = "http://websrv.cs.fsu.edu/~engelen/interop2.cgi";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12IntegerUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GSoapR2_S12_IntegerRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base
     * operation echoIntegerArray
     */
    public void testR2BaseEchoIntegerArray() throws AxisFault {
        url = "http://websrv.cs.fsu.edu/~engelen/interop2.cgi";
        soapAction = "http://soapinterop.org/";

        util = new Round2SOAP12EchoIntegerArrayclientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GSoapR2_S12_IntegerArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base
     * operation echoFloat
     */
    public void testR2BaseEchoFloat() throws AxisFault {
        url = "http://websrv.cs.fsu.edu/~engelen/interop2.cgi";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoFloatClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GSoapR2_S12_FloatRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base
     * operation echoFloatArray
     */
    public void testR2BaseEchoFloatArray() throws AxisFault {
        url = "http://websrv.cs.fsu.edu/~engelen/interop2.cgi";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoFloatArrayClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GSoapR2_S12_FloatArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base
     * operation echoStruct
     */
    //Todo : This test fails due to Axis2 Client Error
    public void testRBaseEchoStruct() throws AxisFault {
        url = "http://websrv.cs.fsu.edu/~engelen/interop2.cgi";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoStructClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GSoapR2_S12_StructRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base
     * operation echoStructArray
     */
    public void testR2BaseEchoStructArray() throws AxisFault {
        url = "http://websrv.cs.fsu.edu/~engelen/interop2.cgi";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoStructArrayClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GSoapR2_S12_StructArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base
     * operation echoVoid
     */
    //Todo : This test fails due to Axis2 Client Error
    public void testR2BaseEchoVoid() throws AxisFault {
        url = "http://websrv.cs.fsu.edu/~engelen/interop2.cgi";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoVoidClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GSoapR2_S12_VoidRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base
     * operation echoBase64
     */
    public void testR2BaseEchoBase64() throws AxisFault {
        url = "http://websrv.cs.fsu.edu/~engelen/interop2.cgi";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoBase64ClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GSoapR2_S12_Base64Res.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base
     * operation echoBase64
     */
    public void testR2BaseEchoDate() throws AxisFault {
        url = "http://websrv.cs.fsu.edu/~engelen/interop2.cgi";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoDateClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GSoapR2_S12_DateRes.xml";
        compareXML(retEnv, tempPath);
    }


    /**
     * Round2
     * Group Base
     * operation echoHexBinary
     */
    public void testR2BaseEchoHexBinary() throws AxisFault {
        url = "http://websrv.cs.fsu.edu/~engelen/interop2.cgi";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoHexBinaryUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GSoapR2_S12_HexBinaryRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base
     * operation echoDecimal
     */
    public void testR2BaseEchoDecimal() throws AxisFault {
        url = "http://websrv.cs.fsu.edu/~engelen/interop2.cgi";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoDecimalClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GSoapR2_S12_DecimalRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group Base
     * operation echoBoolean
     */
    public void testR2BaseEchoBoolean() throws AxisFault {
        url = "http://websrv.cs.fsu.edu/~engelen/interop2.cgi";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoBooleanClientUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GSoapR2_S12_BooleanRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B
     * operation echoStructAsSimpleTypes
     */
    //Todo : This test fails due to Axis2 Client Error
    public void testR2GBEchoStructAsSimpleTypes() throws AxisFault {
        url = "http://websrv.cs.fsu.edu/~engelen/interop2B.cgi";
        soapAction = "http://soapinterop.org/";

        util = new GroupbSoap12EchoStructAsSimpleTypesUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GSoapR2Gb_S12_StructAsSimpleTypesRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B
     * operation echoSimpleTypesAsStruct
     */
    //Todo : This test fails due to Axis2 Client Error
    public void testR2GBEchoSimpleTypesAsStruct() throws AxisFault {
        url = "http://websrv.cs.fsu.edu/~engelen/interop2B.cgi";
        soapAction = "http://soapinterop.org/";

        util = new GroupbSoap12EchoSimpleTypesAsStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GSoapR2Gb_S12_SimpleTypesAsStructRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B
     * operation echo2DStringArray
     */
    public void testR2GBEcho2DStringArray() throws AxisFault {
        url = "http://websrv.cs.fsu.edu/~engelen/interop2B.cgi";
        soapAction = "http://soapinterop.org/";

        util = new GroupbSoap12Echo2DStringArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GSoapR2Gb_S12_2DStringArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B
     * operation echoNestedStruct
     */
    //Todo : This test fails due to Axis2 Client Error
    public void testR2GBEchoNestedStruct() throws AxisFault {
        url = "http://websrv.cs.fsu.edu/~engelen/interop2B.cgi";
        soapAction = "http://soapinterop.org/";

        util = new GroupbSoap12EchoNestedStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GSoapR2Gb_S12_NestedStructRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round2
     * Group B
     * operation echoNestedArray
     */
    //Todo : This test fails due to Axis2 Client Error
    public void testR2GBEchoNestedArray() throws AxisFault {
        url = "http://websrv.cs.fsu.edu/~engelen/interop2B.cgi";
        soapAction = "http://soapinterop.org/";

        util = new GroupbSoap12EchoNestedArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GSoapR2Gb_S12_NestedArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

}

