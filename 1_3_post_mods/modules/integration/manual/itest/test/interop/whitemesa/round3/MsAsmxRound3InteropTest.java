/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package test.interop.whitemesa.round3;

import org.apache.axis2.AxisFault;
import org.apache.axiom.soap.SOAPEnvelope;
import test.interop.whitemesa.SunClient;
import test.interop.whitemesa.SunClientUtil;
import test.interop.whitemesa.WhiteMesaIneterop;
import test.interop.whitemesa.round3.util.GDImport1EchoStringUtil;
import test.interop.whitemesa.round3.util.GDImport2EchoStructUtil;
import test.interop.whitemesa.round3.util.GDImport3StructArrayUtil;
import test.interop.whitemesa.round3.util.GDRpcStringArrayUtil;
import test.interop.whitemesa.round3.util.GDRpcStringUtil;
import test.interop.whitemesa.round3.util.GDRpcStructUtil;
import test.interop.whitemesa.round3.util.GDRpcVoidUtil;
import test.interop.whitemesa.round3.util.GEListUtil;
import test.interop.whitemesa.round3.util.GFHeaderTestUtil;
import test.interop.whitemesa.round3.util.Round3EmptySAEchoStringUtil;

/**
 * class MsAsmxRound3InteropTest
 * To test interoperability in Axis2 Clients Vs ASP NET Server, Round 3
 */

public class MsAsmxRound3InteropTest extends WhiteMesaIneterop {

    SOAPEnvelope retEnv = null;
    String url = "";
    String soapAction = "";
    String resFilePath = "interop/whitemesa/round3/";
    String tempPath = "";
    SunClientUtil util = null;
    SunClient client = new SunClient();

    /**
     * Round 3
     * Group EmptySA
     * operation EchoString
     */
    public void testR3EsaEchoString() throws AxisFault {

        url = "http://mssoapinterop.org/asmx/wsdl/emptysa.asmx";
        soapAction = "";

        util = new Round3EmptySAEchoStringUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MsAsmxR3EmptySAEchoStringRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round 3
     * Group D
     * Service import1
     * operation EchoString
     */
    public void testR3GDEchoString() throws AxisFault {
        url = "http://mssoapinterop.org/asmx/wsdl/import1.asmx";
        soapAction = "http://soapinterop.org/";

        util = new GDImport1EchoStringUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MsAsmxGDImport1StringRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round 3
     * Group D
     * Service import2
     * operation EchoStruct
     */
    public void testR3GDEchoStruct() throws AxisFault {
        url = "http://mssoapinterop.org/asmx/wsdl/import2.asmx";
        soapAction = "http://soapinterop.org/";

        util = new GDImport2EchoStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MsAsmxGDImport2StructRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round 3
     * Group D
     * Service import3
     * operation EchoStruct
     */
    public void testR3GDI3EchoStruct() throws AxisFault {
        url = "http://mssoapinterop.org/asmx/wsdl/import3.asmx";
        soapAction = "http://soapinterop.org/";

        util = new GDImport2EchoStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MsAsmxGDImport3StructRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round 3
     * Group D
     * Service import3
     * operation EchoStructArray
     */
    public void testR3GDI3EchoStructArray() throws AxisFault {
        url = "http://mssoapinterop.org/asmx/wsdl/import3.asmx";
        soapAction = "http://soapinterop.org/";

        util = new GDImport3StructArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MsAsmxGDImport3StructArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round 3
     * Group D
     * Service rpcencoded
     * operation EchoString
     */
    public void testR3GDI3EchoString() throws AxisFault {
        url = "http://mssoapinterop.org/asmx/wsdl/WSDLInteropTestRpcEncService.asmx";
        soapAction = "http://soapinterop.org/";

        util = new GDRpcStringUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MsAsmxGDRpcStringRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round 3
     * Group D
     * Service rpcencoded
     * operation EchoStringArray
     */
    public void testR3GDI3EchoStringArray() throws AxisFault {
        url = "http://mssoapinterop.org/asmx/wsdl/WSDLInteropTestRpcEncService.asmx";
        soapAction = "http://soapinterop.org/";

        util = new GDRpcStringArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MsAsmxGDRpcStringArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round 3
     * Group D
     * Service Rpcencoded
     * operation EchoStruct
     */
    public void testR3GDRpcEchoStruct() throws AxisFault {
        url = "http://mssoapinterop.org/asmx/wsdl/WSDLInteropTestRpcEncService.asmx";
        soapAction = "http://soapinterop.org/";

        util = new GDRpcStructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MsAsmxGDRpcStructRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round 3
     * Group D
     * Service Rpcencoded
     * operation EchoVoid
     */
    public void testR3GDRpcEchoVoid() throws AxisFault {
        url = "http://mssoapinterop.org/asmx/wsdl/WSDLInteropTestRpcEncService.asmx";
        soapAction = "http://soapinterop.org/";

        util = new GDRpcVoidUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MsAsmxGDRpcVoidRes.xml";
        compareXML(retEnv, tempPath);
    }


    /**
     * Round 3
     * Group E
     * Service List
     * operation echoLinkedList
     */
    public void testR3GEEchoList() throws AxisFault {
        url = "http://mssoapinterop.org/asmx/wsdl/WSDLInteropTestListService.asmx";
        soapAction = "";

        util = new GEListUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "MsAsmxGEListRes.xml";
        compareXML(retEnv, tempPath);
    }

    /**
     * Round 3
     * Group F
     * Service Headers
     * operation echoString
     */
    public void testR3GFEchoString() throws AxisFault {
        url = "http://mssoapinterop.org/asmx/wsdl/InteropTestHeaders.asmx";
        soapAction = "http://soapinterop.org/";

        util = new GFHeaderTestUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "GFHeaderTestRes.xml";
        compareXML(retEnv, tempPath);
    }
}
