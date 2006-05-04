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

package test.interop.sun.complex;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.om.impl.exception.XMLComparisonException;
import org.apache.axiom.om.impl.llom.util.XMLComparator;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

import test.interop.sun.round4.complex.EchoBaseStructFaultClientutil;
import test.interop.sun.round4.complex.EchoBlockingClient;
import test.interop.sun.round4.complex.EchoExtendedStructFaultClientUtil;
import test.interop.sun.round4.complex.EchoMultipleFaults1ClientUtil;
import test.interop.sun.round4.complex.EchoMultipleFaults2ClientUtil;
import test.interop.sun.round4.complex.EchoSOAPStructFaultClientUtil;
import test.interop.sun.round4.complex.SunGroupHClientUtil;
public class SunGroupHMain extends TestCase {

    EchoBlockingClient client=null;
    OMElement retEle;
    SunGroupHClientUtil util=null;
    String soapAction="";

    public void setUp(){
        client=new EchoBlockingClient();
    }

    private boolean Compare(OMElement retEle,String filepath) throws XMLStreamException,
            XMLComparisonException {
        boolean compare=false;
        if(retEle!=null){
             InputStream stream = Thread.currentThread()
                     .getContextClassLoader().getResourceAsStream(filepath);
            javax.xml.stream.XMLStreamReader parser = StAXUtils.createXMLStreamReader(stream);
            OMXMLParserWrapper builder = new StAXSOAPModelBuilder(parser, null);
            SOAPEnvelope resEnv = (SOAPEnvelope) builder.getDocumentElement();
            OMElement resElementtobe = resEnv.getBody().getFirstElement();
            XMLComparator comparator = new XMLComparator();
            compare = comparator.compare(retEle,resElementtobe);
        }
        return compare;
    }



    //echoSoapStructFault
    public void testSoapStructFault() throws Exception {
        util=new EchoSOAPStructFaultClientUtil();
        retEle = client.sendMsg(util,soapAction);
        assertTrue(
                Compare(retEle,"interop/sun/round4/complex/resSoapStructFault.xml"));
    }


    //echoBaseSoapStructFault
    public void testBaseStructFault()throws Exception{
        util=new EchoBaseStructFaultClientutil();
        retEle = client.sendMsg(util,soapAction);
        assertTrue(
                Compare( retEle,"interop/sun/round4/complex/resBaseStrutFault.xml"));

    }

    //echoExtendedStructFault
    public void testExtendedStructFault()throws Exception{
        util=new EchoExtendedStructFaultClientUtil();
        retEle = client.sendMsg(util,soapAction);
        assertTrue(
                Compare(retEle,"interop/sun/round4/complex/resExtendedStructFault.xml"));

    }

    //echomultiplefaults1
    public void testMultiplefaults1()throws Exception{
        util=new EchoMultipleFaults1ClientUtil();
        retEle = client.sendMsg(util,soapAction);
        assertTrue(
                Compare( retEle,"interop/sun/round4/complex/resMultipleFaults1.xml"));
    }

    //echomultiplefaults2
    public void testMultiplefaults2()throws Exception{
        util=new EchoMultipleFaults2ClientUtil();
        retEle = client.sendMsg(util,soapAction);
        assertTrue(
                Compare( retEle,"interop/sun/round4/complex/resMultipleFaults2.xml"));
    }

}

