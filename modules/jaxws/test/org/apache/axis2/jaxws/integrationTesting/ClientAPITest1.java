package org.apache.axis2.jaxws.integrationTesting;

import junit.framework.TestCase;

import javax.xml.namespace.QName;
import java.net.URL;

import javax.xml.ws.Call;
import javax.xml.ws.ParameterMode;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceFactory;

public class ClientAPITest1 extends TestCase {

	public static void main(String[] args) {
	}

	public ClientAPITest1(String name) {
		super(name);
	}
	
	public void testDII() {
		try {
			ServiceFactory sf = ServiceFactory.newInstance();
			Service s = sf.createService(new URL(getTestResourceDirectory()+ "/Echo.wsdl") , new QName("EchoService"));
			
			Call call = s.createCall();
			call.addParameter("param1", new QName("Here the URL for XSD should be given","string"), java.lang.String.class, ParameterMode.IN);
			call.setReturnType(new QName("URL of XSD","string"), String.class);
			Object[] inParams = new Object[]{"hello World!"};
			String response = (String) call.invoke(inParams);
			assertEquals("hello World!",response);
		}catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	private String getTestResourceDirectory() {
		return "C:\\workspace\\3.1Workspace\\JAXRPC2_Work\\test-resources";
	}

}
