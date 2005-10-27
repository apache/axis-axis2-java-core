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

package test.interop.whitemesa.round1;

import org.apache.axis2.AxisFault;
import test.interop.whitemesa.WhiteMesaIneterop;
import test.interop.whitemesa.round1.util.*;
import org.apache.axis2.soap.SOAPEnvelope;

public class Round1InteropTest extends WhiteMesaIneterop {

    SOAPEnvelope retEnv = null;
    boolean success = false;
    String url = "http://soapinterop.java.sun.com:80/round2/base";
    String soapAction = "http://soapinterop.org/";
    String resFilePath = "interop/whitemesa/round1/";
    String tempPath = "";
    Round1ClientUtil util;
    Round1Client client = null;
    boolean result = false;

    public void setUp(){
        client = new Round1Client();
    }

    public void testEchoString() throws AxisFault {

        url = "http://easysoap.sourceforge.net/cgi-bin/interopserver";
        soapAction = "urn:soapinterop";
        util = new Round1StringUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "Round1StringUtilRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    public void testEchoVoid() throws AxisFault {
        url = "http://easysoap.sourceforge.net/cgi-bin/interopserver";
        soapAction = "urn:soapinterop";
        util = new Round1VoidUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "Round1VoidUtilRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    public void testEchoStringArray() throws AxisFault {
        url = "http://easysoap.sourceforge.net/cgi-bin/interopserver";
        soapAction = "urn:soapinterop";
        util = new Round1StringArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "Round1StringArrayRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    public void testInteger() throws AxisFault {
        url = "http://easysoap.sourceforge.net/cgi-bin/interopserver";
        soapAction = "urn:soapinterop";
        util = new Round1IntegerUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "Round1IntegerRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    public void testIntegerArray() throws AxisFault {
        url = "http://easysoap.sourceforge.net/cgi-bin/interopserver";
        soapAction = "urn:soapinterop";
        util = new Round1IntArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "Round1IntArrayRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    public void testEchoFloat() throws AxisFault {
        url = "http://easysoap.sourceforge.net/cgi-bin/interopserver";
        soapAction = "urn:soapinterop";
        util = new Round1FloatUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "Round1FloatRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }


    public void testEchoFloatArray() throws AxisFault {
        url = "http://easysoap.sourceforge.net/cgi-bin/interopserver";
        soapAction = "urn:soapinterop";
        util = new Round1FloatArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "Round1FloatArrayRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    public void testEchoStruct() throws AxisFault {
        url = "http://easysoap.sourceforge.net/cgi-bin/interopserver";
        soapAction = "urn:soapinterop";
        util = new Round1StructUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "Round1StructRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }

    public void testEchoStructArray() throws AxisFault {
        url = "http://easysoap.sourceforge.net/cgi-bin/interopserver";
        soapAction = "urn:soapinterop";
        util = new Round1StructArrayUtil();
        retEnv = client.sendMsg(util, url, soapAction);
        tempPath = resFilePath + "Round1StructArrayRes.xml";
        result = compare(retEnv, tempPath);
        assertTrue(result);
    }
}
