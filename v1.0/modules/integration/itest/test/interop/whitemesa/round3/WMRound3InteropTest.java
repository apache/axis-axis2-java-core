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

/**
 * class MsAsmxRound3InteropTest
 * To test interoperability in Axis2 Clients Vs White Mesa Server, Round 3 *
 */
public class WMRound3InteropTest extends WhiteMesaIneterop {

    SunClient client = new SunClient();
    SOAPEnvelope retEnv = null;
    String url = "";
    String soapAction = "";
    String resFilePath = "interop/whitemesa/round3/";
    String tempPath = "";
    SunClientUtil util = null;

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
        compareXML(retEnv, tempPath);
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
        compareXML(retEnv, tempPath);
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
        compareXML(retEnv, tempPath);
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
        compareXML(retEnv, tempPath);
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
        compareXML(retEnv, tempPath);
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
        compareXML(retEnv, tempPath);
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
        compareXML(retEnv, tempPath);
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
        compareXML(retEnv, tempPath);
    }
}
