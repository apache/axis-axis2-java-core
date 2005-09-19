
package org.apache.axis2.interopt.sun.simple;

import junit.framework.TestCase;
import org.apache.axis2.interopt.sun.round4.simple.EchoBlockingClient;
import org.apache.axis2.interopt.sun.round4.simple.util.EchoEmptyFaultClientUtil;
import org.apache.axis2.interopt.sun.round4.simple.util.EchoMultipleFaults1ClientUtil;
import org.apache.axis2.interopt.sun.round4.simple.util.EchoMultipleFaults2ClientUtil;
import org.apache.axis2.interopt.sun.round4.simple.util.EchoMultipleFaults3Clientutil;
import org.apache.axis2.interopt.sun.round4.simple.util.EchoMultipleFaults4ClientUtil;
import org.apache.axis2.interopt.sun.round4.simple.util.EchoStringFaultClientUtil;
import org.apache.axis2.interopt.sun.round4.simple.util.SunGroupHClientUtil;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.llom.exception.XMLComparisonException;
import org.apache.axis2.om.impl.llom.util.XMLComparator;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Nadana
 * Date: Aug 6, 2005
 * Time: 2:39:44 PM
 * To change this template use File | Settings | File Templates.
 */
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
                     .getContextClassLoader().getResourceAsStream("/" + filepath);
            javax.xml.stream.XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(stream);
            OMXMLParserWrapper builder = new StAXSOAPModelBuilder(parser, null);
            SOAPEnvelope resEnv = (SOAPEnvelope) builder.getDocumentElement();
            OMElement resElementtobe = resEnv.getBody().getFirstElement();
            XMLComparator comparator = new XMLComparator();
            compare = comparator.compare(retEle,resElementtobe);
        }
        return compare;
    }





    public void testEchoEmptyFault()  throws Exception{
        util=new EchoEmptyFaultClientUtil();
        retEle =client.sendMsg(util,soapAction);
        assertTrue(Compare(retEle,"/interopt/sun/round4/simple/resEmptyfault.xml"));


    }


    //todo fix me Nadana , this fails due to bug in XML Comparator
    //echoBaseSoapStructFault
//    public void testIntArrayFault()throws Exception{
//        util=new EchoIntArrayFaultClientUtil();
//        retEle = client.sendMsg(util,soapAction);
//        assertTrue(Compare( retEle,"/interopt/sun/round4/simple/resIntArrayFault.xml"));
//    }




    //echoExtendedStructFault
    public void testStringFault()throws Exception{
        util=new EchoStringFaultClientUtil();
        retEle = client.sendMsg(util,soapAction);
        assertTrue(Compare( retEle,"/interopt/sun/round4/simple/resStringFault.xml"));



    }


    //echomultiplefaults1
    public void testMultiplefaults1()throws Exception{
        util=new EchoMultipleFaults1ClientUtil();
        retEle = client.sendMsg(util,soapAction);
        assertTrue(Compare( retEle,"/interopt/sun/round4/simple/resMultipleFaults1.xml"));
    }

    //echomultiplefaults2
    public void testMultiplefaults2()throws Exception{
        util=new EchoMultipleFaults2ClientUtil();
        retEle = client.sendMsg(util,soapAction);
        assertTrue(Compare( retEle,"/interopt/sun/round4/simple/resMultipleFaults2.xml"));


    }

    //echomultiplefaults3
    public void testMultiplefaults3()throws Exception{
        util=new EchoMultipleFaults3Clientutil();
        retEle = client.sendMsg(util,soapAction);
        assertTrue(Compare( retEle,"/interopt/sun/round4/simple/resMultipleFaults3.xml"));


    }
    //echomultiplefaults4
    public void testMultiplefaults4()throws Exception{
        util=new EchoMultipleFaults4ClientUtil();
        retEle = client.sendMsg(util,soapAction);
        assertTrue(Compare( retEle,"/interopt/sun/round4/simple/resMultipleFaults4.xml"));

    }

}