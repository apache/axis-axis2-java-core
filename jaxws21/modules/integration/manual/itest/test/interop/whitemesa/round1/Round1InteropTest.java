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

package test.interop.whitemesa.round1;

import org.apache.axis2.AxisFault;
import org.apache.axiom.soap.SOAPEnvelope;
import test.interop.whitemesa.SunClient;
import test.interop.whitemesa.SunClientUtil;
import test.interop.whitemesa.WhiteMesaIneterop;
import test.interop.whitemesa.round1.util.Round1FloatArrayUtil;
import test.interop.whitemesa.round1.util.Round1FloatUtil;
import test.interop.whitemesa.round1.util.Round1IntArrayUtil;
import test.interop.whitemesa.round1.util.Round1IntegerUtil;
import test.interop.whitemesa.round1.util.Round1StringArrayUtil;
import test.interop.whitemesa.round1.util.Round1StringUtil;
import test.interop.whitemesa.round1.util.Round1StructArrayUtil;
import test.interop.whitemesa.round1.util.Round1StructUtil;
import test.interop.whitemesa.round1.util.Round1VoidUtil;

public class Round1InteropTest extends WhiteMesaIneterop {

    SOAPEnvelope retEnv = null;
    String url = "http://easysoap.sourceforge.net/cgi-bin/interopserver";
    String soapAction = "urn:soapinterop";
    String resFilePath = "interop/whitemesa/round1/";
    String tempPath = "";
    SunClientUtil util;
    SunClient client = new SunClient();

    public void testEchoString() throws AxisFault {
        util = new Round1StringUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "Round1StringUtilRes.xml";
        compareXML(retEnv, tempPath);
    }

    public void testEchoVoid() throws AxisFault {
        util = new Round1VoidUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "Round1VoidUtilRes.xml";
        compareXML(retEnv, tempPath);
    }

    public void testEchoStringArray() throws AxisFault {
        util = new Round1StringArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "Round1StringArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    public void testInteger() throws AxisFault {
        util = new Round1IntegerUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "Round1IntegerRes.xml";
        compareXML(retEnv, tempPath);
    }

    public void testIntegerArray() throws AxisFault {
        util = new Round1IntArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "Round1IntArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    public void testEchoFloat() throws AxisFault {
        util = new Round1FloatUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "Round1FloatRes.xml";
        compareXML(retEnv, tempPath);
    }


    public void testEchoFloatArray() throws AxisFault {
        util = new Round1FloatArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "Round1FloatArrayRes.xml";
        compareXML(retEnv, tempPath);
    }

    public void testEchoStruct() throws AxisFault {
        util = new Round1StructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "Round1StructRes.xml";
        compareXML(retEnv, tempPath);
    }

    public void testEchoStructArray() throws AxisFault {
        util = new Round1StructArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "Round1StructArrayRes.xml";
        compareXML(retEnv, tempPath);
    }
}
