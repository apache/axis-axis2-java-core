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


package test.interop.sun.simple;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.om.impl.exception.XMLComparisonException;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

import test.interop.sun.round4.simple.EchoBlockingClient;
import test.interop.sun.round4.simple.util.EchoEmptyFaultClientUtil;
import test.interop.sun.round4.simple.util.EchoIntArrayFaultClientUtil;
import test.interop.sun.round4.simple.util.EchoMultipleFaults1ClientUtil;
import test.interop.sun.round4.simple.util.EchoMultipleFaults2ClientUtil;
import test.interop.sun.round4.simple.util.EchoMultipleFaults3Clientutil;
import test.interop.sun.round4.simple.util.EchoMultipleFaults4ClientUtil;
import test.interop.sun.round4.simple.util.EchoStringFaultClientUtil;
import test.interop.sun.round4.simple.util.SunGroupHClientUtil;
import test.interop.util.XMLComparatorInterop;

public class SunGroupHMain extends TestCase{

    EchoBlockingClient client=null;
    OMElement retEle;
    SunGroupHClientUtil util=null;
    String soapAction="";

    public void setUp(){
        client=new EchoBlockingClient();

    }

    private boolean Compare(OMElement retEle,String filepath) throws XMLStreamException,
            XMLComparisonException{
        boolean compare=false;
        if(retEle!=null){
             InputStream stream = Thread.currentThread()
                     .getContextClassLoader().getResourceAsStream(filepath);
            javax.xml.stream.XMLStreamReader parser = StAXUtils.createXMLStreamReader(stream);
            OMXMLParserWrapper builder = new StAXSOAPModelBuilder(parser, null);
            SOAPEnvelope resEnv = (SOAPEnvelope) builder.getDocumentElement();
            OMElement resElementtobe = resEnv.getBody().getFirstElement();
            XMLComparatorInterop comparator = new XMLComparatorInterop();
            compare = comparator.compare(retEle,resElementtobe);
        }
        return compare;
    }





    public void testEchoEmptyFault()  throws Exception{
        util=new EchoEmptyFaultClientUtil();
        retEle =client.sendMsg(util,soapAction);
        assertTrue(Compare(retEle,"interop/sun/round4/simple/resEmptyfault.xml"));


    }


    //todo fix me Nadana , this fails due to bug in XML Comparator
    //echoBaseSoapStructFault
    public void testIntArrayFault()throws Exception{
        util=new EchoIntArrayFaultClientUtil();
        retEle = client.sendMsg(util,soapAction);
        assertTrue(Compare( retEle,"interop/sun/round4/simple/resIntArrayFault.xml"));
    }




    //echoExtendedStructFault
    public void testStringFault()throws Exception{
        util=new EchoStringFaultClientUtil();
        retEle = client.sendMsg(util,soapAction);
        assertTrue(Compare( retEle,"interop/sun/round4/simple/resStringFault.xml"));



    }


    //echomultiplefaults1
    public void testMultiplefaults1()throws Exception{
        util=new EchoMultipleFaults1ClientUtil();
        retEle = client.sendMsg(util,soapAction);
        assertTrue(Compare( retEle,"interop/sun/round4/simple/resMultipleFaults1.xml"));
    }

    //echomultiplefaults2
    public void testMultiplefaults2()throws Exception{
        util=new EchoMultipleFaults2ClientUtil();
        retEle = client.sendMsg(util,soapAction);
        assertTrue(Compare( retEle,"interop/sun/round4/simple/resMultipleFaults2.xml"));


    }

    //echomultiplefaults3
    public void testMultiplefaults3()throws Exception{
        util=new EchoMultipleFaults3Clientutil();
        retEle = client.sendMsg(util,soapAction);
        assertTrue(Compare( retEle,"interop/sun/round4/simple/resMultipleFaults3.xml"));


    }
    //echomultiplefaults4
    public void testMultiplefaults4()throws Exception{
        util=new EchoMultipleFaults4ClientUtil();
        retEle = client.sendMsg(util,soapAction);
        assertTrue(Compare( retEle,"interop/sun/round4/simple/resMultipleFaults4.xml"));

    }

}