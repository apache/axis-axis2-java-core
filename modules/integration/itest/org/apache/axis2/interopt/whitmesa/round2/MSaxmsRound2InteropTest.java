package org.apache.axis2.interopt.whitmesa.round2;

import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.interopt.whitemesa.round2.util.*;
import org.apache.axis2.interopt.whitemesa.round2.SunRound2Client;
import org.apache.axis2.interopt.whitemesa.WhiteMesaIneterop;
import org.apache.axis2.AxisFault;
import java.io.*;

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
*
*
*/

/**
 * Author: Gayan Asanka
 * Date: Aug 23, 2005
 * Time: 4:27:20 PM
 */

/**
 * class MSaxmsRound2InteropTest
 * To test interoperability Axis2 clients vs ASP.NET Web Services
 * WSDLs:-
 * "base"    http://www.mssoapinterop.org/asmx/simple.asmx?wsdl
 * "Group B" http://www.mssoapinterop.org/asmx/simpleb.asmx?wsdl
 * "Group C" http://mssoapinterop.org/asmx/header.asmx?wsdl
 */
public class MSaxmsRound2InteropTest extends WhiteMesaIneterop {

    SOAPEnvelope retEnv = null;
    boolean success = false;
    File file = null;
    String url = "";
    String soapAction = "";
    String FS = System.getProperty("file.separator");
    String resFilePath = "interopt/whitemesa/round2/";
    String tempPath = "";
    SunRound2ClientUtil util;
    private boolean results = false;

    /**
     * Round2
     * Group Base
     * operation echoString
     */
    public void testR2BaseEchoString() throws AxisFault {
        url = "http://www.mssoapinterop.org/asmx/simple.asmx";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStringclientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSaxmsBaseStringRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoStringArray
     */
    public void testR2BaseEchoStringArray() throws AxisFault {
        url = "http://www.mssoapinterop.org/asmx/simple.asmx";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStringArrayClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSaxmsBaseStringArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoInteger
     */
    public void testR2BaseEchoInteger() throws AxisFault {
        url = "http://www.mssoapinterop.org/asmx/simple.asmx";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoIntegerClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSaxmsBaseIntegerRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoIntegerArray
     */
    public void testR2BaseEchoIntegerArray() throws AxisFault  {
        url = "http://www.mssoapinterop.org/asmx/simple.asmx";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoIntegerArrayclientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSaxmsBaseIntegerArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoFloat
     */
    public void testR2BaseEchoFloat()  throws AxisFault {
        url = "http://www.mssoapinterop.org/asmx/simple.asmx";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoFloatClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSaxmsBaseFloatRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoFloatArray
     */
    public void testR2BaseEchoFloatArray()  throws AxisFault {
        url = "http://www.mssoapinterop.org/asmx/simple.asmx";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoFloatArrayClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSaxmsBaseFloatArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoStruct
     */
    public void testRBaseEchoStruct() throws AxisFault  {
        url = "http://www.mssoapinterop.org/asmx/simple.asmx";
        soapAction = "";

        util = new Round2EchoStructClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSaxmsBaseStructRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoStructArray
     */
    public void testR2BaseEchoStructArray() throws AxisFault  {
        url = "http://www.mssoapinterop.org/asmx/simple.asmx";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStructArrayClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSaxmsBaseStructArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoVoid
     */
    public void testR2BaseEchoVoid() throws AxisFault  {
        url = "http://www.mssoapinterop.org/asmx/simple.asmx";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoVoidClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSaxmsBaseVoidRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoBase64
     */
    public void testR2BaseEchoBase64() throws AxisFault  {
        url = "http://www.mssoapinterop.org/asmx/simple.asmx";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoBase64ClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSaxmsBaseBase64Res.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoBase64
     */
    public void testR2BaseEchoDate() throws AxisFault  {
        url = "http://www.mssoapinterop.org/asmx/simple.asmx";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoDateClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSaxmsBaseDateRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }


    /**
     * Round2
     * Group Base
     * operation echoHexBinary
     */
    public void testR2BaseEchoHexBinary() throws AxisFault  {
        url = "http://www.mssoapinterop.org/asmx/simple.asmx";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoHexBinaryClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSaxmsBaseHexBinaryRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoDecimal
     */
    public void testR2BaseEchoDecimal() throws AxisFault  {
        url = "http://www.mssoapinterop.org/asmx/simple.asmx";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoDecimalClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSaxmsBaseDecimalRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoBoolean
     */
    public void testR2BaseEchoBoolean() throws AxisFault  {
        url = "http://www.mssoapinterop.org/asmx/simple.asmx";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoBooleanClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSaxmsBaseBooleanRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group B
     * operation echoStructAsSimpleTypes
     */
    public void testR2GBEchoStructAsSimpleTypes() throws AxisFault {
        url = "http://www.mssoapinterop.org/asmx/simpleb.asmx";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoStructAsSimpleTypesUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSaxmsGroupbStructAsSimpleTypesRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group B
     * operation echoSimpleTypesAsStruct
     */
    public void testR2GBEchoSimpleTypesAsStruct() throws AxisFault {
        url = "http://www.mssoapinterop.org/asmx/simpleb.asmx";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoSimpleTypesAsStructUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSaxmsGroupbSimpletypesAsStructRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group B
     * operation echo2DStringArray
     */
    public void testR2GBEcho2DStringArray() throws AxisFault {
        url = "http://www.mssoapinterop.org/asmx/simpleb.asmx";
        soapAction = "http://soapinterop.org/";

        util = new R2MSaxms2DStringArrayUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSaxmsGroupb2DStringArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group B
     * operation echoNestedStruct
     */
    public void testR2GBEchoNestedStruct() throws AxisFault {
        url = "http://www.mssoapinterop.org/asmx/simpleb.asmx";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoNestedStructUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSaxmsGroupbNestedStructRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group B
     * operation echoNestedArray
     */
    public void testR2GBEchoNestedArray() throws AxisFault {
        url = "http://www.mssoapinterop.org/asmx/simpleb.asmx";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoNestedArrayUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MSaxmsGroupbNestedArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

     /**
     * Round2
     * Group C
     * operation echoVoid
     */
     //todo This test failed, doesn't echo header
//    public void testR2GCEchoVoid() throws AxisFault {
//        url = "http://www.mssoapinterop.org/asmx/header.asmx";
//        soapAction = "http://soapinterop.org/";
//
//        util = new GroupcVoidUtil();
//        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "MSaxmsGroupcVoidRes.xml";
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
//                    InputStream stream = MSaxmsRound2InteropTest.class.getClassLoader().getResourceAsStream(filePath);
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

