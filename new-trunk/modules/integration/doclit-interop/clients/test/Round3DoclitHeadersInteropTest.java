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

package test;

import junit.framework.TestCase;
import test.stub.RetHeaderPortTypeStub;
import test.stub.databinding.org.soapinterop.*;

public class Round3DoclitHeadersInteropTest extends TestCase{

    RetHeaderPortTypeStub stub = null;
    Header2 h2 = null;
    Header2Document h2Doc = null;
    Header1 h1 = null;
    Header1Document h1Doc = null;
    EchoStringParamDocument paraDoc = null;
    String str = "String Parameter";


    public void testEchoString() throws Exception{
        stub = new RetHeaderPortTypeStub();
        h2 = Header2.Factory.newInstance();
        h2.setInt(456);
        h2.setString("Header2 para");
        h2Doc = Header2Document.Factory.newInstance();
        h2Doc.setHeader2(h2);
        h1 = Header1.Factory.newInstance();
        h1.setInt(123);
        h1.setString("string header1 para");
        h1Doc = Header1Document.Factory.newInstance();
        h1Doc.setHeader1(h1);
        paraDoc = EchoStringParamDocument.Factory.newInstance();
        paraDoc.setEchoStringParam(str);
        EchoStringReturnDocument retDoc = stub.echoString(paraDoc, h1Doc, h2Doc);
        assertEquals(str,retDoc.getEchoStringReturn());
    }
}
