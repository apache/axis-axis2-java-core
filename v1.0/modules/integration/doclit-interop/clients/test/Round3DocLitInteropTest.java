//package test;
//
//import junit.framework.TestCase;
//import org.soapinterop.xsd.Document;
//import org.soapinterop.xsd.XDocumentDocument1;
//import org.soapinterop.xsd.Person;
//import org.soapinterop.xsd.XPersonDocument;
//import test.stub1.SoapInteropCompound1PortTypeStub;
//
//public class Round3DocLitInteropTest extends TestCase{
//
//    public void testTest1() throws Exception{
//        SoapInteropCompound1PortTypeStub stub = new SoapInteropCompound1PortTypeStub();
//            Document xDoc = Document.Factory.newInstance();
//            xDoc.setID("123");
//            xDoc.setStringValue("Gayan Asanka");
//            XDocumentDocument1 doc = XDocumentDocument1.Factory.newInstance();
//            doc.setXDocument(xDoc);
//            System.out.println( stub.echoDocument(doc));
//
//            Person pers = Person.Factory.newInstance();
//            pers.setName("Gayan Asanka");
//            pers.setAge(28);
//            pers.setID((float)123.456);
//            pers.setMale(true);
//            XPersonDocument xPersDoc = XPersonDocument.Factory.newInstance();
//            xPersDoc.setXPerson(pers);
//            System.out.println(stub.echoPerson(xPersDoc));
//    }
//
//}
