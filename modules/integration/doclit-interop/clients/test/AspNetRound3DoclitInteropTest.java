package test;

import junit.framework.TestCase;
import java.rmi.RemoteException;
import test.stub.databinding.org.soapinterop.*;
import test.stub.WSDLInteropTestDocLitSoapStub;


/**
 * Created by IntelliJ IDEA.
 * User: Gayan
 * Date: Sep 16, 2005
 * Time: 9:29:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class AspNetRound3DoclitInteropTest extends TestCase{

    WSDLInteropTestDocLitSoapStub stub = null;
    EchoStringParamDocument strParaDoc = null;
    String str = "Gayan Asanka";
    EchoStringReturnDocument retStrDoc = null;
    EchoStringArrayParamDocument strArrayParaDoc = null;
    ArrayOfString strLitArr = null;
    EchoStringArrayReturnDocument retArrayDoc = null;
    String[] strArry = {"String 1", "String 2", "String 3"};
    ArrayOfString retArray = null;
    EchoStructParamDocument structParaDoc = null;
    SOAPStruct soapStruct = null;
    float flt = (float)1234.456;
    int i = 123456;
    EchoStructReturnDocument retStructDoc = null;
    SOAPStruct retStruct = null;

    public void setUp() throws Exception {
        stub = new WSDLInteropTestDocLitSoapStub();
    }

    public void testEchoString() throws RemoteException {
        strParaDoc = EchoStringParamDocument.Factory.newInstance();
        strParaDoc.setEchoStringParam(str);
        retStrDoc = stub.echoString(strParaDoc);
        assertEquals(str,retStrDoc.getEchoStringReturn());
    }

    public void testEchoStringArray() throws RemoteException {
        strLitArr = ArrayOfString.Factory.newInstance();
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
}
