package interop.doclit;

import junit.framework.TestCase;

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
public class InteropStubTest extends TestCase{

//    private static String ASP_NET_ENDPOINT = "http://mssoapinterop.org/asmx/wsdl/InteropTestDocLit.asmx";
    private static String ASP_NET_ENDPOINT = "http://localhost:8081/asmx/wsdl/InteropTestDocLit.asmx";
    private static String OPENLINK_ENDPOINT = "http://demo.openlinksw.com/r3/DocLit";


    private void testEchoString(String endpoint,String soapAction){
        InteropStub stub = new InteropStub(endpoint);
        stub.setSOAPAction(soapAction==null?"":soapAction);
        String echoValue = "Hello World";
        assertEquals(stub.echoString(echoValue),echoValue);
    }
    private void testEchoStringArray(String endpoint,String soapAction){
        InteropStub stub = new InteropStub(endpoint);
        stub.setSOAPAction(soapAction==null?"":soapAction);
        String[] echoValueArray = new String[20];
        for (int i = 0; i < echoValueArray.length; i++) {
            echoValueArray[i]="s"+i;
        }
        String[] returnEchoArray = stub.echoStringArray(echoValueArray);

        //the elements are added in reverse order. So comparison must be
        //inverted
        int length = returnEchoArray.length;
        for (int i = 0; i < length; i++) {
            assertEquals(returnEchoArray[i],echoValueArray[length-1-i]);
        }
    }

    private void testEchoStruct(String endpoint,String soapAction){
        InteropStub stub = new InteropStub(endpoint);
        stub.setSOAPAction(soapAction==null?"":soapAction);

        SOAPStruct structToSend = new SOAPStruct();
        structToSend.setVarFloat(new Float(22.22).floatValue());
        structToSend.setVarInt(12);
        structToSend.setVarString("Hello");

        SOAPStruct returnStruct = stub.echoStruct(structToSend);

        assertTrue(returnStruct.equals(structToSend));

    }

    public void testASPEndPoint(){
//       testEchoString(ASP_NET_ENDPOINT,"\"http://soapinterop.org/\"");
//        testEchoStringArray(ASP_NET_ENDPOINT,"\"http://soapinterop.org/\"");
        testEchoStruct(ASP_NET_ENDPOINT,"\"http://soapinterop.org/\"");

    }
    public void testOpenlinkEndPoint(){
        testEchoString(OPENLINK_ENDPOINT,null);

    }

//    public static void main(String[] args) {
//        String x = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header></soapenv:Header><soapenv:Body><itop:echoStringParam xmlns:itop=\"http://soapinterop.org/xsd\">Hello World</itop:echoStringParam></soapenv:Body></soapenv:Envelope>";
//
//        System.out.println(x);
//        System.out.println(x.getBytes().length);
//    }
}
