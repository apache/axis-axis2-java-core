package org.apache.axis2.jaxws.server;

//import java.net.URL;


import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

import javax.xml.namespace.QName;
import javax.xml.ws.Call;
import javax.xml.ws.ParameterMode;
import javax.xml.ws.Service;
//import javax.xml.rpc.ServiceFactory;

import org.apache.axis2.jaxws.client.ServiceImpl;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.OMOutputImpl;

import junit.framework.TestCase;

public class ServerTestingPurposeClient extends TestCase {

	public static void main(String[] args) {
	}

	public ServerTestingPurposeClient(String name) {
		super(name);
	}

	public void testInvoke1() {
		try {

			Service s = new ServiceImpl();
			Call call = s.createCall();
			call.setOperationName(new QName("http://testingURL.org/","getVersion"));
			call.setTargetEndpointAddress("http://localhost:9090/axis2/services/Version");
			call.addParameter("param1", new QName("http://www.w3.org/2001/XMLSchema","any"), java.lang.Object.class, ParameterMode.IN);
			call.setReturnType(new QName("http://www.w3.org/2001/XMLSchema","any"), Object.class);
			Object[] inParams = new Object[]{"hello World!"};
			OMElement response = (OMElement)call.invoke(inParams);

			try {
				OutputStream fos = new BufferedOutputStream(System.out);
				OMOutputImpl otpt = new OMOutputImpl(fos, false);
				response.serialize(otpt);
				fos.flush();
				otpt.flush();
				} catch (Exception e){}
				
			String resultString = response.getText();
			assertEquals("This is just to show that on " +
					"the server side the JAXRPCInOutMessageReceiver is chosen by " +
					"Axis!" , resultString);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			fail(e.getMessage());
		}
	}
}
