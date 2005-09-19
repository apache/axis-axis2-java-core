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

package org.apache.axis2.interop.whitmesa.round3;

import org.apache.axis2.AxisFault;
import org.apache.axis2.interop.whitemesa.WhiteMesaIneterop;
import org.apache.axis2.interop.whitemesa.round3.SunRound3Client;
import org.apache.axis2.interop.whitemesa.round3.util.GDImport1EchoStringUtil;
import org.apache.axis2.interop.whitemesa.round3.util.GDImport2EchoStructUtil;
import org.apache.axis2.interop.whitemesa.round3.util.GDImport3StructArrayUtil;
import org.apache.axis2.interop.whitemesa.round3.util.GDRpcStringArrayUtil;
import org.apache.axis2.interop.whitemesa.round3.util.GDRpcStringUtil;
import org.apache.axis2.interop.whitemesa.round3.util.GDRpcStructUtil;
import org.apache.axis2.interop.whitemesa.round3.util.GDRpcVoidUtil;
import org.apache.axis2.interop.whitemesa.round3.util.GEListUtil;
import org.apache.axis2.interop.whitemesa.round3.util.GFHeaderTestUtil;
import org.apache.axis2.interop.whitemesa.round3.util.SunRound3ClientUtil;
import org.apache.axis2.soap.SOAPEnvelope;

/**
 * /**
 * class SunRound3InteropTest
 * To test interoperability in Axis2 Clients Vs ASP NET Server, Round 3
 *
 */

public class
        SunRound3InteropTest extends WhiteMesaIneterop {

    SunRound3Client client = null;
    SOAPEnvelope retEnv = null;
    boolean success = false;
    String url = "";
    String soapAction = "";
    String resFilePath = "interopt/whitemesa/round3/";
    String tempPath = "";
    SunRound3ClientUtil util = null;
    private boolean result = false;

    public void setUp() {
        client = new SunRound3Client();
    }

    /**
     * Round 3
     * Group EmptySA
     * operation EchoString
     * todo This test fails!!!
     */
//    public void testR3EsaEchoString() throws AxisFault {
//
//        url = "http://www.whitemesa.net:80/interop/r3/emptySA";
//        soapAction = "";
//
//        util = new Round3EmptySAEchoStringUtil();
//        retEnv = client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "Round3EmptySAEchoStringRes.xml";
//        result = compare(retEnv, tempPath);
//        assertTrue(result);
//
//    }

    /**
     * Round 3
     * Group D
     * Service import1
     * operation EchoString
     */
    public void testR3GDEchoString() throws AxisFault {
        url = "http://soapinterop.java.sun.com:80/round3/groupd/import1";
        soapAction = "http://soapinterop.org/";

        util = new GDImport1EchoStringUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "sunGDImport1StringRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    /**
     * Round 3
     * Group D
     * Service import2
     * operation EchoStruct
     */
    public void testR3GDEchoStruct() throws AxisFault {
        url = "http://soapinterop.java.sun.com:80/round3/groupd/import2";
        soapAction = "http://soapinterop.org/";

        util = new GDImport2EchoStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "sunGDImport2StructRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    /**
     * Round 3
     * Group D
     * Service import3
     * operation EchoStruct
     */
    public void testR3GDI3EchoStruct() throws AxisFault {
        url = "http://soapinterop.java.sun.com:80/round3/groupd/import3";
        soapAction = "http://soapinterop.org/";

        util = new GDImport2EchoStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "sunGDImport3StructRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    /**
     * Round 3
     * Group D
     * Service import3
     * operation EchoStructArray
     */
    public void testR3GDI3EchoStructArray() throws AxisFault {
        url = "http://soapinterop.java.sun.com:80/round3/groupd/import3";
        soapAction = "http://soapinterop.org/";

        util = new GDImport3StructArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "sunGDImport3StructArrayRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    /**
     * Round 3
     * Group D
     * Service rpcencoded
     * operation EchoString
     */
    public void testR3GDI3EchoString() throws AxisFault {
        url = "http://soapinterop.java.sun.com:80/round3/groupd/rpcencoded";
        soapAction = "http://soapinterop.org/";

        util = new GDRpcStringUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "sunGDRpcStringRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    /**
     * Round 3
     * Group D
     * Service rpcencoded
     * operation EchoStringArray
     */
    public void testR3GDI3EchoStringArray() throws AxisFault {
        url = "http://soapinterop.java.sun.com:80/round3/groupd/rpcencoded";
        soapAction = "http://soapinterop.org/";

        util = new GDRpcStringArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "sunGDRpcStringArrayRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    /**
     * Round 3
     * Group D
     * Service Rpcencoded
     * operation EchoStruct
     */
    public void testR3GDRpcEchoStruct() throws AxisFault {
        url = "http://soapinterop.java.sun.com:80/round3/groupd/rpcencoded";
        soapAction = "http://soapinterop.org/";

        util = new GDRpcStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "sunGDRpcStructRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    /**
     * Round 3
     * Group D
     * Service Rpcencoded
     * operation EchoVoid
     */
    public void testR3GDRpcEchoVoid() throws AxisFault {
        url = "http://soapinterop.java.sun.com:80/round3/groupd/rpcencoded";
        soapAction = "http://soapinterop.org/";

        util = new GDRpcVoidUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "sunGDRpcVoidRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    /**
     * Round 3
     * Group E
     * Service Rpcencoded
     * operation EchoString
     */
    public void testR3GERpcEchoString() throws AxisFault {
        url = "http://soapinterop.java.sun.com:80/round3/groupe/rpcencoded";
        soapAction = "http://soapinterop.org/";

        util = new GDRpcStringUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "SunGERpcStringRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    /**
     * Round 3
     * Group E
     * Service Rpcencoded
     * operation EchoStringArray
     */
    public void testR3GERpcEchoStringArray() throws AxisFault {
        url = "http://soapinterop.java.sun.com:80/round3/groupe/rpcencoded";
        soapAction = "http://soapinterop.org/";

        util = new GDRpcStringArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "SunGERpcStringArrayRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    /**
     * Round 3
     * Group E
     * Service Rpcencoded
     * operation EchoStruct
     */
    public void testR3GERpcEchoStruct() throws AxisFault {
        url = "http://soapinterop.java.sun.com:80/round3/groupe/rpcencoded";
        soapAction = "http://soapinterop.org/";

        util = new GDRpcStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "SunGERpcStructRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }


    /**
     * Round 3
     * Group E
     * Service Rpcencoded
     * operation EchoVoid
     */
    public void testR3GERpcEchoVoid() throws AxisFault {
        url = "http://soapinterop.java.sun.com:80/round3/groupe/rpcencoded";
                soapAction = "http://soapinterop.org/";

        util = new GDRpcVoidUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "SunGERpcVoidRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    /**
     * Round 3
     * Group E
     * Service List
     * operation echoLinkedList
     */
    public void testR3GEEchoList() throws AxisFault {
        url = "http://soapinterop.java.sun.com:80/round3/groupe/list";
//        url = "http://localhost:8000/round3/groupe/list";
        soapAction = "http://soapinterop.org/";

        util = new GEListUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "SunGEListRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    /**
     * Round 3
     * Group F
     * Service Headers
     * operation echoString
     */
    public void testR3GFEchoString() throws AxisFault {
        url = "http://soapinterop.java.sun.com:80/round3/groupf/headers";
        soapAction = "http://soapinterop.org/";

        util = new GFHeaderTestUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GFHeaderTestRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }


//    private static boolean compare(SOAPEnvelope retEnv, String filePath) throws AxisFault {
//        boolean ok = false;
//        try {
//            if (retEnv != null) {
//                SOAPBody body = retEnv.getBody();
//                if (!body.hasFault()) {
//                    InputStream stream = SunRound3InteropTest.class.getClassLoader().getResourceAsStream( filePath);
//                    OMElement firstChild = (OMElement) body.getFirstElement();
//                    XMLStreamReader parser = null;
//                    parser = XMLInputFactory.newInstance().createXMLStreamReader(stream);
//                    OMXMLParserWrapper builder = new StAXSOAPModelBuilder(parser, null);
//                    SOAPEnvelope resEnv = (SOAPEnvelope) builder.getDocumentElement();
//                    OMElement refNode = (OMElement) resEnv.getBody().getFirstElement();
//                    XMLComparator comparator = new XMLComparator();
//                    ok = comparator.compare(firstChild, refNode);
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
