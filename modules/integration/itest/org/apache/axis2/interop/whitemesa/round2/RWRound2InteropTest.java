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

package org.apache.axis2.interop.whitemesa.round2;

import org.apache.axis2.AxisFault;
import org.apache.axis2.interop.whitemesa.WhiteMesaIneterop;
import org.apache.axis2.interop.whitemesa.round2.util.*;
import org.apache.axis2.soap.SOAPEnvelope;

import java.io.File;

/**
 * class  RWRound2InteropTest
 * To test Interoperability Axis2 clients vs Rogue Wave Server, Round2
 * WSDLs:-
 * "base"     http://soapinterop.roguewave.com:8013/interop2base/InteropRound2BaseTest
 * "Group B"  http://soapinterop.roguewave.com:8013/interop2testB/InteropRound2TestB
 * "Group C"  http://soapinterop.roguewave.com:8013/interop2testC/InteropRound2TestC
 */

public class RWRound2InteropTest extends WhiteMesaIneterop {

    SOAPEnvelope retEnv = null;
    boolean success = false;
    File file = null;
    String url = "";
    String soapAction = "";
    String FS = System.getProperty("file.separator");
    String resFilePath = "interop/whitemesa/round2/";
    String tempPath = "";
    SunRound2ClientUtil util;
    private boolean results = false;

    /**
     * Round2
     * Group Base
     * operation echoString
     */
    public void testR2BaseEchoString() throws AxisFault {
        url = "http://soapinterop.roguewave.com:8013/interop2base/InteropRound2BaseTest";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStringclientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "RWBaseStringRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoStringArray
     */
    //todo Test failed !!!
//    public void testR2BaseEchoStringArray() throws AxisFault {
//        url = "http://soapinterop.roguewave.com:8013/interop2base/InteropRound2BaseTest";
//        soapAction = "http://soapinterop.org/";
//
//        util = new Round2EchoStringArrayClientUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "RWBaseStringArrayRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }

    /**
     * Round2
     * Group Base
     * operation echoInteger
     */
    public void testR2BaseEchoInteger() throws AxisFault {
        url = "http://soapinterop.roguewave.com:8013/interop2base/InteropRound2BaseTest";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoIntegerClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "RWBaseIntegerRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoIntegerArray
     */
    //todo Test failed !!!
//    public void testR2BaseEchoIntegerArray() throws AxisFault  {
//        url = "http://soapinterop.roguewave.com:8013/interop2base/InteropRound2BaseTest";
//        soapAction = "http://soapinterop.org/";
//
//        util = new Round2EchoIntegerArrayclientUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "RWBaseIntegerArrayRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }

    /**
     * Round2
     * Group Base
     * operation echoFloat
     */
    public void testR2BaseEchoFloat()  throws AxisFault {
        url = "http://soapinterop.roguewave.com:8013/interop2base/InteropRound2BaseTest";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoFloatClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "RWBaseFloatRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoFloatArray
     */
    //todo Test failed !!!
//    public void testR2BaseEchoFloatArray()  throws AxisFault {
//        url = "http://soapinterop.roguewave.com:8013/interop2base/InteropRound2BaseTest";
//        soapAction = "http://soapinterop.org/";
//
//        util = new Round2EchoFloatArrayClientUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "RWBaseFloatArrayRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }

    /**
     * Round2
     * Group Base
     * operation echoStruct
     */
    public void testRBaseEchoStruct() throws AxisFault  {
        url = "http://soapinterop.roguewave.com:8013/interop2base/InteropRound2BaseTest";
        soapAction = "";

        util = new Round2EchoStructClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "RWBaseStructRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoStructArray
     */
    //todo Test failed !!!
//    public void testR2BaseEchoStructArray() throws AxisFault  {
//        url = "http://soapinterop.roguewave.com:8013/interop2base/InteropRound2BaseTest";
//        soapAction = "http://soapinterop.org/";
//
//        util = new Round2EchoStructArrayClientUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "RWBaseStructArrayRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }

    /**
     * Round2
     * Group Base
     * operation echoVoid
     */
    public void testR2BaseEchoVoid() throws AxisFault  {
        url = "http://soapinterop.roguewave.com:8013/interop2base/InteropRound2BaseTest";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoVoidClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "RWBaseVoidRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoBase64
     */
    public void testR2BaseEchoBase64() throws AxisFault  {
        url = "http://soapinterop.roguewave.com:8013/interop2base/InteropRound2BaseTest";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoBase64ClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "RWBaseBase64Res.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoBase64
     */
    public void testR2BaseEchoDate() throws AxisFault  {
        url = "http://soapinterop.roguewave.com:8013/interop2base/InteropRound2BaseTest";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoDateClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "RWBaseDateRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }


    /**
     * Round2
     * Group Base
     * operation echoHexBinary
     */
    public void testR2BaseEchoHexBinary() throws AxisFault  {
        url = "http://soapinterop.roguewave.com:8013/interop2base/InteropRound2BaseTest";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoHexBinaryClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "RWBaseHexBinaryRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoDecimal
     */
    public void testR2BaseEchoDecimal() throws AxisFault  {
        url = "http://soapinterop.roguewave.com:8013/interop2base/InteropRound2BaseTest";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoDecimalClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "RWBaseDecimalRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoBoolean
     */
    public void testR2BaseEchoBoolean() throws AxisFault  {
        url = "http://soapinterop.roguewave.com:8013/interop2base/InteropRound2BaseTest";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoBooleanClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "RWBaseBooleanRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group B
     * operation echoStructAsSimpleTypes
     */
    public void testR2GBEchoStructAsSimpleTypes() throws AxisFault {
        url = "http://soapinterop.roguewave.com:8013/interop2testB/InteropRound2TestB";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoStructAsSimpleTypesUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "RWGroupbStructAsSimpleTypesRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group B
     * operation echoSimpleTypesAsStruct
     */
    public void testR2GBEchoSimpleTypesAsStruct() throws AxisFault {
        url = "http://soapinterop.roguewave.com:8013/interop2testB/InteropRound2TestB";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoSimpleTypesAsStructUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "RWGroupbSimpletypesAsStructRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group B
     * operation echo2DStringArray
     */
    //todo Test failed !!!
//    public void testR2GBEcho2DStringArray() throws AxisFault {
//        url = "http://soapinterop.roguewave.com:8013/interop2testB/InteropRound2TestB";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupbEcho2DStringArrayUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "RWGroupb2DStringArrayRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }

    /**
     * Round2
     * Group B
     * operation echoNestedStruct
     */
    public void testR2GBEchoNestedStruct() throws AxisFault {
        url = "http://soapinterop.roguewave.com:8013/interop2testB/InteropRound2TestB";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoNestedStructUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "RWGroupbNestedStructRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group B
     * operation echoNestedArray
     */
    //todo Test failed !!!
//    public void testR2GBEchoNestedArray() throws AxisFault {
//        url = "http://soapinterop.roguewave.com:8013/interop2testB/InteropRound2TestB";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupbEchoNestedArrayUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "RWGroupbNestedArrayRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }

    /**
     * Round2
     * Group C
     * operation echoString
     */
    //todo Group C tests failed, no headers returned
//    public void testR2GCEchoString() throws AxisFault {
//        url = "http://soapinterop.roguewave.com:8013/interop2testC/InteropRound2TestC";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupcEchoStringUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "RWGroupcEchoStringRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group C
//     * operation echoInterger
//     */
//    public void testR2GCEchoInterger() throws AxisFault {
//        url = "http://soapinterop.roguewave.com:8013/interop2testC/InteropRound2TestC";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupcIntergerUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "RWGroupcIntergerRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group C
//     * operation echoStringArray
//     */
//    public void testR2GCEchoStringArray() throws AxisFault {
//        url = "http://soapinterop.roguewave.com:8013/interop2testC/InteropRound2TestC";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupcStringArrayUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "RWGroupcStringArrayRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group C
//     * operation echoIntergerArray
//     */
//    public void testR2GCEchoIntergerArray() throws AxisFault {
//        url = "http://soapinterop.roguewave.com:8013/interop2testC/InteropRound2TestC";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupcIntegerArrayUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "RWGroupcIntegerArrayRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group C
//     * operation echoFloat
//     */
//    public void testR2GCEchoFloat() throws AxisFault {
//        url = "http://soapinterop.roguewave.com:8013/interop2testC/InteropRound2TestC";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupcFloatUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "RWGroupcFloatRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group C
//     * operation echoFloatArray
//     */
//    public void testR2GCEchoFloatArray() throws AxisFault {
//        url = "http://soapinterop.roguewave.com:8013/interop2testC/InteropRound2TestC";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupcFloatArrayUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "RWGroupcFloatArrayRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group C
//     * operation echoStruct
//     */
//    public void testR2GCEchoStruct() throws AxisFault {
//        url = "http://soapinterop.roguewave.com:8013/interop2testC/InteropRound2TestC";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupcStructUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "RWGroupcStructRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group C
//     * operation echoStructArray
//     */
//    public void testR2GCEchoStructArray() throws AxisFault {
//        url = "http://soapinterop.roguewave.com:8013/interop2testC/InteropRound2TestC";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupcStructArrayUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "RWGroupcStructArrayRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group C
//     * operation echoVoid
//     */
//    public void testR2GCEchoVoid() throws AxisFault {
//        url = "http://soapinterop.roguewave.com:8013/interop2testC/InteropRound2TestC";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupcVoidUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "RWGroupcVoidRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group C
//     * operation echoBase64
//     */
//    public void testR2GCEchoBase64() throws AxisFault {
//        url = "http://soapinterop.roguewave.com:8013/interop2testC/InteropRound2TestC";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupcBase64Util();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "RWGroupcBase64Res.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group C
//     * operation echoHexBinary
//     */
//    public void testR2GCEchoHexBinary() throws AxisFault {
//        url = "http://soapinterop.roguewave.com:8013/interop2testC/InteropRound2TestC";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupcHexBinaryUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "RWGroupcHexBinaryRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group C
//     * operation echoBoolean
//     */
//    public void testR2GCEchoBoolean() throws AxisFault {
//        url = "http://soapinterop.roguewave.com:8013/interop2testC/InteropRound2TestC";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupcBooleanUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "RWGroupcBooleanRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }

//    private static boolean compare(SOAPEnvelope retEnv, String filePath) throws AxisFault {
//
//        boolean ok = false;
//        try {
//            if (retEnv != null) {
//                SOAPBody body = retEnv.getBody();
//                if (!body.hasFault()) {
//                    //OMElement firstChild = (OMElement) body.getFirstElement();
//
//                    InputStream stream = RWRound2InteropTest.class.getClassLoader().getResourceAsStream(filePath);
//
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
//            throw new AxisFault(e);
//        }
//        return ok;
//    }
}

