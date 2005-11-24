package org.apache.axis2.jaxws.handler;

import org.apache.axis2.jaxws.handler.soap.SOAPMessageContextImpl;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.handlers.AbstractHandler;

public class Axis2Handler extends AbstractHandler {
	
	private javax.xml.ws.handler.AbstractHandler jaxRpcHandler;
	
	public Axis2Handler(){
		//Default constructor, jaxRpcHandler will be set through setter method
	}
	
	public Axis2Handler(javax.xml.ws.handler.AbstractHandler jaxRpcHandler){
		this.jaxRpcHandler = jaxRpcHandler;
	}
	
	public void setJaxRpcHandler(javax.xml.ws.handler.AbstractHandler handler){
		jaxRpcHandler = handler;
	}
	
	public javax.xml.ws.handler.AbstractHandler getJaxRpcHandler(){
		return jaxRpcHandler;
	}

	public void invoke(MessageContext mc) throws AxisFault {
		//Call the handlerMessage() method from 
		jaxRpcHandler.handleMessage(new SOAPMessageContextImpl(mc));
	}

}
