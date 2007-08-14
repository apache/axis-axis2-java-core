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

package test;

import junit.framework.TestCase;
import test.stub.WSDLInteropTestDocLitPortTypeStub;
import test.stub.databinding.org.soapinterop.*;

import java.rmi.RemoteException;

public class WMRound3DoclitInteropTest extends TestCase{

    WSDLInteropTestDocLitPortTypeStub stub = null;
    EchoStringParamDocument strParaDoc = null;
    String str = "Gayan Asanka";
    EchoStringReturnDocument retStrDoc = null;
    EchoStringArrayParamDocument strArrayParaDoc = null;
    ArrayOfstringLiteral strLitArr = null;
    EchoStringArrayReturnDocument retArrayDoc = null;
    String[] strArry = {"String 1", "String 2", "String 3"};
    ArrayOfstringLiteral retArray = null;
    EchoStructParamDocument structParaDoc = null;
    SOAPStruct soapStruct = null;
    float flt = (float)1234.456;
    int i = 123456;
    EchoStructReturnDocument retStructDoc = null;
    SOAPStruct retStruct = null;

    public void setUp() throws Exception {
        stub = new WSDLInteropTestDocLitPortTypeStub();
    }

    public void testEchoString() throws RemoteException {
        strParaDoc = EchoStringParamDocument.Factory.newInstance();
        strParaDoc.setEchoStringParam(str);
        retStrDoc = stub.echoString(strParaDoc);
        assertEquals(str,retStrDoc.getEchoStringReturn());
    }

    public void testEchoStringArray() throws RemoteException {
        strLitArr = ArrayOfstringLiteral.Factory.newInstance();
        strLitArr.setStringArray(strArry);
        strArrayParaDoc = EchoStringArrayParamDocument.Factory.newInstance();
        strArrayParaDoc.setEchoStringArrayParam(strLitArr);
        retArrayDoc = stub.echoStringArray(strArrayParaDoc);
        retArray=retArrayDoc.getEchoStringArrayReturn();
        assertEquals(strArry[0],retArray.getStringArray()[0]);
        assertEquals(strArry[1],retArray.getStringArray()[1]);
        assertEquals(strArry[2],retArray.getStringArray()[2]);
    }

    public void testEchoStruct() throws RemoteException {
        soapStruct = SOAPStruct.Factory.newInstance();
        soapStruct.setVarFloat(flt);
        soapStruct.setVarInt(i);
        soapStruct.setVarString(str);
        structParaDoc = EchoStructParamDocument.Factory.newInstance();
        structParaDoc.setEchoStructParam(soapStruct);
        retStructDoc = stub.echoStruct(structParaDoc);
        retStruct = retStructDoc.getEchoStructReturn();
        assertEquals(flt,retStruct.getVarFloat(),0);
        assertEquals(i,retStruct.getVarInt());
        assertEquals(str,retStruct.getVarString());

    }

//    public void testEchoVoid() throws RemoteException {
//        OMFactory fac = OMAbstractFactory.getOMFactory();
//        OMElement elem = fac.createOMElement("gayan",null);
//        stub.echoVoid(elem);
//    }
}
