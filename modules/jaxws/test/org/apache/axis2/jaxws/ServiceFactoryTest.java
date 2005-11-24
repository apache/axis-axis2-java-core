package org.apache.axis2.jaxws;

import junit.framework.TestCase;

import javax.xml.ws.Service;
import javax.xml.ws.ServiceFactory;
import javax.xml.namespace.QName;

public class ServiceFactoryTest extends TestCase {

	public ServiceFactoryTest(String name) {
		super(name);
	}
	
	public void testServiceFactoryInstantiation() {
		try {
			ServiceFactory sf = ServiceFactory.newInstance();
			assertTrue(sf!=null);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void testServiceCreation() {
		try {
			ServiceFactory sf = ServiceFactory.newInstance();
			Service s = sf.createService(null,new QName("http://defaultPackage","Echo"));
			assertTrue(s!=null);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
