package org.apache.axis2.jaxws.sample;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.SOAPBinding;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.sample.addressbook.AddEntry;
import org.apache.axis2.jaxws.sample.addressbook.AddEntryResponse;
import org.apache.axis2.jaxws.sample.addressbook.AddressBookEntry;
import org.apache.axis2.jaxws.sample.addressbook.ObjectFactory;

public class AddressBookTests extends TestCase {

    private static final String NAMESPACE = "http://org/apache/axis2/jaxws/sample/addressbook";
    private static final QName QNAME_SERVICE = new QName(
            NAMESPACE, "AddressBookService");
    private static final QName QNAME_PORT = new QName(
            NAMESPACE, "AddressBook");
    private static final String URL_ENDPOINT = "http://localhost:8080/axis2/services/AddressBookService";
    
    public void testAddressBookEndpoint() throws Exception {
        JAXBContext jbc = JAXBContext.newInstance("org.apache.axis2.jaxws.sample.addressbook");
        
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP12HTTP_BINDING, URL_ENDPOINT);
        Dispatch<Object> dispatch = service.createDispatch(
                QNAME_PORT, jbc, Mode.PAYLOAD);
                
        ObjectFactory factory = new ObjectFactory();
        AddEntry request = factory.createAddEntry();
        AddressBookEntry content = factory.createAddressBookEntry();
        
        content.setFirstName("Ron");
        content.setLastName("Testerson");
        content.setPhone("512-459-2222");
        
        request.setEntry(content);
        
        AddEntryResponse response = (AddEntryResponse) dispatch.invoke(request);
        
        assertNotNull(response);
        
        System.out.println(">> RESPONSE RECEIVED!!! " + response.isStatus());
    }
}
