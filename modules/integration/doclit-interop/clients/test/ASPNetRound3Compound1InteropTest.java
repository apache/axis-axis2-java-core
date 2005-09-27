 package test;

    import junit.framework.TestCase;

    import java.rmi.RemoteException;

    import test.stub.Compound1SoapStub;
    import test.stub.databinding.org.soapinterop.*;

    /**
     * Created by IntelliJ IDEA.
     * User: Gayan
     * Date: Sep 15, 2005
     * Time: 6:34:39 PM
     * To change this template use File | Settings | File Templates.
/**
 * Created by IntelliJ IDEA.
 * User: Gayan
 * Date: Sep 22, 2005
 * Time: 9:52:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class ASPNetRound3Compound1InteropTest extends TestCase{




        Compound1SoapStub stub = null;
        XDocumentDocument1 xDocDoc1 = null;
        Document doc = null;
        String id = "Document1";
        String val = "this is a document";
        XPersonDocument persDoc = null;
        Person person = null;
        String name = "Gayan Asanka";
        double age = 25;
        boolean male = true;
        float personID = (float)456.3123;
        ResultPersonDocument retPersDoc =null;
        Person retPers = null;



        ResultDocumentDocument1 retDoc1 = null;
        Document retDoc=null;

        public void setUp() throws Exception {
           stub = new Compound1SoapStub();
        }

        public void testEchoDocument() throws Exception {

            doc = Document.Factory.newInstance();
            xDocDoc1 = XDocumentDocument1.Factory.newInstance();
            doc.setStringValue(val);
            doc.setID(id);
            xDocDoc1.setXDocument(doc);
            retDoc1 = stub.echoDocument(xDocDoc1);
            retDoc =retDoc1.getResultDocument();
            //id = "nothing"; //to fail the test
            assertEquals(id,retDoc.getID());
            assertEquals(val,retDoc.getStringValue());
        }

        public void testEchoPerson() throws RemoteException {
            person = Person.Factory.newInstance();
            person.setName(name);
            person.setAge(age);
            person.setMale(male);
            person.setID(personID);
            persDoc = XPersonDocument.Factory.newInstance();
            persDoc.setXPerson(person);
            retPersDoc = stub.echoPerson(persDoc);
            retPers = retPersDoc.getResultPerson();
            assertEquals(name,retPers.getName());
            assertEquals(age,retPers.getAge(),0);
            assertEquals(male,retPers.getMale());
            assertEquals(personID,retPers.getID(),0);
        }
    }



