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

package org.apache.axis2.interop.whitmesa.round2;

import org.apache.axis2.interop.whitemesa.WhiteMesaIneterop;
import org.apache.axis2.interop.whitemesa.round2.util.SunRound2ClientUtil;
import org.apache.axis2.soap.SOAPEnvelope;

import java.io.File;

/**
 * class
 * To test Interoperability Axis2 clients vs VW OpentalkSoap 1.0 Server, Round2
 * WSDLs:-
 * "base"     http://www.cincomsmalltalk.com:8080/CincomSmalltalkWiki/DOWNLOAD/WebServices/vwInteropSchema.wsdl
 * "Group B"  http://www.cincomsmalltalk.com:8080/CincomSmalltalkWiki/DOWNLOAD/WebServices/vwInteropR2GroupB.wsdl
 */
//todo All tests failed, Connection Failure

public class VWRound2InteropTest extends WhiteMesaIneterop {

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

    public void testChack(){
        //just addding a test case , since all the tase casea are fail
    }


    /**
     * Round2
     * Group Base
     * operation echoString
     */
//    public void testR2BaseEchoString() throws AxisFault {
//        url = "http://www.cincomsmalltalk.com/soap/interop";              //www.cincomsmalltalk.com
//        soapAction = "http://soapinterop.org/";
//
//        util = new Round2EchoStringclientUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "sunBaseStringRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group Base
//     * operation echoStringArray
//     */
//    public void testR2BaseEchoStringArray() throws AxisFault {
//        url = "http://www.cincomsmalltalk.com/soap/interop";
//        soapAction = "http://soapinterop.org/";
//
//        util = new Round2EchoStringArrayClientUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "SunBaseStringArrayRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group Base
//     * operation echoInteger
//     */
//    public void testR2BaseEchoInteger() throws AxisFault {
//        url = "http://www.cincomsmalltalk.com/soap/interop";
//        soapAction = "http://soapinterop.org/";
//
//        util = new Round2EchoIntegerClientUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "sunBaseIntegerRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group Base
//     * operation echoIntegerArray
//     */
//    public void testR2BaseEchoIntegerArray() throws AxisFault  {
//        url = "http://www.cincomsmalltalk.com/soap/interop";
//        soapAction = "http://soapinterop.org/";
//
//        util = new Round2EchoIntegerArrayclientUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "sunBaseIntegerArrayRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group Base
//     * operation echoFloat
//     */
//    public void testR2BaseEchoFloat()  throws AxisFault {
//        url = "http://www.cincomsmalltalk.com/soap/interop";
//        soapAction = "http://soapinterop.org/";
//
//        util = new Round2EchoFloatClientUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "sunBaseFloatRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group Base
//     * operation echoFloatArray
//     */
//    public void testR2BaseEchoFloatArray()  throws AxisFault {
//        url = "http://www.cincomsmalltalk.com/soap/interop";
//        soapAction = "http://soapinterop.org/";
//
//        util = new Round2EchoFloatArrayClientUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "sunBaseFloatArrayRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group Base
//     * operation echoStruct
//     */
//    public void testRBaseEchoStruct() throws AxisFault  {
//        url = "http://www.cincomsmalltalk.com/soap/interop";
//        soapAction = "";
//
//        util = new Round2EchoStructClientUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "sunBaseStructRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group Base
//     * operation echoStructArray
//     */
//    public void testR2BaseEchoStructArray() throws AxisFault  {
//        url = "http://www.cincomsmalltalk.com/soap/interop";
//        soapAction = "http://soapinterop.org/";
//
//        util = new Round2EchoStructArrayClientUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "sunBaseStructArrayRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group Base
//     * operation echoVoid
//     */
//    public void testR2BaseEchoVoid() throws AxisFault  {
//        url = "http://www.cincomsmalltalk.com/soap/interop";
//        soapAction = "http://soapinterop.org/";
//
//        util = new Round2EchoVoidClientUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "sunBaseVoidRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group Base
//     * operation echoBase64
//     */
//    public void testR2BaseEchoBase64() throws AxisFault  {
//        url = "http://www.cincomsmalltalk.com/soap/interop";
//        soapAction = "http://soapinterop.org/";
//
//        util = new Round2EchoBase64ClientUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "sunBaseBase64Res.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group Base
//     * operation echoBase64
//     */
//    public void testR2BaseEchoDate() throws AxisFault  {
//        url = "http://www.cincomsmalltalk.com/soap/interop";
//        soapAction = "http://soapinterop.org/";
//
//        util = new Round2EchoDateClientUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "sunBaseDateRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//
//    /**
//     * Round2
//     * Group Base
//     * operation echoHexBinary
//     */
//    public void testR2BaseEchoHexBinary() throws AxisFault  {
//        url = "http://www.cincomsmalltalk.com/soap/interop";
//        soapAction = "http://soapinterop.org/";
//
//        util = new Round2EchoHexBinaryClientUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "sunBaseHexBinaryRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group Base
//     * operation echoDecimal
//     */
//    public void testR2BaseEchoDecimal() throws AxisFault  {
//        url = "http://www.cincomsmalltalk.com/soap/interop";
//        soapAction = "http://soapinterop.org/";
//
//        util = new Round2EchoDecimalClientUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "sunBaseDecimalRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group Base
//     * operation echoBoolean
//     */
//    public void testR2BaseEchoBoolean() throws AxisFault  {
//        url = "http://www.cincomsmalltalk.com/soap/interop";
//        soapAction = "http://soapinterop.org/";
//
//        util = new Round2EchoBooleanClientUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "sunBaseBooleanRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group B
//     * operation echoStructAsSimpleTypes
//     */
//    public void testR2GBEchoStructAsSimpleTypes() throws AxisFault {
//        url = "http://www.cincomsmalltalk.com/r2groupb/interop";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupbEchoStructAsSimpleTypesUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "sunGroupbStructAsSimpleTypesRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group B
//     * operation echoSimpleTypesAsStruct
//     */
//    public void testR2GBEchoSimpleTypesAsStruct() throws AxisFault {
//        url = "http://www.cincomsmalltalk.com/r2groupb/interop";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupbEchoSimpleTypesAsStructUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "sunGroupbSimpletypesAsStructRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group B
//     * operation echo2DStringArray
//     */
//    public void testR2GBEcho2DStringArray() throws AxisFault {
//        url = "http://www.cincomsmalltalk.com/r2groupb/interop";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupbEcho2DStringArrayUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "sunGroupb2DStringArrayRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group B
//     * operation echoNestedStruct
//     */
//    public void testR2GBEchoNestedStruct() throws AxisFault {
//        url = "http://www.cincomsmalltalk.com/r2groupb/interop";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupbEchoNestedStructUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "sunGroupbNestedStructRes.xml";
//        results = compare(retEnv, tempPath);
//        assertTrue(results);
//    }
//
//    /**
//     * Round2
//     * Group B
//     * operation echoNestedArray
//     */
//    public void testR2GBEchoNestedArray() throws AxisFault {
//        url = "http://www.cincomsmalltalk.com/r2groupb/interop";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupbEchoNestedArrayUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "sunGroupbNestedArrayRes.xml";
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
//                    InputStream stream = VWRound2InteropTest.class.getClassLoader().getResourceAsStream(filePath);
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
//            throw new AxisFault(e); //To change body of catch statement use File | Settings | File Templates.
//        }
//        return ok;
//    }
}

