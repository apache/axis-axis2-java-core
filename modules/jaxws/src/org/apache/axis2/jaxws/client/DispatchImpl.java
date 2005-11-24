package org.apache.axis2.jaxws.client;

import java.rmi.RemoteException;
import java.util.concurrent.Future;


import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.om.impl.llom.factory.OMXMLBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Dispatch;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Response;
import javax.xml.namespace.QName;

public class DispatchImpl extends BindingProviderImpl implements Dispatch {

	private String targetEndpointAddress;
	private QName operationName;
	public DispatchImpl(String tgtEndptAddr, QName opName) {
		super();
		this.targetEndpointAddress=tgtEndptAddr;
		this.operationName=opName;
		// TODO Auto-generated constructor stub
	}

	/**
	 * Method invoke
	 * Invoke a service operation synchronously. The client is responsible 
	 * for ensuring that the msg object when marshalled is formed according 
	 * to the requirements of the protocol binding in use.
	 * 
	 * @param msg - An object that will form the message or payload of the 
	 * message used to invoke the operation.
	 * @return The response message or message payload to the operation 
	 * invocation.
	 * @throws java.rmi.RemoteException - If a fault occurs during 
	 * communication with the service
	 * @throws WebServiceException - If there is any error in the configuration 
	 * of the Dispatch instance. 
	 */
	public Object invoke(Object msg) throws RemoteException {
		OMElement response;
		try {
			//This try-catch is exclusively for the 'Message Payload' kind of Mode.
		//Assuming SOAP protocol, I'll take it that the <code>Object</code> that
		//I received as input param is a piece of XML text that can represent 
		//soap:body element
		
		//We shall build an OMElement out of the input param and use it to
		//invoke the service.
			Source src = null; //TODO actually sould create a source object
						//out of the input msg we got.
			StAXOMBuilder staxOMBuilder = OMXMLBuilderFactory.
            createStAXOMBuilder(OMAbstractFactory.getOMFactory(),
                    XMLInputFactory.newInstance().createXMLStreamReader(src));
			OMElement inputElement = staxOMBuilder.getDocumentElement();
			//use this inputElement to invoke the underlying Axis2 call's
			//invokeBlocking(...) method.
			org.apache.axis2.clientapi.Call axis2Call = new org.apache.axis2.clientapi.Call();
			axis2Call.setTo(new EndpointReference(targetEndpointAddress));
			response = axis2Call.invokeBlocking(operationName.getLocalPart(),inputElement);
			//TODO actually the response message better be transformed to a Source object
			//and returned.
		} catch(Exception e) {
			throw new WebServiceException(e);
		}
		
		//Implementation (at least a makeshift as above) should happen for the other
		//mode also i.e., the Message mode.
		return response;
	}

	/**
	 * Method invokeAsync
	 * Invoke a service operation asynchronously. The method returns without 
	 * waiting for the response to the operation invocation, the results of 
	 * the operation are obtained by polling the returned Response. The 
	 * client is responsible for ensuring that the msg object when marshalled 
	 * is formed according to the requirements of the protocol binding in use.
	 * 
	 * @param msg - An object that will form the message or payload of the 
	 * message used to invoke the operation.
	 * @return The response message or message payload to the operation 
	 * invocation.
	 * @throws WebServiceException - If there is any error in the configuration 
	 * of the Dispatch instance.
	 */
	public Response invokeAsync(Object msg) throws WebServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Method invokeAsync
	 * Invoke a service operation asynchronously. The method returns without 
	 * waiting for the response to the operation invocation, the results of 
	 * the operation are communicated to the client via the passed in handler.
	 * The client is responsible for ensuring that the msg object when 
	 * marshalled is formed according to the requirements of the protocol 
	 * binding in use.
	 * 
	 * @param msg - An object that will form the message or payload of the 
	 * message used to invoke the operation.
	 * @param handler - The handler object that will receive the response to 
	 * the operation invocation.
	 * @return A Future object that may be used to check the status of the 
	 * operation invocation. This object must not be used to try to obtain 
	 * the results of the operation - the object returned from Future.get() 
	 * is implementation dependent and any use of it will result in 
	 * non-portable behaviour.
	 * @throws WebServiceException - If there is any error in the configuration 
	 * of the Dispatch instance
	 */
	public Future invokeAsync(Object msg, AsyncHandler handler)
			throws WebServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Method invokeOneWay
	 * Invokes a service operation using the one-way interaction mode. The 
	 * operation invocation is logically non-blocking, subject to the 
	 * capabilities of the underlying protocol, no results are returned. 
	 * When the protocol in use is SOAP/HTTP, this method must block until an 
	 * HTTP response code has been received or an error occurs. The client is 
	 * responsible for ensuring that the msg object when marshalled is formed 
	 * according to the requirements of the protocol binding in use.
	 * 
	 * @param msg - An object that will form the message or payload of the 
	 * message used to invoke the operation.
	 * @throws WebServiceException - If there is any error in the configuration 
	 * of the Dispatch instance or if an error occurs during the invocation.
	 */
	public void invokeOneWay(Object msg) throws WebServiceException {
		// TODO Auto-generated method stub

	}

}
