package test;

import test.stub.RetHeaderPortTypeStub;
import test.stub.databinding.org.soapinterop.*;
import org.apache.axis2.AxisFault;
import junit.framework.TestCase;

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
