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

package org.apache.axis2.interop.whitmesa.round2.Soap12;

import org.apache.axis2.AxisFault;
import org.apache.axis2.interop.whitemesa.WhiteMesaIneterop;
import org.apache.axis2.interop.whitemesa.round2.SunRound2Client;
import org.apache.axis2.interop.whitemesa.round2.util.SunRound2ClientUtil;
import org.apache.axis2.interop.whitemesa.round2.util.soap12.*;
import org.apache.axis2.soap.SOAPEnvelope;

import java.io.File;

/**
 * class
 * To test Interoperability Axis2 clients vs sun Server, Round2
 * WSDLs:-
 * "base"     http://soapinterop.java.sun.com/round2/base?WSDL
 * "Group B"  http://soapinterop.java.sun.com/round2/groupb?WSDL
 * "Group C"  http://soapinterop.java.sun.com/round2/groupc?WSDL
 */

public class WMRound2Soap12InteropTest extends WhiteMesaIneterop {

    SOAPEnvelope retEnv = null;
    boolean success = false;
    File file = null;
    String url = "";
    String soapAction = "";
    String resFilePath = "interop/whitemesa/round2/SOAP12/";
    String tempPath = "";
    SunRound2ClientUtil util;
    private boolean results = false;

    /**
     * Round2
     * Group Base
     * operation echoString
     */
    public void testR2BaseEchoString() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/base";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12StringUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMR2_S12_StringRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoStringArray
     */
    public void testR2BaseEchoStringArray() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/base";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12StringArrayUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMR2_S12_StringArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoInteger
     */
    public void testR2BaseEchoInteger() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/base";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12IntegerUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMR2_S12_IntegerRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoIntegerArray
     */
    public void testR2BaseEchoIntegerArray() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/base";
        soapAction = "http://soapinterop.org/";

        util = new Round2SOAP12EchoIntegerArrayclientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMR2_S12_IntegerArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoFloat
     */
    public void testR2BaseEchoFloat() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/base";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoFloatClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMR2_S12_FloatRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoFloatArray
     */
    public void testR2BaseEchoFloatArray() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/base";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoFloatArrayClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMR2_S12_FloatArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoStruct
     */
    public void testRBaseEchoStruct() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/base";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoStructClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMR2_S12_StructRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoStructArray
     */
    public void testR2BaseEchoStructArray() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/base";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoStructArrayClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMR2_S12_StructArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoVoid
     */
    public void testR2BaseEchoVoid() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/base";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoVoidClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMR2_S12_VoidRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoBase64
     */
    public void testR2BaseEchoBase64() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/base";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoBase64ClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMR2_S12_Base64Res.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoBase64
     */
    public void testR2BaseEchoDate() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/base";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoDateClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMR2_S12_DateRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }


    /**
     * Round2
     * Group Base
     * operation echoHexBinary
     */
    public void testR2BaseEchoHexBinary() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/base";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoHexBinaryUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMR2_S12_HexBinaryRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoDecimal
     */
    public void testR2BaseEchoDecimal() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/base";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoDecimalClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMR2_S12_DecimalRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoBoolean
     */
    public void testR2BaseEchoBoolean() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/base";
        soapAction = "http://soapinterop.org/";

        util = new Round2Soap12EchoBooleanClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMR2_S12_BooleanRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group B
     * operation echoStructAsSimpleTypes
     */
    public void testR2GBEchoStructAsSimpleTypes() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/groupB";
        soapAction = "http://soapinterop.org/";

        util = new GroupbSoap12EchoStructAsSimpleTypesUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMR2Gb_S12_StructAsSimpleTypesRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group B
     * operation echoSimpleTypesAsStruct
     */
    public void testR2GBEchoSimpleTypesAsStruct() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/groupB";
        soapAction = "http://soapinterop.org/";

        util = new GroupbSoap12EchoSimpleTypesAsStructUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMR2Gb_S12_SimpleTypesAsStructRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group B
     * operation echo2DStringArray
     */
    public void testR2GBEcho2DStringArray() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/groupB";
        soapAction = "http://soapinterop.org/";

        util = new GroupbSoap12Echo2DStringArrayUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMR2Gb_S12_2DStringArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group B
     * operation echoNestedStruct
     */
    public void testR2GBEchoNestedStruct() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/groupB";
        soapAction = "http://soapinterop.org/";

        util = new GroupbSoap12EchoNestedStructUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMR2Gb_S12_NestedStructRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group B
     * operation echoNestedArray
     */
    public void testR2GBEchoNestedArray() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/groupB";
        soapAction = "http://soapinterop.org/";

        util = new GroupbSoap12EchoNestedArrayUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMR2Gb_S12_NestedArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group C
     * operation echoString
     */
    public void testR2GCEchoString() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/groupC";
        soapAction = "http://soapinterop.org/";

        util = new WMRound2Soap12GroupcEchoStringUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMRound2Soap12GroupcEchoStringRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group C
     * operation echoInterger
     */
    public void testR2GCEchoInterger() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/groupC";
        soapAction = "http://soapinterop.org/";

        util = new WMRound2Soap12GroupcIntergerUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMRound2Soap12GroupcIntergerRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group C
     * operation echoStringArray
     */
    public void testR2GCEchoStringArray() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/groupC";
        soapAction = "http://soapinterop.org/";

        util = new WMRound2Soap12GroupcStringArrayUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMRound2Soap12GroupcStringArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group C
     * operation echoIntergerArray
     */
    public void testR2GCEchoIntergerArray() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/groupC";
        soapAction = "http://soapinterop.org/";

        util = new WMRound2Soap12GroupcIntegerArrayUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMRound2Soap12GroupcIntegerArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group C
     * operation echoFloat
     */
    public void testR2GCEchoFloat() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/groupC";
        soapAction = "http://soapinterop.org/";

        util = new WMRound2Saop12GroupcFloatUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMRound2Soap12GroupcFloatRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group C
     * operation echoFloatArray
     */
    public void testR2GCEchoFloatArray() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/groupC";
        soapAction = "http://soapinterop.org/";

        util = new WMRound2Soap12GroupcFloatArrayUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMRound2Soap12GroupcFloatArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group C
     * operation echoStruct
     */
    public void testR2GCEchoStruct() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/groupC";
        soapAction = "http://soapinterop.org/";

        util = new WMRound2Soap12GroupcStructUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMRound2Soap12GroupcStructRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group C
     * operation echoStructArray
     */
    public void testR2GCEchoStructArray() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/groupC";
        soapAction = "http://soapinterop.org/";

        util = new WMRound2Soap12GroupcStructArrayUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMRound2Soap12GroupcStructArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group C
     * operation echoVoid
     */
    public void testR2GCEchoVoid() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/groupC";
        soapAction = "http://soapinterop.org/";

        util = new WMRound2Soap12GroupcVoidUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMRound2Soap12GroupcVoidRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group C
     * operation echoBase64
     */
    public void testR2GCEchoBase64() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/groupC";
        soapAction = "http://soapinterop.org/";

        util = new WMRound2Soap12GroupcBase64Util();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMRound2Soap12GroupcBase64Res.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group C
     * operation echoHexBinary
     */
    public void testR2GCEchoHexBinary() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/groupC";
        soapAction = "http://soapinterop.org/";

        util = new WMRound2Soap12GroupcHexBinaryUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMRound2Soap12GroupcHexBinaryRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group C
     * operation echoBoolean
     */
    public void testR2GCEchoBoolean() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r2/groupC";
        soapAction = "http://soapinterop.org/";

        util = new WMRound2Soap12GroupcBooleanUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMRound2Soap12GroupcBooleanRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

//    private static boolean compare(SOAPEnvelope retEnv, String filePath) throws AxisFault {
//
//        boolean ok = false;
//        try {
//            if (retEnv != null) {
//                SOAPBody body = retEnv.getBody();
//                if (!body.hasFault()) {
//                    //OMElement firstChild = (OMElement) body.getFirstElement();
//                    InputStream stream = GSoapRound2Soap12InteropTest.class.getClassLoader().getResourceAsStream(filePath);
//                    XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(stream);
//                    OMXMLParserWrapper builder = new StAXSOAPModelBuilder(parser, null);
//                    SOAPEnvelope refEnv = (SOAPEnvelope) builder.getDocumentElement();
//                    //OMElement refNode = (OMElement) resEnv.getBody().getFirstElement();
//                    XMLComparator comparator = new XMLComparator();
//                    ok = comparator.compare(retEnv, refEnv);
//                } else
//                    return false;
//            } else
//                return false;
//
//        } catch (Exception e) {
//            throw new AxisFault(e); //To change body of catch statement use File | Settings | File Templates.
//        }
//        return ok;
//    }
}

