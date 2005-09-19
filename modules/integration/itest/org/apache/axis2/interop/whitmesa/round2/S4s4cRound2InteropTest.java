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

import org.apache.axis2.AxisFault;
import org.apache.axis2.interop.whitemesa.WhiteMesaIneterop;
import org.apache.axis2.interop.whitemesa.round2.SunRound2Client;
import org.apache.axis2.interop.whitemesa.round2.util.*;
import org.apache.axis2.soap.SOAPEnvelope;

import java.io.File;

/**
 * class
 * To test interoperability Axis2 clients vs 4s4c server
 * WSDLs:-
 * "base"     http://www.pocketsoap.com/services/ilab.wsdl
 * "Group B"  http://www.pocketsoap.com/services/ilab_b.wsdl
 */

public class S4s4cRound2InteropTest extends WhiteMesaIneterop {

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
        url = "http://soap.4s4c.com/ilab/soap.asp";    //soap.4s4c.com/
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStringclientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "S4s4cBaseStringRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoStringArray
     */
    public void testR2BaseEchoStringArray() throws AxisFault {
        url = "http://soap.4s4c.com/ilab/soap.asp";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStringArrayClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "S4s4cBaseStringArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoInteger
     */
    public void testR2BaseEchoInteger() throws AxisFault {
        url = "http://soap.4s4c.com/ilab/soap.asp";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoIntegerClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "S4s4cBaseIntegerRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoIntegerArray
     */
    public void testR2BaseEchoIntegerArray() throws AxisFault  {
        url = "http://soap.4s4c.com/ilab/soap.asp";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoIntegerArrayclientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "S4s4cBaseIntegerArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoFloat
     */
    public void testR2BaseEchoFloat()  throws AxisFault {
        url = "http://soap.4s4c.com/ilab/soap.asp";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoFloatClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "S4s4cBaseFloatRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoFloatArray
     */
    public void testR2BaseEchoFloatArray()  throws AxisFault {
        url = "http://soap.4s4c.com/ilab/soap.asp";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoFloatArrayClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "S4s4cBaseFloatArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoStruct
     */
    public void testRBaseEchoStruct() throws AxisFault  {
        url = "http://soap.4s4c.com/ilab/soap.asp";
        soapAction = "";

        util = new Round2EchoStructClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "S4s4cBaseStructRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoStructArray
     */
    public void testR2BaseEchoStructArray() throws AxisFault  {
        url = "http://soap.4s4c.com/ilab/soap.asp";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoStructArrayClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "S4s4cBaseStructArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoVoid
     */
    public void testR2BaseEchoVoid() throws AxisFault  {
        url = "http://soap.4s4c.com/ilab/soap.asp";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoVoidClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "S4s4cBaseVoidRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoBase64
     */
    public void testR2BaseEchoBase64() throws AxisFault  {
        url = "http://soap.4s4c.com/ilab/soap.asp";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoBase64ClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "S4s4cBaseBase64Res.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoBase64
     */
    public void testR2BaseEchoDate() throws AxisFault  {
        url = "http://soap.4s4c.com/ilab/soap.asp";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoDateClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "S4s4cBaseDateRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }


    /**
     * Round2
     * Group Base
     * operation echoHexBinary
     */
    public void testR2BaseEchoHexBinary() throws AxisFault  {
        url = "http://soap.4s4c.com/ilab/soap.asp";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoHexBinaryClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "S4s4cBaseHexBinaryRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoDecimal
     */
    public void testR2BaseEchoDecimal() throws AxisFault  {
        url = "http://soap.4s4c.com/ilab/soap.asp";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoDecimalClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "S4s4cBaseDecimalRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group Base
     * operation echoBoolean
     */
    public void testR2BaseEchoBoolean() throws AxisFault  {
        url = "http://soap.4s4c.com/ilab/soap.asp";
        soapAction = "http://soapinterop.org/";

        util = new Round2EchoBooleanClientUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "S4s4cBaseBooleanRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group B
     * operation echoStructAsSimpleTypes
     */
    public void testR2GBEchoStructAsSimpleTypes() throws AxisFault {
        url = "http://soap.4s4c.com/ilab/soap.asp";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoStructAsSimpleTypesUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "S4s4cGroupbStructAsSimpleTypesRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group B
     * operation echoSimpleTypesAsStruct
     */
    public void testR2GBEchoSimpleTypesAsStruct() throws AxisFault {
        url = "http://soap.4s4c.com/ilab/soap.asp";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoSimpleTypesAsStructUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "S4s4cGroupbSimpletypesAsStructRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group B
     * operation echo2DStringArray
     */
    public void testR2GBEcho2DStringArray() throws AxisFault {
        url = "http://soap.4s4c.com/ilab/soap.asp";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEcho2DStringArrayUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "S4s4cGroupb2DStringArrayRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group B
     * operation echoNestedStruct
     */
    public void testR2GBEchoNestedStruct() throws AxisFault {
        url = "http://soap.4s4c.com/ilab/soap.asp";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoNestedStructUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "S4s4cGroupbNestedStructRes.xml";
        results = compare(retEnv, tempPath);
        assertTrue(results);
    }

    /**
     * Round2
     * Group B
     * operation echoNestedArray
     */
    public void testR2GBEchoNestedArray() throws AxisFault {
        url = "http://soap.4s4c.com/ilab/soap.asp";
        soapAction = "http://soapinterop.org/";

        util = new GroupbEchoNestedArrayUtil();
        retEnv = SunRound2Client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "S4s4cGroupbNestedArrayRes.xml";
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
//
//                    InputStream stream = S4s4cRound2InteropTest.class.getClassLoader().getResourceAsStream(filePath);
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

