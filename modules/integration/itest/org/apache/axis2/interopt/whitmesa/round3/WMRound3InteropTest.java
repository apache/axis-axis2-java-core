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

package org.apache.axis2.interopt.whitmesa.round3;

import org.apache.axis2.AxisFault;
import org.apache.axis2.interopt.whitemesa.WhiteMesaIneterop;
import org.apache.axis2.interopt.whitemesa.round3.SunRound3Client;
import org.apache.axis2.interopt.whitemesa.round3.util.GDImport1EchoStringUtil;
import org.apache.axis2.interopt.whitemesa.round3.util.GDImport2EchoStructUtil;
import org.apache.axis2.interopt.whitemesa.round3.util.GDImport3StructArrayUtil;
import org.apache.axis2.interopt.whitemesa.round3.util.GDRpcStringArrayUtil;
import org.apache.axis2.interopt.whitemesa.round3.util.GDRpcStringUtil;
import org.apache.axis2.interopt.whitemesa.round3.util.GDRpcStructUtil;
import org.apache.axis2.interopt.whitemesa.round3.util.GDRpcVoidUtil;
import org.apache.axis2.interopt.whitemesa.round3.util.SunRound3ClientUtil;
import org.apache.axis2.soap.SOAPEnvelope;

/**
 * class MsAsmxRound3InteropTest
 * To test interoperability in Axis2 Clients Vs White Mesa Server, Round 3 *
 */
public class
        WMRound3InteropTest extends WhiteMesaIneterop {

    SunRound3Client client = null;
    SOAPEnvelope retEnv = null;
    boolean success = false;
    String url = "";
    String soapAction = "";
    String resFilePath = "";
    String tempPath = "";
    SunRound3ClientUtil util = null;
    private boolean result = false;

    public void setUp() {
        client = new SunRound3Client();
        resFilePath = "interopt/whitemesa/round3/";
    }

    /**
     * Round 3
     * Group EmptySA
     * operation EchoString
     * This test Not Available!!!
     */
//    public void testR3EsaEchoString() throws AxisFault {
//
//        url = "http://www.whitemesa.net/interop/r3/emptySA-12";
//        soapAction = "";
//
//        util = new Round3EmptySAEchoStringUtil();
//        retEnv = client.sendMsg(util, url, soapAction);
//        tempPath = resFilePath + "WMR3EsaRes.xml";
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
        url = "http://www.whitemesa.net/interop/r3/import1";
        soapAction = "http://soapinterop.org/";

        util = new GDImport1EchoStringUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMGDImport1StringRes.xml";
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
        url = "http://www.whitemesa.net/interop/r3/import2";
        soapAction = "http://soapinterop.org/";

        util = new GDImport2EchoStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMGDImport2StructRes.xml";
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
        url = "http://www.whitemesa.net/interop/r3/import3";
        soapAction = "http://soapinterop.org/";

        util = new GDImport2EchoStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMGDImport3StructRes.xml";
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
        url = "http://www.whitemesa.net/interop/r3/import3";
        soapAction = "http://soapinterop.org/";

        util = new GDImport3StructArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMGDImport3StructArrayRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }


     /**
     * Round 3
     * Group D
     * Service rpcencoded
     * operation EchoString
     */
    public void testR3GDRpcEchoString() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r3/rpcEnc";
        soapAction = "";

        util = new GDRpcStringUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMGDRpcStringRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    /**
     * Round 3
     * Group D
     * Service rpcencoded
     * operation EchoStringArray
     */
    public void testR3GDRpcEchoStringArray() throws AxisFault {
        url = "http://www.whitemesa.net/interop/r3/rpcEnc";
        soapAction = "";

        util = new GDRpcStringArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMGDRpcStringArrayRes.xml";
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
        url = "http://www.whitemesa.net/interop/r3/rpcEnc";
        soapAction = "";

        util = new GDRpcStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMGDRpcStructRes.xml";
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
        url = "http://www.whitemesa.net/interop/r3/rpcEnc";
        soapAction = "";

        util = new GDRpcVoidUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "WMGDRpcVoidRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }




//    private static boolean compare(SOAPEnvelope retEnv, String filePath) throws AxisFault {
//        boolean ok = false;
//        try {
//            if (retEnv != null) {
//                SOAPBody body = retEnv.getBody();
//                if (!body.hasFault()) {
//                    InputStream stream = WMRound3InteropTest.class.getClassLoader().getResourceAsStream( filePath);
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
